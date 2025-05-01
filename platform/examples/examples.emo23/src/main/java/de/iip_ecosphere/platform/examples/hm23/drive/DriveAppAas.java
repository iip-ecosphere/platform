/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm23.drive;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.examples.hm23.CobotCommands;
import de.iip_ecosphere.platform.examples.hm23.Commands;
import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DataIngestors;
import de.iip_ecosphere.platform.services.environment.ParameterConfigurer;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TraceToAasService;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter.ConverterInstances;
import de.iip_ecosphere.platform.services.environment.testing.DataRecorder;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.transport.AppIntercom;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;
import de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProvider;
import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProviderWithJsonDefault;
import de.iip_ecosphere.platform.transport.serialization.GenericJsonSerializer;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.configuration.defaultLib.MipIdentificationSensor.MipTimerTask;
import iip.datatypes.Command;
import iip.datatypes.DriveAiResult;
import iip.datatypes.DriveBeckhoffOutput;
import iip.datatypes.DriveCommand;
import iip.datatypes.DriveCommandImpl;
import iip.datatypes.LenzeDriveMeasurement;
import iip.datatypes.MipAiPythonOutput;
import iip.datatypes.MipMqttInput;
import iip.datatypes.PlcInput;
import iip.datatypes.PlcOutput;

/**
 * Drive application AAS. In this app, this class also contains the controlling state machine, which in the FL app 
 * is the {@link de.iip_ecosphere.platform.examples.hm23.ActionDecider}. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class DriveAppAas extends TraceToAasService implements iip.interfaces.DriveAppAasInterface {

    public static final long OVERALL_TIMEOUT = TimeUnit.MINUTES.toMillis(2);
    private static final String OP_REQUEST_DRIVE = "requestDrive";
    private static final String OP_GET_TENSION = "getTension";
    private static final String OP_GET_FRICTION = "getFriction";
    private static final String OP_GET_DRIVE_POSITION = "getDrivePosition";
    private static final String OP_GET_DRIVE_WAIT_FOR_COMMAND = "getDriveWaitForCommand";
    private static final String PROP_TENSION = "tension";
    private static final String PROP_FRICTION = "friction";
    private static final String PROP_DRIVE_POSITION = "drivePosition";
    private static final String PROP_DRIVE_WAIT_FOR_COMMAND = "driveWaitForCommand";
    
    private static final boolean MOCK_DRIVE_REQUEST = Boolean.valueOf(
        System.getProperty("iip.app.hm23.driveBypassLockRequest", "false"));
    private static final boolean BYPASS_MIP = Boolean.valueOf(
        System.getProperty("iip.app.hm23.driveBypassMIP", "false"));
    private static final boolean ROTATE_OBSTACLES = Boolean.valueOf(
        System.getProperty("iip.app.hm23.driveRotateObstacles", "false"));
    
    private static final int DRIVE_START_MM = 300;
    private static final int DRIVE_END_MM = 700;
    private static final String[] EXCLUDED_FIELDS = {"energy", "drive", "PROCESS"};
    
    private DataIngestors<DriveCommand> driveCommandIngestors = new DataIngestors<>();
    private DataIngestors<Command> commandIngestors = new DataIngestors<>();
    private DataIngestors<PlcInput> plcInputIngestors = new DataIngestors<>();
    private DataIngestors<MipMqttInput> mipInputIngestors = new DataIngestors<>();
    private Server opServer;
    private AppIntercom<Command> intercom = new AppIntercom<Command>(d -> processCommand(d), Command.class);
    private State currentState = State.STOP;
    private State nextWaitState = State.STOP;
    private long lastStateChange;
    private DriveBeckhoffOutput lastDriveOut;
    private final DriveRequest fwdDriveRequest = new DriveRequest(0, 100, DRIVE_END_MM, 100);
    private final DriveRequest bwdDriveRequest = new DriveRequest(0, 100, DRIVE_START_MM, 100);
    private DriveRequest lastFwdDriveRequest;
    private DriveRequest lastBwdDriveRequest;
    private int driveRequestCounter = ROTATE_OBSTACLES ? 0 : -1; 
    private CobotCommands cobotCommands = new CobotCommands(null, pi -> plcInputIngestors.ingest(pi));
    private String lastDriveError;
    private int sensorFail = 0;
    private String mipSensorId = "ATML2589040200002054";
    private Timer timer = new Timer();
    
    private enum State {
        STOP,
        ID_PRE_SCAN,
        ID_SCAN,
        ID_DRIVE,
        PLACE_CAR,
        WAIT_FOR_DRIVE_FWD_STARTED,
        WAIT_FOR_DRIVE_FWD_COMPLETED,
        WAIT_FOR_DRIVE_BWD,
        WAIT_FOR_DRIVE_BWD_STARTED,
        WAIT_FOR_DRIVE_BWD_COMPLETED,
        WAIT_FOR_APP_ACK,
        WAIT_FOR_ROBOT_BUSY,
        WAIT_FOR_ROBOT_DONE
    }
    
    /**
     * Represents a drive request.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DriveRequest {
        
        private int friction;
        private int tension;
        private double position;
        private double velocity;

        /**
         * Creates a drive request instance.
         * 
         * @param friction the simulated friction (in percent)
         * @param tension the simulated tension (in percent)
         * @param position the target position in mm
         * @param velocity the drive speed in mm/s
         */
        private DriveRequest(int friction, int tension, double position, double velocity) {
            this.friction = friction;
            this.tension = tension;
            this.position = position;
            this.velocity = velocity;
        }

        /**
         * Creates a drive request instance.
         * 
         * @param request the request to take the base data from
         */
        private DriveRequest(DriveRequest request) {
            this.friction = request.friction;
            this.tension = request.tension;
            this.position = request.position;
            this.velocity = request.velocity;
        }

        /**
         * Turns the drive request into a command.
         * 
         * @param execute shall the command be executed or just values be set?
         * @return the corresponding drive command
         */
        private DriveCommand toCommand(boolean execute) {
            DriveCommand cmd = new DriveCommandImpl();
            cmd.setBExecute(execute);
            cmd.setIFriction_set((short) Math.max(0, friction));
            cmd.setITension_set((short) Math.max(0, tension));
            cmd.setRPosition(position);
            cmd.setRVelocity(velocity);
            return cmd;
        }

        /**
         * Changes the obstacle values, usually after copying an existing request.
         * 
         * @param friction the simulated friction (in percent)
         * @param tension the simulated tension (in percent)
         * @return <b>this</b>
         */
        private DriveRequest setObstacle(int friction, int tension) {
            this.friction = friction;
            this.tension = tension;
            return this;
        }
        
        /**
         * Creates a copy of a drive request.
         * 
         * @return the copy
         */
        private DriveRequest copy() {
            return new DriveRequest(this);
        }

        @Override
        public String toString() {
            return "DriveRequest friction: " + friction + " tension: " + tension + " position: " 
                + position + " velocity: " + velocity;
        }
        
    }
    
    static {
        TraceRecord.ignoreClass(LenzeDriveMeasurement.class);
    }

    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public DriveAppAas(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates a service instance. [for testing]
     *
     * @param app static information about the application
     * @param yaml the service description 
     */
    public DriveAppAas(ApplicationSetup app, YamlService yaml) {
        super(app, yaml);
    }
    
    @Override
    protected void registerParameterConfigurers() {
     // parameter must be declared in this form in model!
        addParameterConfigurer(new ParameterConfigurer<String>("mipSensorId", String.class, TypeTranslators.JSON_STRING,
            t -> mipSensorId = t, () -> mipSensorId).withSystemProperty("iip.app.hm23.mipId"));
    }
    
    /**
     * Emulates (fixed) robot id parameter.
     *  
     * @return the robot id
     */
    private int getParameterRobotId() {
        return 1;
    }
    
    @Override
    protected boolean isAasEnabled() {
        return false; // also disabled WS
    }
    
    @Override
    public void processDriveAiResult(DriveAiResult data) {
        // prune data, problems with MQTT
        System.out.println("Sending Drive AI result");
        sendTransportAsync("iip/Drive-app/results", data);
        System.out.println("Sending Drive AI result done");
    }
    
    @Override
    public void processDriveBeckhoffOutput(DriveBeckhoffOutput data) {
        boolean initial = lastDriveOut == null;
        lastDriveOut = data;
        if (initial) {
            // if not in initial place, drive backward; initialize drive, hangs on a fresh day
            sendDriveCommand(bwdDriveRequest, currentState);
        }
        if (currentState == State.WAIT_FOR_DRIVE_FWD_STARTED) {
            if (!data.getBWaitForCommand()) {
                setCurrentState(State.WAIT_FOR_DRIVE_FWD_COMPLETED);
            }
        } else if (currentState == State.WAIT_FOR_DRIVE_FWD_COMPLETED) {
            if (data.getBWaitForCommand()) {
                nextWaitState = State.PLACE_CAR;
                sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_PLACE_CAR);
            }
        } else if (currentState == State.WAIT_FOR_DRIVE_BWD_STARTED) {
            if (!data.getBWaitForCommand()) {
                setCurrentState(State.WAIT_FOR_DRIVE_BWD_COMPLETED);
            }
        } else if (currentState == State.WAIT_FOR_DRIVE_BWD_COMPLETED) {
            if (data.getBWaitForCommand()) {
                System.out.println("SENDING DRIVE_DONE");
                // other app, I'm done, free
                CobotCommands.sendIntercom(intercom, Commands.createCommand(Commands.DRIVE_DONE, null));
                // kept for UI/UMATI
                commandIngestors.ingest(Commands.createCommand(Commands.DRIVE_DONE, null));
                setCurrentState(State.STOP);
            }            
        }
        sendTransportAsync("iip/Drive-app/drive", data);
        try {
            Aas aas = retrieveAas();
            Submodel cmds = aas.getSubmodel(SUBMODEL_COMMANDS);
            AasUtils.setPropertyValueSafe(cmds, PROP_FRICTION, data.getIFriction_actual());
            AasUtils.setPropertyValueSafe(cmds, PROP_TENSION, data.getITension_actual());
            AasUtils.setPropertyValueSafe(cmds, PROP_DRIVE_POSITION, data.getRPosition_actual());
            AasUtils.setPropertyValueSafe(cmds, PROP_DRIVE_WAIT_FOR_COMMAND, data.getBWaitForCommand());
        } catch (IOException e) {
            System.out.println("Cannot update AAS: " + e.getMessage());
        }
    }
    
    @Override
    protected TransportConnector createTransport(BasicSerializerProvider serializationProvider) {
        TransportConnector conn = null;
        if (null != getTransportParameter()) {
            try {
                conn = new PahoMqttV3TransportConnector();
                conn.setSerializerProvider(serializationProvider);
                TransportParameter param = getTransportParameter();
                // large payload problem, reconfigure?
                param = TransportParameter.TransportParameterBuilder.newBuilder(param)
                    .setMqttQoS(MqttQoS.AT_MOST_ONCE)
                    .setActionTimeout(5000).build();
                conn.connect(param);
                LoggerFactory.getLogger(getClass()).info("MQTT-Out connector created");
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).info("Cannot create MQTT-Out connector: {}", e.getMessage());
                conn = null;
            }
        }
        return conn; 
    }
    
    @Override
    protected BasicSerializerProvider getConfiguredSerializationProvider() {
        return new BasicSerializerProviderWithJsonDefault() {

            @Override
            protected <T> Serializer<T> createDefault(Class<T> type) {
                GenericJsonSerializer<T> r = new GenericJsonSerializer<>(type);
                if (TraceRecord.class == type) {
                    JsonUtils.exceptFields(r.getMapper(), EXCLUDED_FIELDS);
                }
                return r;
            }

        };
    }

    @Override
    public void processCommand(Command data) {
        Commands cmd = Commands.valueOfSafe(data.getCommand());
        handleCommand(cmd, data.getStringParam(), CobotCommands.PLC_ROBOT_GRIP_CAR);
    }
    
    /**
     * Returns whether the drive is ready for operation.
     * 
     * @return {@code true} for ready, {@code false} else
     */
    private boolean checkDriveReady() {
        boolean result = false;
        lastDriveError = null;
        if (null != lastDriveOut) {
            if (lastDriveOut.getError()) {
                lastDriveError = "Cannot operate drive: Drive error " + lastDriveOut.getErrorID() + ", " 
                    + lastDriveOut.getSAxisMessage();
            } else if (lastDriveOut.getBWaitForCommand()) {
                result = true;
                System.out.println("Drive ok.");
            } else {
                lastDriveError = "Cannot operate drive: Drive either active or busy, " 
                    + lastDriveOut.getSAxisMessage();
            }
        } else {
            lastDriveError = "Cannot start id scan. No drive state. Drive switched on?";
        }
        if (null != lastDriveError) {
            System.out.println(lastDriveError);
        }
        return result;
    }
    
    /**
     * Handles a command, represents the state machine transitions.
     * 
     * @param cmd the command
     * @param param the parameter for the command, may be <b>null</b> or empty for none
     * @param idCobotNext for id scan, which PLC cobot command (index) to use
     */
    protected void handleCommand(Commands cmd, String param, int idCobotNext) {
        System.out.println("DriveAppAas: Received command " + cmd + " Current state: " + currentState);
        sendTransportAsync("iip/Drive-app/commands", Commands.createCommand(cmd, param));
        switch (cmd) {
        case REQUEST_QUIT:
            if (currentState == State.WAIT_FOR_ROBOT_BUSY 
                || currentState  == State.WAIT_FOR_ROBOT_DONE) {
                sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_QUIT);
                setCurrentState(State.STOP);
            } else {
                System.out.println("Action Decider: Cannot send quit to robot. Process is not running. Ignored.");
            }
            break;
        case REQUEST_START_ID_SCAN:
            boolean readyForRequest = cobotCommands.readyForRequest();
            if (currentState == State.STOP && readyForRequest) {
                nextWaitState = State.ID_PRE_SCAN;
                sendCobotCommand(idCobotNext);
                // next steps in PLC reception
            } else {
                System.out.println("Action Decider: Cannot start process. Current state: " 
                    + currentState + ", robot id: " + getParameterRobotId() + " readyForRequest: " 
                    + readyForRequest + ". Ignored.");
            }
            break;
        case RESPONSE_DRIVE_ACK:
            if (currentState == State.WAIT_FOR_APP_ACK) {
                sendDriveCommand(lastFwdDriveRequest, State.WAIT_FOR_DRIVE_FWD_STARTED);
                // next steps in beckhoff reception, PLC reception
            } else {
                System.out.println("Action Decider: Cannot handle drive ACK. Current state: " 
                    + currentState + ", robot id: " + getParameterRobotId() + ". Ignored.");
            }
            break;
        case RESPONSE_DRIVE_NACK:
            if (currentState == State.WAIT_FOR_APP_ACK) {
                System.out.println("QA robot not free, cannot operate drive. Stopping");
                nextWaitState = State.STOP;
                sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_BASE);
            } else {
                System.out.println("Action Decider: Cannot handle drive NACK. Current state: " 
                    + currentState + ", robot id: " + getParameterRobotId() + ". Ignored.");
            }
            break;
        default:
            break;
        }
        if (System.currentTimeMillis() - lastStateChange > OVERALL_TIMEOUT) {
            System.out.println("DECIDER TIMEOUT: Reset to Stop");
            setCurrentState(State.STOP);
        }
    }
    
    /**
     * Sends a drive command.
     * 
     * @param request the 
     * @param nextState the state the current state shall be switched to
     */
    private void sendDriveCommand(DriveRequest request, State nextState) {
        if (checkDriveReady()) {
            System.out.println("Send drive command " + request);
            
            // if we are somehwere in the middle, shall be done via states, quick fix
            if (DRIVE_END_MM == request.position && (lastDriveOut.getRPosition_actual() - DRIVE_START_MM) > 2) {
                sendDriveCommand(bwdDriveRequest, currentState);
                long start = System.currentTimeMillis();
                while ((lastDriveOut.getRPosition_actual() - DRIVE_START_MM) > 2 
                    && System.currentTimeMillis() - start < 10000) {
                    TimeUtils.sleep(300);
                }
            }
            driveCommandIngestors.ingest(request.toCommand(false));
            TimeUtils.sleep(500);
            driveCommandIngestors.ingest(request.toCommand(true));
            setCurrentState(nextState);
        } else {
            setCurrentState(State.STOP);
            commandIngestors.ingest(Commands.createCommand(Commands.DRIVE_INTERRUPTED, null)); // other app, free
        }
    }
    
    /**
     * Sends a cobot command and goes to {@link State#WAIT_FOR_ROBOT_BUSY}.
     * 
     * @param idCobotNext the next PLC index
     */
    private void sendCobotCommand(int idCobotNext) {
        System.out.println("SEND COMMAND " + idCobotNext);
        cobotCommands.sendCommand(getParameterRobotId(), idCobotNext);
        setCurrentState(State.WAIT_FOR_ROBOT_BUSY);
    }

    @Override
    public void attachDriveCommandIngestor(DataIngestor<DriveCommand> ingestor) {
        driveCommandIngestors.attachIngestor(ingestor);
        LoggerFactory.getLogger(getClass()).info("Drive command ingestor attached.");
    }
    

    @Override
    public void attachCommandIngestor(DataIngestor<Command> ingestor) {
        commandIngestors.attachIngestor(ingestor);
        LoggerFactory.getLogger(getClass()).info("Command ingestor attached.");
    }
    
    @Override
    protected void augmentCommandsSubmodel(SubmodelBuilder smBuilder) {
        super.augmentCommandsSubmodel(smBuilder);
        ServerAddress vabServer = new ServerAddress(Schema.HTTP); // ephemeral, localhost
        AasFactory factory = AasFactory.getInstance();
        BasicSetupSpec spec = new BasicSetupSpec(AasFactory.DEFAULT_PROTOCOL, 
            vabServer.getHost(), vabServer.getPort());
        InvocablesCreator ic = factory.createInvocablesCreator(spec);
        smBuilder.createOperationBuilder(OP_REQUEST_DRIVE)
            .addInputVariable("iFriction", Type.INTEGER)
            .addInputVariable("iTension", Type.INTEGER)
            .setInvocable(ic.createInvocable(OP_REQUEST_DRIVE))
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_GET_TENSION)
            .setInvocable(ic.createInvocable(OP_GET_TENSION))
            .build(Type.INTEGER);
        smBuilder.createOperationBuilder(OP_GET_FRICTION)
            .setInvocable(ic.createInvocable(OP_GET_FRICTION))
            .build(Type.INTEGER);
        smBuilder.createOperationBuilder(OP_GET_DRIVE_WAIT_FOR_COMMAND)
            .setInvocable(ic.createInvocable(OP_GET_DRIVE_WAIT_FOR_COMMAND))
            .build(Type.BOOLEAN);
        smBuilder.createOperationBuilder(OP_GET_DRIVE_POSITION)
            .setInvocable(ic.createInvocable(OP_GET_DRIVE_POSITION))
            .build(Type.DOUBLE);
        smBuilder.createPropertyBuilder(PROP_FRICTION)
            .setValue(Type.INTEGER, -1)
            .build();
        smBuilder.createPropertyBuilder(PROP_TENSION)
            .setValue(Type.INTEGER, -1)
            .build();
        smBuilder.createPropertyBuilder(PROP_DRIVE_POSITION)
            .setValue(Type.DOUBLE, false)
            .build();
        smBuilder.createPropertyBuilder(PROP_DRIVE_WAIT_FOR_COMMAND)
            .setValue(Type.BOOLEAN, false)
            .build();
        
        ProtocolServerBuilder psb = factory.createProtocolServerBuilder(spec);
        psb.defineOperation(OP_REQUEST_DRIVE, 
            p -> requestDriveStart(AasUtils.readInt(p, 0, 0), AasUtils.readInt(p, 1, 0), 100, 1));
        psb.defineOperation(OP_GET_TENSION, 
            p -> null == lastDriveOut ? -1 : lastDriveOut.getITension_actual());
        psb.defineOperation(OP_GET_FRICTION, 
            p -> null == lastDriveOut ? -1 : lastDriveOut.getIFriction_actual());
        psb.defineOperation(OP_GET_DRIVE_POSITION, 
            p -> null == lastDriveOut ? false : lastDriveOut.getRPosition_actual());
        psb.defineOperation(OP_GET_DRIVE_WAIT_FOR_COMMAND, 
            p -> null == lastDriveOut ? false : lastDriveOut.getBWaitForCommand());

        opServer = psb.build();
        opServer.start();
    }

    @Override
    protected String getAasTransportChannel() {
        return "iip/Drive-app/traces";
    }

    @Override
    protected ServiceState start() throws ExecutionException {
        intercom.start();
        return super.start();
    }

    @Override
    protected ServiceState stop() {
        intercom.stop();
        if (null != opServer) {
            opServer.stop(false);
        }
        return super.stop();
    }
    
    /**
     * Changes the current state and records the last change timestamp.
     * 
     * @param state the state
     */
    private void setCurrentState(State state) {
        this.currentState = state;
        this.lastStateChange = System.currentTimeMillis();
    }

    @Override
    protected DataRecorder createDataRecorder() {
        return null; // by default no recording
    }

    @Override
    public void processPlcOutput(PlcOutput data) {
        cobotCommands.setLastPlcOutput(data);
        if (!data.getSafetyOk()) { // safety stop, stop program, don't handle further requests
            System.out.println("Safety stop!");
            Transport.sendTraceRecord(new TraceRecord(getId(), TraceRecord.ACTION_SENDING, 
                Commands.createCommand(Commands.NOTIFY_COBOT_SAFETY, String.valueOf(getParameterRobotId()))));
            setCurrentState(State.STOP);
        } else if (data.getHW_Btn1()) { // start cobot 1 by hardware button
            if (State.STOP == currentState) {
                lastFwdDriveRequest = fwdDriveRequest; // normal
                if (driveRequestCounter >= 0) {
                    if (0 == driveRequestCounter) {
                        lastFwdDriveRequest = fwdDriveRequest; // normal
                    } else if (1 == driveRequestCounter) {
                        lastFwdDriveRequest = fwdDriveRequest.copy().setObstacle(0, 140); 
                    } else if (2 == driveRequestCounter) {
                        lastFwdDriveRequest = fwdDriveRequest.copy().setObstacle(0, 60); 
                    } 
                    driveRequestCounter = (driveRequestCounter + 1) % 3;
                }
                lastBwdDriveRequest = bwdDriveRequest.copy()
                    .setObstacle(lastFwdDriveRequest.friction, lastFwdDriveRequest.tension);
                handleCommand(Commands.REQUEST_START_ID_SCAN, null, CobotCommands.PLC_ROBOT_GRIP_CAR);
            }
        } else if (data.getHW_Btn0()) {
            if (State.STOP == currentState) { // don't do that in normal operations
                // after emergency stop: if not in initial place, drive backward
                sendDriveCommand(bwdDriveRequest, currentState);
            }
        }
        if (State.WAIT_FOR_ROBOT_BUSY == currentState) {
            if (data.getUR_BusyOperating()) { 
                setCurrentState(State.WAIT_FOR_ROBOT_DONE);
            }            
        } else if (State.WAIT_FOR_ROBOT_DONE == currentState) {
            if (!data.getUR_BusyOperating()) { 
                if (System.currentTimeMillis() - lastStateChange > CobotCommands.TIMEDIFF_ROBOT_BUSY_SAFE) {
                    System.out.println("TIMEOUT waiting for cobot (" + CobotCommands.TIMEDIFF_ROBOT_BUSY_SAFE + ")");
                    setCurrentState(State.STOP);
                } else {
                    System.out.println("Next Wait State: " + nextWaitState);
                    setCurrentState(nextWaitState); // usually WAITING -> RUNNING, different for drive
                    if (State.WAIT_FOR_DRIVE_BWD == nextWaitState) {
                        sendDriveCommand(lastBwdDriveRequest, State.WAIT_FOR_DRIVE_BWD_STARTED);
                    } else if (State.ID_PRE_SCAN == nextWaitState) {
                        nextWaitState = State.ID_SCAN;
                        // easier to adjust of not synched with ANY_NEXT on PLC
                        timer.schedule(new MipTimerTask(true, mipSensorId, mipInputIngestors), 3500L);
                        timer.schedule(new MipTimerTask(false, mipSensorId, mipInputIngestors), 3500L + 5000L);
                        //sendMipCommand(true); -> timer
                        sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_ANY);
                    } else if (State.ID_SCAN == nextWaitState) {
                        //sendMipCommand(false); // sets nextWaitState, wait for MIP data -> timer
                        mipEnd();
                    } else if (State.ID_DRIVE == nextWaitState) {
                        requestDriveAllocation();
                    } else if (State.PLACE_CAR == nextWaitState) {
                        nextWaitState = State.WAIT_FOR_DRIVE_BWD;
                        sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_BWD_DRIVE);
                    }
                }
            }
        }
    }
    
    /**
     * Requests the allocation of the drive, i.e., notifies the other cobot that no quality scanning shall happen.
     * May fail, see RESPONSE_DRIVE_NACK.
     */
    private void requestDriveAllocation() {
        System.out.println("REQUESTING DRIVE ALLOC (mock: " + MOCK_DRIVE_REQUEST + ")");
        if (MOCK_DRIVE_REQUEST) {
            setCurrentState(State.WAIT_FOR_APP_ACK);
            handleCommand(Commands.RESPONSE_DRIVE_ACK, null, getParameterRobotId());
        } else {
            if (checkDriveReady() && cobotCommands.isInSafePosition()) {
                if (CobotCommands.sendIntercom(intercom, Commands.createCommand(Commands.REQUEST_DRIVE, null))) {
                    setCurrentState(State.WAIT_FOR_APP_ACK);
                } else {
                    sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_BASE);
                    setCurrentState(State.STOP);
                }
            } else {
                System.out.println("Cannot request drive as either drive is not ready or cobot "
                    + "is not in safe position.");
                sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_BASE);
                setCurrentState(State.STOP);
            }
        }
    }

    /**
     * Requests a drive start (via AAS).
     * 
     * @param friction the simulated friction (in percent)
     * @param tension the simulated tension (in percent)
     * @param position the target position in mm
     * @param velocity the drive speed in mm/s
     * @return "ok" or the reason for not fulfilling the request
     */
    private String requestDriveStart(int friction, int tension, double position, double velocity) {
        lastBwdDriveRequest = new DriveRequest(friction, tension, position, velocity);
        handleCommand(Commands.REQUEST_START_ID_SCAN, null, CobotCommands.PLC_ROBOT_GRIP_CAR);
        return lastDriveError == null ? "ok" : lastDriveError;
    }

    @Override
    public void attachPlcInputIngestor(DataIngestor<PlcInput> ingestor) {
        plcInputIngestors.attachIngestor(ingestor);
    }
    
    /**
     * Returns the filter for handleNew.
     * 
     * @return the filter
     */
    protected Predicate<TraceRecord> getHandleNewFilter() {
        return data -> !TraceRecord.ACTION_RECEIVING.equals(data.getAction()) || data.getPayload() instanceof Command;
    }
    
    @Override
    protected ConverterInstances<TraceRecord> createConverter() {
        ConverterInstances<TraceRecord> result = super.createConverter();
        TransportConverter<TraceRecord> conv = result.getConverter();
        conv.setExcludedFields(CollectionUtils.addAll(new HashSet<>(), EXCLUDED_FIELDS));
        conv.setHandleNewFilter(getHandleNewFilter());
        return result;
    }
    
    /**
     * Returns whether the received data is ok.
     * 
     * @param data the data to check
     * @return {@code true} for ok, {@code false} else
     */
    private boolean isOk(MipAiPythonOutput data) {
        return true; // we assume for now: data.getAiid_tag() != null && data.getAiid_tag().length() > 0;
    }

    @Override
    public void processMipAiPythonOutput(MipAiPythonOutput data) {
        boolean queryCarAas = false;
        if (!BYPASS_MIP) {
            if (sensorFail > 2) {
                sensorFail = 0;
                nextWaitState = State.STOP; // no drive allocated so far
                sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_BASE);
            } else if (isOk(data)) {
                sensorFail = 0;
                nextWaitState = State.ID_DRIVE;
                sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_FWD_DRIVE);
                queryCarAas = true;
            } else if (!isOk(data)) {
                sensorFail++;
                handleCommand(Commands.REQUEST_START_ID_SCAN, null, CobotCommands.PLC_ROBOT_NEXT_GOTO_ID_SCAN_POS);
            }
        } else {
            queryCarAas = true;
        }
        if (queryCarAas) {
            String tag = data.getAimip_id_tag();
            if (tag != null && tag.length() == 0) {
                tag = data.getAiid_tag();
            }
            System.out.println("Requesting Car ID " + tag);
            CobotCommands.sendIntercom(intercom, Commands.createCarQueryCommand(Commands.DRIVE_QUERY_CAR_AAS, tag));  
        }
        sendTransportAsync("iip/Drive-app/mip", data);
    }

    @Override
    public void attachMipMqttInputIngestor(DataIngestor<MipMqttInput> ingestor) {
        mipInputIngestors.attachIngestor(ingestor);
    }
    
    /**
     * Things do be done at the (synced with PLC) end of MIP sensor reading.
     */
    private void mipEnd() {
        if (BYPASS_MIP) {
            nextWaitState = State.ID_DRIVE;
            sendCobotCommand(CobotCommands.PLC_ROBOT_NEXT_FWD_DRIVE);
        }
    }

}
