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

package de.iip_ecosphere.platform.examples;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * Spring Cloud Stream emulating startup code.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringStartup {
    
    public static final String ARG_BROKER_PORT = "iip.test.brokerPort";
    public static final int DFLT_BROKER_PORT = 8883;
    public static final String ARG_STOP = "iip.test.stop";

    /**
     * Main program to start the application.
     * 
     * @param args the command line arguments; the first is the artifact file to start, the remaining is passed on 
     *     to Spring
     */
    public static void main(String[] args) {
        start(args);
    }
    
    /**
     * Starts the application. Used from generated templates. Do not change signature.
     * 
     * @param args the command line arguments; the first is the artifact file to start, the remaining is passed on 
     *     to Spring
     */
    public static final void start(String... args) {
        if (args.length > 0) {
            File f = new File(args[0]);
            String[] restArgs = new String[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                restArgs[i - 1] = args[i];
            } 
            SpringStartup.start(f, restArgs);
        }
    }
    
    /**
     * Starts the application. Used from examples. Do not change signature.
     * 
     * @param artifact the artifact file (JAR/ZIP) containing the application
     * @param args the command line arguments
     */
    public static void start(File artifact, String... args) {
        start(artifact, true, null, args);
    }
    
    /**
     * Starts the application.
     * 
     * @param artifact the artifact file (JAR/ZIP) containing the application
     * @param doExit whether at the end of the timing if a timeout is given by {@code args} the JVM shall be shut down
     * @param procCfg a configurer for the process being generated, may be <b>null</b> for none
     * @param args the command line arguments
     */
    public static void start(File artifact, boolean doExit, Consumer<ProcessBuilder> procCfg, String... args) {
        String brokerHost = "localHost";
        int adminPort = -1; // ephemeral
        String serviceProtocol = "";
        
        int brokerPort = CmdLine.getIntArg(args, ARG_BROKER_PORT, DFLT_BROKER_PORT);
        int stop = CmdLine.getIntArg(args, ARG_STOP, 0);
        try {
            List<String> cmdLine = DescriptorUtils.createStandaloneCommandArgs(artifact, brokerPort, 
                brokerHost, adminPort, serviceProtocol);
            LogManager.getLogger(SpringStartup.class).info("Starting with arguments: " + cmdLine);
            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            if (null != procCfg) {
                procCfg.accept(builder);
            } else {
                builder.inheritIO();
            }
            Process proc = builder.start();
            if (stop > 0) {
                LogManager.getLogger(SpringStartup.class).info("Scheduling for auto-stop after " + stop + " ms");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    
                    @Override
                    public void run() {
                        LogManager.getLogger(SpringStartup.class).info("Auto-stop after: " + stop + " ms");
                        proc.destroyForcibly();
                        timer.cancel();
                        if (doExit) {
                            System.exit(0);
                        }
                    }
                }, stop);
            }
            proc.waitFor();
        } catch (ExecutionException | InterruptedException | IOException e) {
            LogManager.getLogger(SpringStartup.class).error("Running the app: " + e.getMessage());
        }
    }

}
