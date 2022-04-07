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

package de.iip_ecosphere.platform.examples.rtsa;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.LogManager;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils;

/**
 * Starts the application by emulating a bit platform functionality (Spring Cloud Stream service manager).
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    /**
     * Starts the application.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        String brokerHost = "localHost";
        int brokerPort = 8883;
        int adminPort = -1; // ephemeral
        String serviceProtocol = "";
        
        File f = new File("gen/rtsa/SimpleRTSADemoFlowApp/target/SimpleRTSADemoFlowApp-0.1.0-SNAPSHOT-bin.jar");
        /*if (!f.exists()) {
            LogManager.getLogger(Starter.class).error("Cannot find Spring service binary " + f.getAbsolutePath() 
                + ". Did you run the instantiation process?");
        }
        try {
            // This shall not occur in normal applications. Usually, we do not know what the service execution
            // is. Here we rely on spring, also because the descriptors are not yet abstracted. 
            YamlArtifact art = DescriptorUtils.readFromFile(f);
            List<String> cmdLine = new ArrayList<String>();
            cmdLine.add("java");
            cmdLine.add("-jar");
            cmdLine.add("-Dlog4j2.formatMsgNoLookups=true");
            cmdLine.add(f.getAbsolutePath());
            cmdLine.add("--iip.test.service.autostart=true"); // only for testing
            for (YamlService service : art.getServices()) {
                YamlProcess proc = service.getProcess();
                if (null != proc) {
                    File d = DescriptorUtils.extractProcessArtifacts(service.getId(), proc, f, null);
                    d.deleteOnExit();
                }
                for (Relation r : service.getRelations()) {
                    // simplification, don't think about relations
                    DescriptorUtils.addEndpointArgs(cmdLine, r.getEndpoint(), brokerPort, brokerHost);
                }
                cmdLine.addAll(service.getCmdArg(adminPort, serviceProtocol));
            }
            LogManager.getLogger(Starter.class).info("Starting with arguments: " + cmdLine);
            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            builder.inheritIO().start().waitFor();
        } catch (ExecutionException | InterruptedException | IOException e) {
            LogManager.getLogger(Starter.class).error("Running the app: " + e.getMessage());
        }*/

        try {
            List<String> cmdLine = DescriptorUtils.createStandaloneCommandArgs(f, brokerPort, brokerHost, adminPort, 
                serviceProtocol);
            LogManager.getLogger(Starter.class).info("Starting with arguments: " + cmdLine);
            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            builder.inheritIO().start().waitFor();
        } catch (ExecutionException | InterruptedException | IOException e) {
            LogManager.getLogger(Starter.class).error("Running the app: " + e.getMessage());
        }
    }

}
