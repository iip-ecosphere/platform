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

package test.de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.ConfigurationAas;
import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.DrawflowGraphFormat;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.ivml.GraphFormat;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;

/**
 * Tests an AAS IVML model mapping.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestAasIvmlModel {
    
    /**
     * Executes the program.
     * 
     * @param args name of the configuration mode, folder contaning the configuration, optional names of mesh 
     *     variables to be emitted 
     */
    public static void main(String[] args) {
        if (args.length < 2) { 
            System.out.println("CfgModelName cfgFolder [meshVariables*]");
        } else {
            EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
            File modelFolder = new File("src/main/easy");
            File cfgFolder = new File(args[1]);
            ep.setBase(modelFolder);
            ep.setIvmlMetaModelFolder(modelFolder);
            ep.setIvmlConfigFolder(cfgFolder);
            ep.setIvmlModelName(args[0]);

            ConfigurationLifecycleDescriptor lc = new ConfigurationLifecycleDescriptor();
            lc.startup(new String[0]); // shall register executor
            ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
            if (null  == rRes) {
                System.out.println("No model loaded");
                System.exit(1);
            }

            Configuration cfg = ConfigurationManager.getVilConfiguration();
            GraphFormat format = new DrawflowGraphFormat();
            AasIvmlMapper mapper = new AasIvmlMapper(() -> cfg, new ConfigurationAas.IipGraphMapper(), null); 
            mapper.addGraphFormat(format);
            ConfigurationManager.setAasIvmlMapper(mapper);        
            
            AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
            AasPartRegistry.setAasSetup(aasSetup);
            ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
            Endpoint regEndpoint = aasSetup.adaptEndpoint(aasSetup.getRegistryEndpoint());
            System.out.println("ServerHost " + aasSetup.getServerHost() + " " + regEndpoint.toUri());
            PersistenceType pType = LocalPersistenceType.INMEMORY;
            String fullRegUri = AasFactory.getInstance().getFullRegistryUri(regEndpoint);
            System.out.println("Starting " + pType + " AAS registry on " + fullRegUri);
            Server registryServer = rcp.createRegistryServer(regEndpoint, pType);
            registryServer.start();
            Endpoint serverEndpoint = aasSetup.adaptEndpoint(aasSetup.getServerEndpoint());
            System.out.println("ServerHost " + aasSetup.getServerHost() + " " + serverEndpoint.toUri());
            System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
            Server aasServer = rcp.createAasServer(aasSetup.getServerEndpoint(), pType, regEndpoint);
            aasServer.start();

            AasFactory aasFactory = AasFactory.getInstance();
            AasBuilder aasBuilder = aasFactory.createAasBuilder("Platform", null);
            SubmodelBuilder smb = aasBuilder.createSubmodelBuilder(ConfigurationAas.NAME_SUBMODEL, null);
            InvocablesCreator iCreator = aasFactory.createInvocablesCreator(AasFactory.LOCAL_PROTOCOL, "localhost", 0);
            ProtocolServerBuilder psb = aasFactory.createProtocolServerBuilder(AasFactory.LOCAL_PROTOCOL, 0);
            mapper.mapByType(smb, iCreator);
            mapper.bindOperations(psb);
            smb.build();
            Aas aas = aasBuilder.build();
            aas.accept(new AasPrintVisitor());
            psb.build();

            try {
                AasPartRegistry.remoteDeploy(CollectionUtils.toList(aas)); 
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 2; i < args.length; i++) {
                try {
                    System.out.println("Graph " + args[i] + ":");
                    System.out.println(mapper.getGraph(args[i], format.getName()));
                } catch (ExecutionException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
            
            aasServer.stop(true);
            registryServer.stop(true);
            lc.shutdown();
        }
    }

}
