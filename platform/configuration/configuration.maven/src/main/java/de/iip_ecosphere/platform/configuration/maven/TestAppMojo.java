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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.ProcessUnitBuilder;
import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.TerminationReason;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.collector.Collector;

/**
 * A platform application testing MOJO. May start an entire (local) platform
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "testApp", defaultPhase = LifecyclePhase.PACKAGE)
public class TestAppMojo extends AbstractLoggingMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    
    @Parameter(defaultValue = "${session.offline}")
    private boolean offline;
    
    @Parameter(defaultValue = "${session.request}")
    private MavenExecutionRequest request;
    
    @Parameter(property = "configuration.testApp.testCmd", required = false, defaultValue = "")
    private String testCmd;

    @Parameter(property = "configuration.testApp.testCmdAsScript", required = false, defaultValue = "false")
    private boolean testCmdAsScript;

    @Parameter(property = "configuration.testApp.appId", required = false, defaultValue = "app")
    private String appId;

    @Parameter(property = "configuration.testApp.appProfile", required = false, defaultValue = "App")
    private String appProfile;

    @Parameter(property = "configuration.testApp.appPom", required = false)
    private File appPom;

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

    @Parameter(property = "configuration.testApp.deploymentPlan", required = false)
    private File deploymentPlan;

    @Parameter(property = "configuration.testApp.deploymentResource", required = false, defaultValue = "local")
    private String deploymentResource;

    @Parameter(property = "configuration.testApp.befores", required = false)
    private List<TestProcessSpec> befores;
    
    private List<ProcessUnit> units = new ArrayList<>();
    
    private long testStart;
    
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
        if (isValidFile(platformDir)) {
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
            final String iipId = "--iip.id=" + deploymentResource;
            if (startPlatform) {
                buildAndRegister(createPlatformBuilder("platform central services", gen, "platform"));
            }
            if (startEcsServiceManager) {
                buildAndRegister(createPlatformBuilder("ECS-Runtime & service manager", gen, "ecsServiceMgr", iipId));
            } else {
                if (startEcsRuntime) {
                    buildAndRegister(createPlatformBuilder("ECS-Runtime", gen, "ecs", iipId));
                }
                if (startServiceManager) {
                    int iipPort = NetUtils.getEphemeralPort(); // usually set by container
                    buildAndRegister(createPlatformBuilder("service manager", gen, "serviceMgr", 
                        "--iip.port=" + iipPort, iipId));
                }
            }
        }
    }
    
    /**
     * Returns if {@code file} is a valid file.
     * 
     * @param file the file
     * @return {@code true} for valid, {@code false}
     */
    private static boolean isValidFile(File file) {
        return null != file && file.exists();
    }
    
    /**
     * Deploys an application via deployment descriptor if specified and case of a {@link #testCmd}.
     * The resource in the deployment descriptor must be {@link #deploymentResource}.
     * 
     * @param deploy deploy or undeploy
     * @throws MojoExecutionException if deployment/undeployment fails
     */
    private void deployApp(boolean deploy) throws MojoExecutionException {
        if (isValidFile(platformDir) && isValidFile(deploymentPlan)) {
            File gen = new File("gen");
            ProcessUnit pu = new ProcessUnitBuilder(deploy ? "deploy app" : "undeploy app", this)
                .setHome(gen)
                .addShellScriptCommand("cli")
                .addArgument(deploy ? "deploy" : "undeploy")
                .addArgument(deploymentPlan.getAbsolutePath())
                .build();
            int status = pu.waitFor();
            if (ProcessUnit.isFailed(status)) {
                throw new MojoExecutionException(pu.getDescription() + " terminated with status: " + status);
            }
        }
        
    }

    /**
     * Starts defined processes.
     * 
     * @throws MojoExecutionException if process execution fails
     */
    private void startProcesses() throws MojoExecutionException {
        if (befores != null) {
            for (TestProcessSpec p : befores) {
                p.allocatePorts(project, getLog());
                ProcessUnitBuilder builder = new ProcessUnitBuilder(p.getDescription(), this);
                builder.addArgumentOrScriptCommand(p.isCmdAsScript(), p.getCmd());
                if (p.isErrToIn()) {
                    builder.redirectErr2In();
                }
                if (null != p.getHome()) {
                    builder.setHome(p.getHome());
                }
                builder.addArguments(p.extrapolateArgs());
                ProcessUnit pu = buildAndRegister(builder);
                if (p.isWaitFor()) {
                    int status = pu.waitFor();
                    if (status != ProcessUnit.UNKOWN_EXIT_STATUS && status != 0) {
                        throw new MojoExecutionException(pu.getDescription() + " terminated with status: " + status);
                    }
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
            Collector.collect(project.getArtifactId())
                .addExecutionTimeMs(System.currentTimeMillis() - testStart)
                .close();
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
        if (isValidFile(appPom)) {
            testBuilder.addArgument("-f");
            testBuilder.addArgument(appPom);
        }
        if (appProfile != null && appProfile.trim().length() > 0 && !appProfile.equals("-")) {
            testBuilder.addArgument("-P");
            testBuilder.addArgument(appProfile);
        }
        String buildId = System.getProperty(Collector.PROPERTY_BUILDID);
        if (buildId != null) {
            testBuilder.addArgument("-D" + Collector.PROPERTY_BUILDID + "=" + buildId);
        }
        if (null != mvnArgs) {
            testBuilder.addArguments(extrapolate(mvnArgs, befores));
        }
        testBuilder.addArgument("exec:java@" + appId);
        if (null != mvnPluginArgs) {
            testBuilder.addArguments(extrapolate(mvnPluginArgs, befores));
        }
        testBuilder.addArgument(
            "-Diip.springStart.args=\"--iip.test.stop=" + testTime 
            + " --iip.test.brokerPort=" + brokerPort 
            + tmpAppArgs + "\"");
        return testBuilder;
    }
    
    /**
     * Extrapolates the given arguments for the given processes.
     * 
     * @param args the command line arguments, may be <b>null</b>
     * @param processes the processes, may be <b>null</b>
     * @return the extrapolated arguments
     */
    private List<String> extrapolate(List<String> args, List<TestProcessSpec> processes) {
        List<String> result = null;
        if (null != args && null != processes) {
            result = new ArrayList<>();
            result.addAll(args);
            for (TestProcessSpec p : processes) {
                p.extrapolateArgs(result);
            }
        }
        return result;
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
        if (brokerPort > 0) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stopProcessUnits()));
            getLog().info("Using broker port: " + brokerPort);
            buildAndRegister(createPlatformBuilder("broker", new File("gen/broker/broker"), "broker", 
                String.valueOf(brokerPort))
                .setTimeout(testTime));
            TimeUtils.sleep(brokerWaitTime); // broker may take a while
            startPlatform(brokerPort);
        }
        startProcesses();
        
        ProcessUnitBuilder testBuilder = new ProcessUnit.ProcessUnitBuilder("test app", this);
        if (null != logFile) {
            getLog().info("Logging test output to " + logFile);
            testBuilder.logTo(logFile);
        }
        if (null != testCmd && testCmd.length() > 0) {
            deployApp(true);
            testBuilder.addArgumentOrScriptCommand(testCmdAsScript, testCmd);
            testBuilder.addArguments(appArgs);
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
        testStart = System.currentTimeMillis();
        ProcessUnit testUnit = buildAndRegister(testBuilder);
        getLog().info("Waiting for test end, at maximum specified test time: " + testTime + " ms");
        TimeUtils.waitFor(() -> !testTerminated.get() && testUnit.isRunning(), testTime, 300);
        if (null != testCmd && testCmd.length() > 0) {
            deployApp(false);
        }
        
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
