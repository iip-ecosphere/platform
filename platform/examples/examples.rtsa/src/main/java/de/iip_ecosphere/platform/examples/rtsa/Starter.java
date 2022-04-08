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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.LogManager;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

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
        
        int stop = CmdLine.getIntArg(args, "iip.test.stop", 0);
        File f = new File("gen/rtsa/SimpleRTSADemoFlowApp/target/SimpleRTSADemoFlowApp-0.1.0-SNAPSHOT-bin.jar");
        try {
            List<String> cmdLine = DescriptorUtils.createStandaloneCommandArgs(f, brokerPort, brokerHost, adminPort, 
                serviceProtocol);
            LogManager.getLogger(Starter.class).info("Starting with arguments: " + cmdLine);
            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            Process proc = builder.inheritIO().start();
            if (stop > 0) {
                LogManager.getLogger(Starter.class).info("Scheduling for auto-stop after " + stop + " ms");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    
                    @Override
                    public void run() {
                        LogManager.getLogger(Starter.class).info("Auto-stop after: " + stop + " ms");
                        proc.destroyForcibly();
                        timer.cancel();
                        System.exit(0);
                    }
                }, stop);
            }    
            proc.waitFor();
        } catch (ExecutionException | InterruptedException | IOException e) {
            LogManager.getLogger(Starter.class).error("Running the app: " + e.getMessage());
        }
    }

}
