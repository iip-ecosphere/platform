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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;

import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.ProcessUnitBuilder;
import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.TerminationListener;
import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.TerminationReason;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.collector.Collector;
import de.iip_ecosphere.platform.support.iip_aas.config.RuntimeSetup;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;
import de.iip_ecosphere.platform.tools.maven.python.AbstractLoggingMojo;
import de.iip_ecosphere.platform.tools.maven.python.FilesetUtils;

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

    @Parameter(property = "configuration.testApp.appOffline", required = false, defaultValue = "true")
    private boolean appOffline;

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

    @Parameter(property = "configuration.testApp.logRegExMatchCount", required = false, defaultValue = "1")
    private int logRegExMatchCount;

    @Parameter(property = "configuration.testApp.skip", required = false, defaultValue = "false")
    private boolean skip;

    @Parameter(property = "configuration.testApp.brokerDir", required = false, defaultValue = "")
    private String brokerDir;

    @Parameter(property = "configuration.testApp.brokerPort", required = false, defaultValue = "-1")
    private int brokerPort;

    @Parameter(property = "configuration.testApp.brokerWaitTime", required = true, defaultValue = "3000")
    private int brokerWaitTime;
    
    @Parameter(property = "configuration.testApp.testTime", required = true, defaultValue = "120000")
    private int testTime;
    private int testTimePlatform;
    
    @Parameter(property = "configuration.outputDirectory", required = true, defaultValue = "gen")
    private String outputDirectory;

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

    @Parameter(property = "configuration.testApp.mgtUiSetupFileTemplate", required = false, defaultValue = "")
    private File mgtUiSetupFileTemplate;

    @Parameter(property = "configuration.testApp.mgtUiSetupFile", required = false, defaultValue = "")
    private File mgtUiSetupFile;

    @Parameter(property = "configuration.testApp.befores", required = false)
    private List<TestProcessSpec> befores;
    
    @Parameter(property = "configuration.ngTest.nodejs", required = false, defaultValue = "")
    private String nodejs;  // takeover    
    
    @Parameter(required = false)
    private FileSet artifacts;
    
    private List<ProcessUnit> units = new ArrayList<>();
    
    private long testStart;
    
    /**
     * Builds the process unit of {@code builder} and registers it for shutdown.
     * 
     * @param builder the builder
     * @return the created process unit
     * @throws MojoExecutionException if creating the process fails
     */
    private ProcessUnit buildAndRegister(ProcessUnitBuilder builder) throws MojoExecutionException {
        ProcessUnit result = builder.build4Mvn();
        units.add(0, result);
        return result;
    }
    
    /**
     * Creates a process unit builder for a platform process.
     * 
     * @param description the description of the process
     * @param home the home directory of the script
     * @param scriptName the script name (without extension)
     * @param listener optional listener to be informed when the process terminates or a match happens, 
     *    may be <b>null</b>
     * @param args additional optional arguments
     * @return the process unit builder
     */
    private ProcessUnitBuilder createPlatformBuilder(String description, File home, String scriptName, 
        TerminationListener listener, String... args) {
        Pattern p = Pattern.compile("^.*" + LifecycleHandler.MSG_STARTUP_COMPLETED + ".*$");
        ProcessUnitBuilder builder = new ProcessUnit.ProcessUnitBuilder(description, this)
            .setHome(home)
            .addShellScriptCommand(scriptName)
            .addArguments(args)
            .addCheckRegEx(p)
            .setListener(listener)
            .setTimeout(testTimePlatform)
            .setNotifyListenerByLogMatch(true);
        return builder;
    }
    
    /**
     * Starts a platform service and waits for startup completion.
     * 
     * @param description the description of the process
     * @param home the home directory of the script
     * @param scriptName the script name (without extension)
     * @param args additional optional arguments
     * @return the process unit builder
     * @throws MojoExecutionException if starting the service fails
     */
    private ProcessUnit startPlatformService(String description, File home, String scriptName, String... args) 
        throws MojoExecutionException {
        AtomicBoolean started = new AtomicBoolean();
        ProcessUnit pu = buildAndRegister(createPlatformBuilder(description, home, scriptName, r -> {
            if (TerminationReason.MATCH_COMPLETE == r) {
                started.set(true); 
            }
            return false;
        }, args));
        TimeUtils.waitFor(() -> !started.get(), 50 * 1000, 300);
        if (!started.get()) {
            throw new MojoExecutionException("Start of " + pu.getDescription() + " did not emit expected regEx");
        }
        return pu;
    }
    
    /**
     * Starts the platform processes depending on the selection in the attributes of this class. Registers all 
     * created processes via {@link #buildAndRegister(ProcessUnitBuilder)}.
     * 
     * @param brokerPort the port the broker is running on
     * @return the runtime setup instance of the platform, may be <b>null</b> in case of failures
     * 
     * @see #platformDir
     * @see #startPlatform
     * @see #startEcsServiceManager
     * @see #startEcsRuntime
     * @see #startServiceManager
     * @throws MojoExecutionException if starting the service fails
     */
    private RuntimeSetup startPlatform(int brokerPort) throws MojoExecutionException {
        RuntimeSetup result = null;
        if (isValidFile(platformDir)) {
            FilesetUtils.streamFiles(artifacts, false).forEach(f -> {
                File target = new File(new File(platformDir, "artifacts"), f.getName());
                try {
                    getLog().info("Copying artifact " + f + " to " + target);
                    Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    getLog().error("Cannot copy artifact " + f + ":" + e.getMessage());
                }
            });
            File rtSetup = RuntimeSetup.getFile();
            FileUtils.deleteQuietly(rtSetup);
            File local = new File(platformDir, AbstractSetup.DEFAULT_OVERRIDE_FNAME);
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
                startPlatformService("platform central services", platformDir, "platform");
            }
            if (startEcsServiceManager) {
                startPlatformService("ECS-Runtime & service manager", platformDir, "ecsServiceMgr", iipId);
            } else {
                if (startEcsRuntime) {
                    startPlatformService("ECS-Runtime", platformDir, "ecs", iipId);
                }
                if (startServiceManager) {
                    int iipPort = NetUtils.getEphemeralPort(); // usually set by container
                    startPlatformService("service manager", platformDir, "serviceMgr", "--iip.port=" + iipPort, iipId);
                }
            }
            if (TimeUtils.waitFor(() -> !rtSetup.exists(), 2000, 300)) {
                result = RuntimeSetup.load();
            }
        }
        return result;
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
     * @param id the undeployment id
     * @throws MojoExecutionException if deployment/undeployment fails
     */
    private void deployApp(boolean deploy, String id) throws MojoExecutionException {
        if (isValidFile(platformDir) && isValidFile(deploymentPlan)) {
            String name = deploy ? "deploy app" : "undeploy app";
            if (id.length() > 0) {
                name += " " + id;
            }
            ProcessUnitBuilder pub = new ProcessUnitBuilder(name, this)
                .setHome(platformDir)
                .addShellScriptCommand("cli")
                .addArgument(deploy ? "deploy" : "undeploy")
                .addArgument(deploymentPlan.getAbsolutePath());
            if (!deploy) {
                pub.addArgument(id)
                    .setTimeout(20000); // may already be gone
            }
            ProcessUnit pu = pub.build4Mvn();
            int status = pu.waitFor();
            if (deploy && ProcessUnit.isFailed(status)) {
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
        if (isValidFile(platformDir)) {
            FileUtils.deleteQuietly(new File(platformDir, AbstractSetup.DEFAULT_OVERRIDE_FNAME));
        }
        FileUtils.deleteQuietly(mgtUiSetupFile);
        return failed;
    }

    /**
     * Handles a termination notification.
     * 
     * @param reason the termination reason
     * @param terminated the terminated flag to change as a side effect
     * @param testTerminatedCount how often was a termination indicated so far
     * @return stop the process or not
     */
    private boolean handleTermination(TerminationReason reason, AtomicBoolean terminated, 
        AtomicInteger testTerminatedCount) {
        boolean stop = true;
        switch (reason) {
        case TIMEOUT:
            getLog().info("Test timeout");
            break;
        case MATCH_COMPLETE:
            int tCount = testTerminatedCount.incrementAndGet();
            if (tCount >= Math.max(logRegExMatchCount, 1)) {
                getLog().info("Required regEx matches complete. Stopping test.");
                Collector.collect(project.getArtifactId())
                    .addExecutionTimeMs(System.currentTimeMillis() - testStart)
                    .close();
            } else {
                getLog().info("Required regEx matched " + tCount + " times.");
                stop = false;
            }
            break;
        default:
            break;
        }
        terminated.set(stop);
        return stop;
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            testTimePlatform = testTime; 
            // extend platform processes timeout over test time if we running a platform/app
            if (isValidFile(platformDir)) {
                testTimePlatform += TimeUnit.MINUTES.toMillis(2);
            }
            if (isValidFile(deploymentPlan)) {
                testTimePlatform += TimeUnit.MINUTES.toMillis(3); // start and shutdown
            }
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
        if (offline || appOffline) {
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
     * Replaces a placeholder in the UI configuration template by a String value.
     * 
     * @param contents the contents to apply the replacement on
     * @param placeholder the placeholder name
     * @param value the value to replace the placeholder with
     * @return the modified contents
     */
    private String replacePlaceholder(String contents, String placeholder, String value) {
        return null == value ? contents : contents.replace("${" + placeholder + "}", value);
    }

    /**
     * Replaces a placeholder in the UI configuration template by a URI value.
     * 
     * @param contents the contents to apply the replacement on
     * @param placeholder the placeholder name
     * @param value the value to replace the placeholder with
     * @param info information on the placeholder/value for logging
     * @return the modified contents
     */
    private String replaceAsUri(String contents, String placeholder, String value, String info) {
        String result = contents;
        if (null != value) {
            try {
                URI uri = new URI(value);
                uri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null);
                result = replacePlaceholder(contents, placeholder, uri.toString());
            } catch (URISyntaxException  e) {
                getLog().error("Cannot process " + info + ": " + e.getMessage());
            }
        } else {
            getLog().warn("No " + info + " stated in runtime setup");
        }
        return result;
    }

    /**
     * If {@code setup} is given, turn {@link #mgtUiSetupFileTemplate} into {@link #mgtUiSetupFile} by 
     * replacing {@code ${aasRegistryUri}}.
     * 
     * @param setup the setup file
     */
    private void writeMgtUiSetup(RuntimeSetup setup) {
        boolean mgtOutFileDefined = mgtUiSetupFile != null && mgtUiSetupFile.getPath().length() > 0; 
        if (null != setup && isValidFile(mgtUiSetupFileTemplate) && mgtOutFileDefined) {
            
            try {
                String contents = FileUtils.readFileToString(mgtUiSetupFileTemplate, Charset.defaultCharset());
                contents = replaceAsUri(contents, "aasRegistryUri", setup.getAasRegistry(), "AAS registry");
                contents = replaceAsUri(contents, "aasServerUri", setup.getAasServer(), "AAS server");
                mgtUiSetupFile.getParentFile().mkdirs();
                try (PrintWriter out = new PrintWriter(new FileWriter(mgtUiSetupFile))) {
                    out.println(contents);
                }
                getLog().info("Wrote processed management UI setup file " + mgtUiSetupFile);
            } catch (IOException e) {
                getLog().error("Cannot process managment UI setup template/file: " + e.getMessage());
            }
        } else {
            if (null == setup) {
                getLog().warn("No platform runtime setup found");
            } else if (!isValidFile(mgtUiSetupFileTemplate)) {
                getLog().warn("No management UI setup template defined/found: " + mgtUiSetupFileTemplate);
            } else if (!mgtOutFileDefined) {
                getLog().warn("No management UI setup output file defined");
            }
        }
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
                result = p.extrapolateArgs(result);
            }
        }
        return result;
    }
    
    /**
     * Determines the probable broker directory from settings or {@link #outputDirectory}.
     * 
     * @return the broker directory
     */
    private File determineBrokerDir() {
        File dir;
        if (brokerDir != null && brokerDir.length() > 0) {
            dir = new File(brokerDir);
        } else {
            File tmp = new File(outputDirectory);
            do {
                dir = new File(tmp, "broker/broker");
                if (!dir.exists()) {
                    tmp = new File(tmp.getParent());
                }
            } while (!dir.exists() && null != tmp && tmp.toString().length() > 0);
            if (null == tmp || tmp.toString().length() == 0) {
                dir = new File(outputDirectory, "broker/broker"); // fallback
            }
        }
        getLog().info("Using broker dir: " + dir);
        return dir;
    }
    
    /**
     * Implements the test execution.
     * 
     * @throws MojoExecutionException if the Mojo execution failed
     * @throws MojoFailureException if the Mojo failed
     */
    public void executeImpl() throws MojoExecutionException, MojoFailureException {
        AtomicInteger testTerminatedCount = new AtomicInteger();
        AtomicBoolean testTerminated = new AtomicBoolean(false); 
        if (brokerPort < 0) {
            brokerPort = NetUtils.getEphemeralPort();
        }
        if (brokerPort > 0) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stopProcessUnits()));
            getLog().info("Using broker port: " + brokerPort);
            buildAndRegister(createPlatformBuilder("broker", determineBrokerDir(), "broker", null, 
                String.valueOf(brokerPort))
                .setTimeout(testTimePlatform));
            // broker may take a while; regEx would be ok, but we do not know the broker here -> generation?
            TimeUtils.sleep(brokerWaitTime);
            writeMgtUiSetup(startPlatform(brokerPort));
        }
        startProcesses();
        
        ProcessUnitBuilder testBuilder = new ProcessUnit.ProcessUnitBuilder("test app", this);
        if (null != logFile) {
            getLog().info("Logging test output to " + logFile);
            testBuilder.logTo(logFile);
        }
        if (null != testCmd && testCmd.length() > 0) {
            deployApp(true, "");
            testBuilder.setNodeJsHome(nodejs);
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
            .setListener(r -> handleTermination(r, testTerminated, testTerminatedCount));
        if (logFile != null && logFile.getPath().length() > 0) {
            testBuilder.logTo(logFile);
        }
        testStart = System.currentTimeMillis();
        ProcessUnit testUnit = buildAndRegister(testBuilder);
        getLog().info("Waiting for test end, at maximum specified test time: " + testTime + " ms");
        TimeUtils.waitFor(() -> !testTerminated.get() && testUnit.isRunning(), testTime, 300);
        if (null != testCmd && testCmd.length() > 0) {
            deployApp(false, "1");
        }
        int testUnitExitStatus = testUnit.getExitValue();
        boolean failed = stopProcessUnits();
        if (testUnit.hasCheckRegEx()) {
            if (!testUnit.getLogMatches()) {
                throw new MojoExecutionException("Specified regular expressions do not match. Test did not succeed.");
            }
        }
        if (testUnitExitStatus != ProcessUnit.UNKOWN_EXIT_STATUS && testUnitExitStatus != 0) {
            throw new MojoExecutionException("Test processes did not succeed (status " 
                + testUnitExitStatus + ") See above for details.");
        }
        if (failed) {
            throw new MojoExecutionException("Spawned processes did not terminate successfully. "
                + "See above for details.");
        }
    }

}
