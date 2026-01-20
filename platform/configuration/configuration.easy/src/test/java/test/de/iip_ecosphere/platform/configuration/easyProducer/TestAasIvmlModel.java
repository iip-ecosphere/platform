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

package test.de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.cfg.AasChange;
import de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactory;
import de.iip_ecosphere.platform.configuration.easyProducer.AasChanges;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.easyProducer.DrawflowGraphFormat;
import de.iip_ecosphere.platform.configuration.easyProducer.EasySetup;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor.ExecutionMode;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.GraphFormat;
import de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh.ServiceMeshGraphMapper;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.OperationCompletedListener;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

/**
 * Tests an AAS IVML model mapping.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestAasIvmlModel {
    
    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_CONFIGURATION; 
    
    /**
     * Executes the program.
     * 
     * @param args name of the configuration mode, folder containing the configuration, optional names of mesh 
     *     variables to be emitted 
     */
    public static void main(String[] args) {
        if (args.length < 2) { 
            System.out.println("CfgModelName cfgFolder [meshVariables*]");
        } else {
            TestOperation op = args.length > 2 ? new ListGraphs() : new CreateVar();
            TestWithPlugin.setAasPluginId("aas.basyx-1.3");
            TestWithPlugin.setupAASPlugins();
            TestWithPlugin.loadPlugins();
            EasySetup ep = ConfigurationSetup.getSetup(false).getEasyProducer();
            File modelFolder = new File("src/main/easy");
            File cfgFolder = new File(args[1]);
            ep.setBase(modelFolder);
            ep.setIvmlMetaModelFolder(modelFolder);
            ep.setIvmlConfigFolder(cfgFolder);
            ep.setIvmlModelName(args[0]);
            File commonFolder = new File(cfgFolder.getParent(), "common");
            if (commonFolder.exists()) { // config.config test setup
                ep.setAdditionalIvmlFolders(CollectionUtils.toList(commonFolder));
            }
            ConfigurationLifecycleDescriptor lc = new ConfigurationLifecycleDescriptor();
            lc.startup(ExecutionMode.IVML, new String[0]); // shall register executor
            ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
            if (null  == rRes) {
                System.out.println("No model loaded");
                System.exit(1);
            }

            Configuration cfg = ConfigurationManager.getVilConfiguration();
            GraphFormat format = new DrawflowGraphFormat();
            AasIvmlMapper mapper = new AasIvmlMapper(() -> cfg, new ServiceMeshGraphMapper(), null); 
            mapper.addGraphFormat(format);
            ConfigurationManager.setAasIvmlMapper(mapper);        
            
            AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
            AasPartRegistry.setAasSetup(aasSetup);
            ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
            Endpoint regEndpoint = aasSetup.adaptEndpoint(aasSetup.getRegistryEndpoint());
            System.out.println("ServerHost " + aasSetup.getServerHost() + " " + regEndpoint.toUri());
            PersistenceType pType = LocalPersistenceType.INMEMORY;
            System.out.println("Starting " + pType + " AAS registry on " + regEndpoint.toUri());
            Server registryServer = rcp.createRegistryServer(aasSetup, pType);
            registryServer.start();
            Endpoint serverEndpoint = aasSetup.adaptEndpoint(aasSetup.getServerEndpoint());
            System.out.println("ServerHost " + aasSetup.getServerHost() + " " + serverEndpoint.toUri());
            System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
            Server aasServer = rcp.createAasServer(aasSetup, pType);
            aasServer.start();

            AasFactory aasFactory = AasFactory.getInstance();
            AasBuilder aasBuilder = aasFactory.createAasBuilder("Platform", AasPartRegistry.URN_AAS);
            SubmodelBuilder smb = AasPartRegistry.createSubmodelBuilder(aasBuilder, NAME_SUBMODEL);
            InvocablesCreator iCreator = aasFactory.createInvocablesCreator(aasSetup);
            ProtocolServerBuilder psb = aasFactory.createProtocolServerBuilder(aasSetup);

            AasChanges.INSTANCE.setup(smb, iCreator, createOperationCompletedListener());
            AasChanges.INSTANCE.bindOperations(psb);
            
            //mapper.mapByType(smb, iCreator);
            //mapper.bindOperations(psb);
            smb.build();
            Aas aas = aasBuilder.build();
            op.aasCreated(aas);
            Server implServer = psb.build().start();

            try {
                AasPartRegistry.remoteDeploy(CollectionUtils.toList(aas));
                System.out.println("EXECUTING " + op.getClass().getSimpleName());
                op.executeOperation(args, mapper, format);
            } catch (IOException | ExecutionException e) {
                e.printStackTrace();
            }
            
            implServer.stop(false);
            aasServer.stop(true);
            registryServer.stop(true);
            lc.shutdown();
        }
    }

    /**
     * Represents a test operation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private interface TestOperation {

        /**
         * Executes the represented operation.
         * 
         * @param args the command line args
         * @param mapper the AAS IVML mapper
         * @param format the Graph format
         * @throws IOException if the operation fails for AAS retrieval
         * @throws IOException if the operation fails for AAS operation execution reasons
         */
        public void executeOperation(String[] args, AasIvmlMapper mapper, GraphFormat format) 
            throws IOException, ExecutionException;
        
        /**
         * Called when the local AAS is created before deployment. By default, it prints the AAS.
         * 
         * @param aas the AAS
         */
        public default void aasCreated(Aas aas) {
            aas.accept(new AasPrintVisitor());
        }
        
    }
    
    /**
     * Implements a test operation that creates a variable in order to validate the change tracking.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class CreateVar implements TestOperation {

        @Override
        public void executeOperation(String[] args, AasIvmlMapper mapper, GraphFormat format) 
            throws IOException, ExecutionException {
            Aas aas = AasPartRegistry.retrieveIipAas();
            Submodel sm = aas.getSubmodel(NAME_SUBMODEL);
            Operation op = sm.getOperation(AasIvmlMapper.OP_CREATE_VARIABLE);
            op.invoke("javaService_myService", "JavaService", "{"
                + "artifact = \"myGroup:myServices:1.23\", class = \"MyService\", ver = \"1.23\", "
                + "asynchronous = true, kind = ServiceKind.TRANSFORMATION_SERVICE, name = \"myService\", "
                + "id = \"myService\", deployable = true, output = {IOType{forward=true,type=refBy(rec1)}}, "
                + "input = {IOType{forward=true,type=refBy(rec1)}}, traceSent = TraceKind.NONE, "
                + "traceRcv = TraceKind.NONE, monitorProcessingTime = true, monitorSentCount = true, "
                + "monitorRcvCount = true"
                + "}");
            store(aas, "target/testaas-after.txt");
        }

        @Override
        public void aasCreated(Aas aas) {
            store(aas, "target/testaas-before.txt");
        }

    }

    /**
     * Stores the {@code aas} to {@code file}.
     * 
     * @param aas the AAS
     * @param file the file to store to
     */
    private static void store(Aas aas, String file) {
        System.out.println("Writing to " + file);
        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            Submodel sm = aas.getSubmodel(NAME_SUBMODEL);
            AasPrintVisitor visitor = new AasPrintVisitor(out)
                .setSubmodelElementCollectionPredicate(c -> c.getIdShort().equals("ServiceBase"))
                .setSubmodelElementListPredicate(l -> l.getIdShort().equals("ServiceBase"));
            sm.accept(visitor);
        } catch (IOException e) {
            System.out.println("Cannot write " + file + ": " + e.getMessage());
        }
    }

    /**
     * Implements a test operation that creates a variable in order to list the configured graphs.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ListGraphs implements TestOperation {

        @Override
        public void executeOperation(String[] args, AasIvmlMapper mapper, GraphFormat format) 
            throws IOException, ExecutionException {
            for (int i = 2; i < args.length; i++) {
                try {
                    System.out.println("Graph " + args[i] + ":");
                    System.out.println(mapper.getGraph(args[i], format.getName()));
                } catch (ExecutionException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        }
        
    }
    
    /**
     * Creates an operation completed listener.
     * 
     * @return the operation completed listener
     */
    private static OperationCompletedListener createOperationCompletedListener() {
        return new OperationCompletedListener() {

            @Override
            public void operationCompleted() {
                try {
                    Aas aas = AasPartRegistry.retrieveIipAas();
                    Submodel sm = aas.getSubmodel(NAME_SUBMODEL);
                    SubmodelBuilder smB = AasPartRegistry.createSubmodelBuilderRbac(aas, NAME_SUBMODEL);
                    for (AasChange c : ConfigurationFactory.getAasChanges().getAndClearAasChanges()) {
                        c.apply(sm, smB);
                    }
                    smB.build();
                } catch (IOException e) {
                    LoggerFactory.getLogger(TestAasIvmlModel.class).error(
                        "While modifying configuration AAS: {}", e.getMessage());
                }
            }

            @Override
            public void operationFailed() {
                ConfigurationFactory.getAasChanges().clearAasChanges();
            }

        };
    }

}
