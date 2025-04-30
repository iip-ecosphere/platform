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

package de.iip_ecosphere.platform.examples.hm22;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TraceToAasService;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter.ConverterInstances;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
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
    private static final boolean DO_CLEANUP = Boolean.valueOf(System.getProperty("iip.app.hm22.aasCleanup", "true"));

    private static final String OP_START = "startProcess";
    private static final String OP_SWITCH_AI = "switchAi";
    private static final String OP_QUIT_ROBOT = "quitRobot";
    private static final String OP_FEEDBACK = "feedbackToAi";
    private List<DataIngestor<Command>> commandIngestors = new ArrayList<>();
    private Server opServer;
    private int mdzhReceivedCount;

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
    public void processDecisionResult(DecisionResult data) {
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
        smBuilder.createOperationBuilder(OP_SWITCH_AI)
            .setInvocable(ic.createInvocable(OP_SWITCH_AI))
            .build();
        smBuilder.createOperationBuilder(OP_QUIT_ROBOT)
            .setInvocable(ic.createInvocable(OP_QUIT_ROBOT))
            .build();
        smBuilder.createOperationBuilder(OP_FEEDBACK)
            .addInputVariable("ok", Type.BOOLEAN)
            .setInvocable(ic.createInvocable(OP_FEEDBACK))
            .build();
        
        ProtocolServerBuilder psb = factory.createProtocolServerBuilder(spec);
        psb.defineOperation(OP_START, 
            p -> sendCommand(Commands.REQUEST_START, AasUtils.readString(p, 0)));
        psb.defineOperation(OP_SWITCH_AI, 
            p -> sendCommand(Commands.REQUEST_SWITCH_AI, null));
        psb.defineOperation(OP_QUIT_ROBOT, 
            p -> sendCommand(Commands.REQUEST_QUIT, null));
        psb.defineOperation(OP_FEEDBACK, 
             // turn Boolean to String
            p -> sendCommand(Commands.SEND_FEEDBACK_TO_AI, AasUtils.readString(p, 0)));
        opServer = psb.build();
        opServer.start();
    }
    

    @Override
    protected ServiceState stop() {
        if (null != opServer) {
            opServer.stop(false);
        }
        return super.stop();
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
     * Refined converter.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ConfiguredConverter extends Converter {

        @Override
        protected void handleNew(TraceRecord data) {
            boolean send = true;
            if ((data.getPayload() instanceof Command)) { // filter out commands
                Command cmd = ((Command) data.getPayload());
                if (!cmd.getCommand().equals(Commands.SOURCE_DO_QR_SCAN.name())) {
                    send = false;
                }
            }
            if ((data.getPayload() instanceof PlcOutput)) { // filter out commands
                send = false;
            }
            if ((data.getPayload() instanceof MdzhOutput)) { 
                if (mdzhReceivedCount > 30) {
                    send = false;
                }
                mdzhReceivedCount++;
                if (mdzhReceivedCount < 0) { // overflow
                    mdzhReceivedCount = 0;
                }
            }
            if (send) {
                super.handleNew(data);
            }
        }

        @Override
        public boolean cleanup(Aas aas) {
            boolean done = false;
            if (DO_CLEANUP) {
                done = super.cleanup(aas);
            }
            return done;
        }

    }

    @Override
    protected ConverterInstances<TraceRecord> createConverter() {
        return new ConverterInstances<TraceRecord>(new ConfiguredConverter());
    }

}
