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

package de.iip_ecosphere.platform.configuration.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.ProcessUnitBuilder;
import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.TerminationReason;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * A platform application testing MOJO. May start an entire (local) platform
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "testApp", defaultPhase = LifecyclePhase.PACKAGE)
public class TestAppMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session.offline}")
    private boolean offline;
    
    @Parameter(defaultValue = "${session.request}")
    private MavenExecutionRequest request;
    
    @Parameter(property = "configuration.testApp.testCmd", required = false, defaultValue = "")
    private String testCmd;

    @Parameter(property = "configuration.testApp.appId", required = false, defaultValue = "app")
    private String appId;

    @Parameter(property = "configuration.testApp.appArgs", required = false)
    private List<String> appArgs;

    @Parameter(property = "configuration.testApp.mvnArgs", required = false)
    private List<String> mvnArgs;

    @Parameter(property = "configuration.testApp.mvnPluginArgs", required = false)
    private List<String> mvnPluginArgs;

    @Parameter(property = "configuration.testApp.logFile", required = false, defaultValue = "")
    private File logFile;

    @Parameter(property = "configuration.testApp.logRegExprs", required = false)
    private List<String> logRegExprs;

    @Parameter(property = "configuration.testApp.logRegExConjunction", required = false, defaultValue = "true")
    private boolean logRegExConjunction;

    @Parameter(property = "configuration.testApp.skip", required = false, defaultValue = "false")
    private boolean skip;

    @Parameter(property = "configuration.testApp.brokerPort", required = false, defaultValue = "-1")
    private int brokerPort;

    @Parameter(property = "configuration.testApp.brokerWaitTime", required = true, defaultValue = "3000")
    private int brokerWaitTime;
    
    @Parameter(property = "configuration.testApp.testTime", required = true, defaultValue = "120000")
    private int testTime;

    @Parameter(property = "configuration.testApp.platformDir", required = false, defaultValue = "")
    private File platformDir;

    @Parameter(property = "configuration.testApp.startPlatform", required = false, defaultValue = "true")
    private boolean startPlatform;

    @Parameter(property = "configuration.testApp.startEcsRuntime", required = false, defaultValue = "false")
    private boolean startEcsRuntime;

    @Parameter(property = "configuration.testApp.startServiceMgr", required = false, defaultValue = "false")
    private boolean startServiceManager;

    @Parameter(property = "configuration.testApp.startEcsServiceMgr", required = false, defaultValue = "true")
    private boolean startEcsServiceManager;
    
    private List<ProcessUnit> units = new ArrayList<>();
    
    /**
     * Builds the process unit of {@code builder} and registers it for shutdown.
     * 
     * @param builder the builder
     * @return the created process unit
     */
    private ProcessUnit buildAndRegister(ProcessUnitBuilder builder) {
        ProcessUnit result = builder.build();
        units.add(0, result);
        return result;
    }
    
    /**
     * Creates a process unit builder for a platform process.
     * 
     * @param description the description of the process
     * @param home the home directory of the script
     * @param scriptName the script name (without extension)
     * @param args additional optional arguments
     * @return the process unit builder
     */
    private ProcessUnitBuilder createPlatformBuilder(String description, File home, String scriptName, 
        String... args) {
        ProcessUnitBuilder builder = new ProcessUnit.ProcessUnitBuilder(description, this)
            .setHome(home)
            .addShellScriptCommand(scriptName)
            .addArguments(args);
        return builder;
    }
    
    /**
     * Starts the platform processes depending on the selection in the attributes of this class. Registers all 
     * created processes via {@link #buildAndRegister(ProcessUnitBuilder)}.
     * 
     * @param brokerPort the port the broker is running on
     * 
     * @see #platformDir
     * @see #startPlatform
     * @see #startEcsServiceManager
     * @see #startEcsRuntime
     * @see #startServiceManager
     */
    private void startPlatform(int brokerPort) {
        if (null != platformDir && platformDir.getPath().length() > 0) {
            File local = new File("gen/oktoflow-local.yml");
            File gen = new File("gen");
            try (PrintStream setupLocal = new PrintStream(local)) {
                setupLocal.println("transport: ");
                setupLocal.println("  port: " + brokerPort);
                setupLocal.println("service-mgr: ");
                setupLocal.println("  transport: ");
                setupLocal.println("    port: " + brokerPort);
            } catch (IOException e) {
                getLog().error("Cannot write " + local);
            }
            if (startPlatform) {
                buildAndRegister(createPlatformBuilder("platform central services", gen, "platform"));
            }
            if (startEcsServiceManager) {
                buildAndRegister(createPlatformBuilder("ECS-Runtime & service manager", gen, "ecsServiceMgr"));
            } else {
                if (startEcsRuntime) {
                    buildAndRegister(createPlatformBuilder("ECS-Runtime", gen, "ecs"));
                }
                if (startServiceManager) {
                    int iipPort = NetUtils.getEphemeralPort(); // usually set by container
                    buildAndRegister(createPlatformBuilder("service manager", gen, "serviceMgr", 
                        "--iip.port=" + iipPort));
                }
            }
        }
    }

    /**
     * Stops all registered process units.
     * 
     * @return {@code true} for failed, {@code false} else
     */
    private synchronized boolean stopProcessUnits() {
        boolean failed = false;
        for (ProcessUnit u: units) {
            int status = u.stop();
            if (status != ProcessUnit.UNKOWN_EXIT_STATUS && status != 0) {
                getLog().error(u.getDescription() + " terminated with status: " + status);
                failed = true;
            }
        }
        units.clear();
        return failed;
    }

    /**
     * Handles a termination notification.
     * 
     * @param reason the termination reason
     * @param terminated the terminated flag to change as a side effect
     */
    private void handleTermination(TerminationReason reason, AtomicBoolean terminated) {
        switch (reason) {
        case TIMEOUT:
            getLog().info("Test timeout");
            break;
        case MATCH_COMPLETE:
            getLog().info("Required regEx matches complete. Stopping test.");
            break;
        default:
            break;
        }
        terminated.set(true);
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            executeImpl();
        }
    }

    /**
     * Adds the command line arguments for a maven call.
     * 
     * @param testBuilder the process unit builder for the call
     * @return {@code testBuilder}, builder style
     */
    private ProcessUnitBuilder addMavenTestCall(ProcessUnitBuilder testBuilder) {
        String tmpAppArgs = "";
        if (null != appArgs && appArgs.size() > 0) {
            tmpAppArgs = " " + CollectionUtils.toStringSpaceSeparated(appArgs);
        }
        testBuilder.addMavenCommand();
        if (offline) {
            testBuilder.addArgument("-o");
        }
        String sPath = System.getenv("MAVEN_SETTINGS_PATH");
        if (null == sPath) {
            sPath = null != request.getUserSettingsFile() ? request.getUserSettingsFile().getPath() : null;
        }
        if (null != sPath) {
            testBuilder.addArgument("-s");
            testBuilder.addArgument(sPath);
        }
        testBuilder.addArgument("-P");
        testBuilder.addArgument("App");
        if (null != mvnArgs) {
            testBuilder.addArguments(mvnArgs);
        }
        testBuilder.addArgument("exec:java@" + appId);
        if (null != mvnPluginArgs) {
            testBuilder.addArguments(mvnPluginArgs);
        }
        testBuilder.addArgument(
            "-Diip.springStart.args=\"--iip.test.stop=" + testTime 
            + " --iip.test.brokerPort=" + brokerPort 
            + tmpAppArgs + "\"");
        return testBuilder;
    }
    
    /**
     * Implements the test execution.
     * 
     * @throws MojoExecutionException if the Mojo execution failed
     * @throws MojoFailureException if the Mojo failed
     */
    public void executeImpl() throws MojoExecutionException, MojoFailureException {
        AtomicBoolean testTerminated = new AtomicBoolean(false); 
        if (brokerPort < 0) {
            brokerPort = NetUtils.getEphemeralPort();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopProcessUnits()));
        getLog().info("Using broker port: " + brokerPort);
        buildAndRegister(createPlatformBuilder("broker", new File("gen/broker/broker"), "broker", 
            String.valueOf(brokerPort))
            .setTimeout(testTime));
        TimeUtils.sleep(brokerWaitTime); // broker may take a while

        startPlatform(brokerPort); 

        ProcessUnitBuilder testBuilder = new ProcessUnit.ProcessUnitBuilder("test app", this);
        if (null != logFile) {
            getLog().info("Logging test output to " + logFile);
            testBuilder.logTo(logFile);
        }
        if (null != testCmd && testCmd.length() > 0) {
            testBuilder
                .addArgument(testCmd)
                .addArguments(appArgs);
        } else {
            addMavenTestCall(testBuilder);
        }
        testBuilder.setRegExConjunction(logRegExConjunction);
        if (null != logRegExprs) {
            for (String ex : logRegExprs) {
                try {
                    testBuilder.addCheckRegEx(Pattern.compile(ex));
                } catch (PatternSyntaxException e) {
                    getLog().error("Cannot compile regex " + ex + ":" + e.getMessage() + " Ignoring.");
                }
            }
        }
        testBuilder
            .setTimeout(testTime)
            .setListener(r -> handleTermination(r, testTerminated));
        if (logFile != null && logFile.getPath().length() > 0) {
            testBuilder.logTo(logFile);
        }
        ProcessUnit testUnit = buildAndRegister(testBuilder);

        getLog().info("Waiting for test end, at maximum specified test time: " + testTime + " ms");
        TimeUtils.waitFor(() -> !testTerminated.get(), testTime, 300);
        
        boolean failed = stopProcessUnits();
        if (testUnit.hasCheckRegEx()) {
            if (!testUnit.getLogMatches()) {
                throw new MojoFailureException("Specified regular expressions do not match. Test did not succeed.");
            }
        }
        if (failed) {
            throw new MojoExecutionException("Spawned processes did not terminate successfully. "
                + "See above for details.");
        }
    }

}
