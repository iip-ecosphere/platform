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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.services.spring.ServerManager;
import de.iip_ecosphere.platform.services.spring.SpringCloudArtifactDescriptor;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringInstances;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * Spring Cloud Stream emulating startup code. Considers system properties ({@value Starter#PROPERTY_JAVA8} 
 * and {@value #PROPERTY_ARGS}) as well as command line arguments {@value #ARG_BROKER_PORT} (broker port to use) 
 * and {@link #ARG_STOP} (auto-stop time in ms). In contrast to a startup of an application with the platform
 * and the service manager, services are started here in arbitrary sequence without considering their dependencies.
 * Testing input shall consider a certain startup time before causing actions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringStartup {
    
    public static final String PROPERTY_ARGS = "iip.springStart.args";
    public static final String ARG_BROKER_PORT = "iip.test.brokerPort";
    public static final int DFLT_BROKER_PORT = 8883;
    public static final String ARG_STOP = "iip.test.stop";

    /**
     * Main program to start the application. Takes into account additional args via system
     * property {@value #PROPERTY_ARGS} to allow for maven basic execution with fixed parameters in POM 
     * and additional arguments passed in via {@value #PROPERTY_ARGS}.
     * 
     * @param args the command line arguments; the first is the artifact file to start, the remaining is passed on 
     *     to Spring
     */
    public static void main(String[] args) {
        String sysArgs = System.getProperty(PROPERTY_ARGS, null);
        if (null != sysArgs) {
            List<String> tmp = CollectionUtils.toList(args);
            CollectionUtils.addAll(tmp, CmdLine.toArgs(sysArgs));
            args = tmp.toArray(new String[0]);
        }
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
            getLogger().info("Artifact args: {}", Arrays.toString(args));
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
     * Retrieves the AAS notification mode from cmd line argument {@link Starter#ARG_AAS_NOTIFICATION} and adds
     * it to {@code cmdArgs}.
     *  
     * @param args the command line arguments
     * @param cmdArgs the command line arguments to be modified as a side effect
     * @see Starter#setAasNotificationMode(String[], NotificationMode)
     */
    private static void addAasNotificationMode(String[] args, List<String> cmdArgs) {
        NotificationMode mode = Starter.setAasNotificationMode(args, NotificationMode.NONE);
        if (null != mode) {
            cmdArgs.add(CmdLine.composeArgument(Starter.ARG_AAS_NOTIFICATION, mode.name()));
        }
    }
    
    /**
     * If given in {@code args}, adds {@link Starter#PARAM_IIP_APP_ID} to {@code cmdArgs}.
     * 
     * @param args the command line arguments
     * @param cmdArgs the command line arguments to be modified as a side effect
     */
    private static void addAppId(String[] args, List<String> cmdArgs) {
        String appId = CmdLine.getArg(args, Starter.PARAM_IIP_APP_ID, "");
        if (appId.length() > 0) {
            cmdArgs.add(CmdLine.composeArgument(Starter.PARAM_IIP_APP_ID, appId));
        }
    }
    
    /**
     * Starts the application. Considers system property {@value Starter#PROPERTY_JAVA8} as java binary for Java 8 if 
     * not running under Java 8.
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

        SpringInstances.setConfig(new SpringCloudServiceSetup());
        Starter.considerInstalledDependencies(); // if there, transported by createStandalineCommandArgs
        int brokerPort = CmdLine.getIntArg(args, ARG_BROKER_PORT, DFLT_BROKER_PORT);
        int stop = CmdLine.getIntArg(args, ARG_STOP, 0);
        try {
            ServerManager serverMgr = new ServerManager(() -> NetworkManagerFactory.getInstance()); // local, no AAS
            final Collection<SpringCloudArtifactDescriptor> artDesc = getArtifacts(artifact);
            serverMgr.startServers(null, artDesc);
            getLogger().info("Command line for artifact: {}", artifact);
            List<String> cmdLine = DescriptorUtils.createStandaloneCommandArgs(artifact, brokerPort, 
                brokerHost, adminPort, serviceProtocol);
            addAasNotificationMode(args, cmdLine);
            addAppId(args, cmdLine);
            getLogger().info("Starting with arguments: {}", cmdLine);
            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            if (null != procCfg) {
                procCfg.accept(builder);
            } else {
                builder.inheritIO();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> serverMgr.stopServers(artDesc)));
            Process proc = builder.start();
            if (stop > 0) {
                getLogger().info("Scheduling for auto-stop after " + stop + " ms");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    
                    @Override
                    public void run() {
                        getLogger().info("Auto-stop after: " + stop + " ms");
                        proc.destroyForcibly();
                        serverMgr.stopServers(artDesc);
                        timer.cancel();
                        if (doExit) {
                            System.exit(0);
                        }
                    }
                }, stop);
            }
            proc.waitFor();
        } catch (ExecutionException | InterruptedException | IOException e) {
            getLogger().error("Running the app: " + e.getMessage());
        }
    }
    
    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(SpringStartup.class);
    }
    
    /**
     * Returns the artifact descriptor(s) from the deployment descriptor in {@code file}.
     * 
     * @param file the file to read the artifact from
     * @return the artifact descriptor(s), may be empty
     */
    private static Collection<SpringCloudArtifactDescriptor> getArtifacts(File file) {
        Collection<SpringCloudArtifactDescriptor> result = new ArrayList<>();
        YamlArtifact yamlArtifact = null;
        if (null != file) {
            try {
                yamlArtifact = DescriptorUtils.readFromFile(file);
                SpringCloudArtifactDescriptor desc = SpringCloudArtifactDescriptor.createInstance(
                    yamlArtifact, file.toURI(), file);
                result.add(desc);
            } catch (ExecutionException e) {
                getLogger().error("Loading deployment descriptor from : {}. Cannot start servers.", e.getMessage());
            }
        } else {
            getLogger().error("Loading deployment descriptor from {}: Cannot load file, cannot start servers.");
        }
        return result;
    }

}
