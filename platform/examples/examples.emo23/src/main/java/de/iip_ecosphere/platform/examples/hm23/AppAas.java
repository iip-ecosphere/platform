/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm23;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
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
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProvider;
import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProviderWithJsonDefault;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import iip.datatypes.BeckhoffOutput;
import iip.datatypes.Command;
import iip.datatypes.DecisionResult;
import iip.datatypes.MdzhOutput;
import iip.datatypes.PlcOutput;

/**
 * AAS trace based sink.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AppAas extends TraceToAasService implements iip.interfaces.AppAasInterface {

    // default: no cleanup due to BaSyx exceptions
    private static final boolean DO_CLEANUP = Boolean.valueOf(System.getProperty("iip.app.hm23.aasCleanup", "true"));

    private static final String OP_START = "startProcess";
    private static final String OP_QUIT_ROBOT = "quitRobot";
    private static final String OP_FEEDBACK = "feedbackToAi";
    private static final String OP_MODEL_CHANGE = "changeAiModel";
    private static final String OP_REQUEST_CAR_AAS = "requestCarAas";
    private List<DataIngestor<Command>> commandIngestors = new ArrayList<>();
    private Server opServer;
    private int mdzhReceivedCount;
    private int robotId = 1;
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public AppAas(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates a service instance. [for testing]
     *
     * @param app static information about the application
     * @param yaml the service description 
     */
    public AppAas(ApplicationSetup app, YamlService yaml) {
        super(app, yaml);
    }

    @Override
    protected boolean isAasEnabled() {
        return false; // also disables WS
    }
    
    @Override
    protected void registerParameterConfigurers() {
     // parameter must be declared in this form in model!
        addParameterConfigurer(new ParameterConfigurer<>("robotId", Integer.class, TypeTranslators.INTEGER, 
            t -> robotId = t).withSystemProperty("iip.app.hm23.robotId"));
    }

    @Override
    public void processDecisionResult(DecisionResult data) {
        sendTransportAsync("iip/FL-app" + robotId + "/results", data);
    }
    
    @Override
    protected String getAasTransportChannel() {
        return "iip/FL-app" + robotId + "/traces";
    }

    /**
     * Returns the AAS idShort of the AAS represented by this service/application.
     * 
     * @return the idShort
     */
    public String getAasId() {
        return super.getAasId() + String.valueOf(robotId);
    }

    /**
     * Returns the AAS URN of the AAS represented by this service/application.
     * 
     * @return the URN
     */
    public String getAasUrn() {
        return  "urn:::AAS:::" + getAasId() + robotId + "#";
    }

    @Override
    protected void augmentCommandsSubmodel(SubmodelBuilder smBuilder) {
        super.augmentCommandsSubmodel(smBuilder);
        ServerAddress vabServer = new ServerAddress(Schema.HTTP); // ephemeral, localhost
        AasFactory factory = AasFactory.getInstance();
        BasicSetupSpec spec = new BasicSetupSpec(AasFactory.DEFAULT_PROTOCOL, 
                vabServer.getHost(), vabServer.getPort());
        InvocablesCreator ic = factory.createInvocablesCreator(spec);
        smBuilder.createOperationBuilder(OP_START)
            .addInputVariable("id", Type.STRING)
            .setInvocable(ic.createInvocable(OP_START))
            .build();
        smBuilder.createOperationBuilder(OP_QUIT_ROBOT)
            .setInvocable(ic.createInvocable(OP_QUIT_ROBOT))
            .build();
        smBuilder.createOperationBuilder(OP_FEEDBACK)
            .addInputVariable("feedback", Type.STRING)
            .setInvocable(ic.createInvocable(OP_FEEDBACK))
            .build();
        smBuilder.createOperationBuilder(OP_MODEL_CHANGE)
            .addInputVariable("modelId", Type.STRING)
            .setInvocable(ic.createInvocable(OP_MODEL_CHANGE))
            .build();
        smBuilder.createOperationBuilder(OP_REQUEST_CAR_AAS)
            .addInputVariable("productId", Type.STRING)
            .setInvocable(ic.createInvocable(OP_REQUEST_CAR_AAS))
            .build();
        
        ProtocolServerBuilder psb = factory.createProtocolServerBuilder(spec);
        psb.defineOperation(OP_START, 
            p -> sendCommand(Commands.REQUEST_START_QUALITY_DETECTION, AasUtils.readString(p, 0)));
        psb.defineOperation(OP_QUIT_ROBOT, 
            p -> sendCommand(Commands.REQUEST_QUIT, null));
        psb.defineOperation(OP_FEEDBACK, 
            p -> sendCommand(Commands.SEND_FEEDBACK_TO_AI, AasUtils.readString(p, 0)));
        psb.defineOperation(OP_MODEL_CHANGE, 
            p -> sendCommand(Commands.SEND_MODEL_CHANGE_TO_AI, AasUtils.readString(p, 0)));
        psb.defineOperation(OP_REQUEST_CAR_AAS, 
            p -> sendCommand(Commands.QUERY_CAR_AAS, AasUtils.readString(p, 0)));

        opServer = psb.build();
        opServer.start();
    }

    @Override
    protected BasicSerializerProvider getConfiguredSerializationProvider() {
        return new BasicSerializerProviderWithJsonDefault();
    }

    @Override
    protected TransportConnector createTransport(BasicSerializerProvider serializationProvider) {
        PahoMqttV3TransportConnector conn = null;
        if (null != getTransportParameter()) {
            try {
                conn = new PahoMqttV3TransportConnector();
                conn.setSerializerProvider(serializationProvider);
                conn.connect(getTransportParameter());
                LoggerFactory.getLogger(getClass()).info("MQTT-Out connector created");
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).info("Cannot create MQTT-Out connector: {}", e.getMessage());
                conn = null;
            }
        }
        return conn; 
    }

    @Override
    protected ServiceState stop() {
        if (null != opServer) {
            opServer.stop(false);
        }
        return super.stop();
    }
    
    @Override
    public void processCommand(Command data) {
        sendTransportAsync("iip/FL-app" + robotId + "/commands", data);
    }
    
    /**
     * Sends a command through the asynchronous ingestor. Logs an error if there is no ingestor.
     * 
     * @param cmd the command
     * @param param value of the string param, may be <b>null</b>
     * @return <b>null</b> for the AAS invocables
     */
    protected Object sendCommand(Commands cmd, String param) {
        System.out.println("SENDING COMMAND " + cmd);
        for (DataIngestor<Command> commandIngestor : commandIngestors) {
            commandIngestor.ingest(Commands.createCommand(cmd, param));
        }
        return null;
    }
    
    @Override
    public void attachCommandIngestor(DataIngestor<Command> ingestor) {
        this.commandIngestors.add(ingestor);
        LoggerFactory.getLogger(getClass()).info("Command data ingestor attached.");
    }
    
    /**
     * Returns the filter for new data.
     * 
     * @return the filter
     */
    protected Predicate<TraceRecord> getHandleNewFilter() {
        return data -> {
            boolean send = !TraceRecord.ACTION_RECEIVING.equals(data.getAction());
            Object payload = data.getPayload();
            if (payload instanceof Command) { // filter out commands
                Command cmd = ((Command) data.getPayload());
                if (!cmd.getCommand().equals(Commands.SOURCE_TAKE_PICTURE.name())) {
                    send = false;
                }
            } else if (payload instanceof PlcOutput || payload instanceof BeckhoffOutput) { // filter out PLC
                send = false;
            } else if (payload instanceof MdzhOutput) { 
                if (mdzhReceivedCount > 30) {
                    send = false;
                }
                mdzhReceivedCount++;
                if (mdzhReceivedCount < 0) { // overflow
                    mdzhReceivedCount = 0;
                }
            }
            return send;
        };
    }

    @Override
    protected ConverterInstances<TraceRecord> createConverter() {
        ConverterInstances<TraceRecord> result = super.createConverter(); 
        TransportConverter<TraceRecord> conv = result.getConverter();
        if (!DO_CLEANUP) {
            conv.setCleanupTimeout(-1);
        }
        conv.setExcludedFields(CollectionUtils.addAll(new HashSet<String>(), "image"));
        conv.setHandleNewFilter(getHandleNewFilter());
        return result;
    }
    
    @Override
    protected DataRecorder createDataRecorder() {
        return null; // by default no recording
    }
    
    @Override
    public void processMdzhOutput(MdzhOutput data) {
        sendTransportAsync("iip/FL-app" + robotId + "/carAas", data);
    }

}
