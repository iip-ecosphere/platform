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

package de.iip_ecosphere.platform.examples.python;

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
        
        File f = new File("gen/py/SimplePythonDemoFlowApp/target/SimplePythonDemoFlowApp-0.1.0-SNAPSHOT-bin.jar");
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
