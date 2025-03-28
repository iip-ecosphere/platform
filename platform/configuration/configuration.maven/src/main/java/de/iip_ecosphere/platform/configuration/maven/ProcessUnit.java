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

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.JavaUtils;
import de.iip_ecosphere.platform.tools.maven.python.Logger;
import de.iip_ecosphere.platform.tools.maven.python.StandardLogger;

/**
 * Represents a process.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessUnit {
    
    public static final String PROP_MAVEN_BIN = "okto.mvn.home";
    public static final Set<String> SCRIPT_NAMES = Collections.unmodifiableSet(
        CollectionUtils.toSet("ant", "ng", "npm", "mvn"));
    public static final String[] WIN_BAT_PREFIX = {"cmd", "/s", "/c"};
    public static final int UNKOWN_EXIT_STATUS = Integer.MIN_VALUE;
    private static Timer timer;
    
    private String description;
    private Process process;
    private TimerTask timeoutTask;
    private List<Closeable> closeables;
    private boolean logMatches;
    private List<Pattern> checkRegEx;
    private Logger logger;
    private TerminationListener listener;

    // checkstyle: stop parameter number check
    
    /**
     * Creates a process unit.
     * 
     * @param description the description
     * @param process the process being executed
     * @param timeout maximum lifetime of {@code process} in milliseconds, ignored if not positive
     * @param listener optional listener to be called when the execution/testing termination is reached, 
     *     <b>null</b> for none
     * @param logger the logging instance
     * @param checkRegEx the regular expressions to be applied to the log output
     */
    private ProcessUnit(String description, Process process, long timeout, TerminationListener listener, 
        List<Pattern> checkRegEx, Logger logger) {
        this.description = description;
        this.process = process;
        this.listener = listener;
        this.logger = logger;
        if (checkRegEx != null) {
            this.checkRegEx = Collections.unmodifiableList(checkRegEx);
        }
        if (timeout > 0) {
            if (null == timer) {
                timer = new Timer();
            }
            timeoutTask = new TimerTask() {
                
                @Override
                public void run() {
                    handleListenerNotification(TerminationReason.TIMEOUT);
                }
            };
            timer.schedule(timeoutTask, timeout);
        }
    }

    // checkstyle: resume parameter number check

    /**
     * Denotes the reason for termination.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TerminationReason {
        
        /**
         * Termination by timeout.
         */
        TIMEOUT(true),
        
        /**
         * Termination when all required log regular expressions are matched.
         */
        MATCH_COMPLETE(false);
        
        private boolean stopRequired;
        
        /**
         * Creates a constant.
         * 
         * @param stopRequired is process stop required when this reason occurs
         */
        private TerminationReason(boolean stopRequired) {
            this.stopRequired = stopRequired;
        }
        
        /**
         * Returns whether process stop required when this reason occurs.
         * 
         * @return {@code true} for stop, {@code false} not required
         */
        public boolean isStopRequired() {
            return stopRequired;
        }

    }
    
    /**
     * In-Process termination listener.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TerminationListener {

        /**
         * Called when an in-process termination occurred.
         * 
         * @param reason the reason for the termination
         * @return {@code} true if the process shall be stopped, {@code false} else; 
         *  ignored for {@link TerminationReason#TIMEOUT}.
         */
        public boolean notifyTermination(TerminationReason reason);

    }
    
    /**
     * Attaches a handler.
     * 
     * @param handler the handler to be attached
     */
    private void attach(InputStreamHandler handler) {
        attach((Closeable) handler);
        new Thread(handler).start();
    }
    
    /**
     * Attaches a closeable.
     * 
     * @param closeable the closeable to attache
     */
    private void attach(Closeable closeable) {
        if (null == closeables) {
            closeables = new ArrayList<>();
        }        
        closeables.add(closeable);
    }

    /**
     * Called when there is a log-regex matching result.
     * 
     * @param terminateByLogMatch terminate the process by a log match or not
     */
    private void notifyLogMatches(boolean terminateByLogMatch) {
        logMatches = true;
        if (terminateByLogMatch) {
            handleListenerNotification(TerminationReason.MATCH_COMPLETE);
        }
    }
    
    /**
     * Handles a listener notification and decides about stopping the process, either as the reason requires 
     * immediate stop or as the listener decides.
     * 
     * @param reason the reason for the notification.
     */
    private void handleListenerNotification(TerminationReason reason) {
        boolean stop = reason.isStopRequired();
        if (null != listener) {
            stop |= listener.notifyTermination(reason);
        } else {
            stop = true;
        }
        if (stop) {
            stop();
        }
    }
    
    /**
     * Returns whether the regular expressions for process logging specified during creation 
     * matched so far.
     *  
     * @return {@code true} for match, {@code false} else (default)
     */
    public boolean getLogMatches() {
        return logMatches;
    }
    
    /**
     * Returns the exit status of the contained process. Only returns a value if the process came to an end by itself, 
     * i.e., not terminated by a timeout or by a log match. 
     * 
     * @return the process exit value, {@link #UNKOWN_EXIT_STATUS} for unknown
     */
    public int getExitValue() {
        return null == process || process.isAlive() ? UNKOWN_EXIT_STATUS : process.exitValue();
    }
    
    /**
     * Returns whether a process failed ({@code status} is not {@link #UNKOWN_EXIT_STATUS} and not {@code 0}).
     *
     * @param status the status to check
     * @return {@code true} if failed, {@code false} may be running, not started or successful
     */
    public static boolean isFailed(int status) {
        return status != ProcessUnit.UNKOWN_EXIT_STATUS && status != 0;        
    }

    /**
     * Returns whether a process failed ({@code status} is not {@link #UNKOWN_EXIT_STATUS} and {@code 0}).
     *
     * @param status the status to check
     * @return {@code true} if successful, {@code false} if running, not started or failed
     */
    public static boolean isSuccess(int status) {
        return status != ProcessUnit.UNKOWN_EXIT_STATUS && status == 0;        
    }
    
    /**
     * Returns the maven bin folder via {@link #PROP_MAVEN_BIN}.
     * 
     * @return the maven bin folder, may be <b>null</b>
     */
    public static String getMavenBinPath() {
        return System.getProperty(PROP_MAVEN_BIN);
    }

    /**
     * Returns the description of the unit.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the check regEx of this unit.
     * 
     * @return the regEx, may be <b>null</b>
     */
    public List<Pattern> getCheckRegEx() {
        return checkRegEx;
    }
    
    /**
     * Returns whether this unit has regEx to check.
     * 
     * @return {@code true} if there are regEx, {@code false} else
     */
    public boolean hasCheckRegEx() {
        return getCheckRegEx() != null && getCheckRegEx().size() > 0; 
    }

    /**
     * Returns whether the contained process is (still) running.
     * 
     * @return {@code true} if the process is running, {@code false} else
     */
    public boolean isRunning() {
        return null != process && process.isAlive();
    }
    
    /**
     * Waits for the process to end if it has been started.
     * 
     * @return the process exit value
     */
    public int waitFor() {
        int result = UNKOWN_EXIT_STATUS;
        if (null != process && process.isAlive()) {
            try {
                result = process.waitFor();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return result;
    }
    
    /**
     * Creates process units.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ProcessUnitBuilder {
        
        private String description;
        private List<String> args = new ArrayList<>();
        private File home;
        private Logger logger;
        private long timeout;
        private File logFile;
        private List<Pattern> checkRegEx;
        private TerminationListener listener;
        private boolean notifyByLogMatch = true;
        private boolean conjunctLogMatches = true;
        private String argAggregate;
        private String argAggregateStart;
        private String argAggregateEnd;
        private boolean err2In = false;
        private String nodejsHome;

        /**
         * Creates a process builder instance.
         *
         * @param description the description of the unit
         * @param logger the logger, may be <b>null</b> then a {@link StandardLogger} is used
         */
        public ProcessUnitBuilder(String description, Logger logger) {
            this.description = description;
            this.logger = null == logger ? new StandardLogger() : logger;
        }

        /**
         * Sets the logging to a given file.
         * 
         * @param logFile the file to log to
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder logTo(File logFile) {
            this.logFile = logFile;
            return this;
        }

        /**
         * Adds a checking regular expression on the logging.
         * 
         * @param regEx the regular expression
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addCheckRegEx(Pattern regEx) {
            if (null == checkRegEx) {
                checkRegEx = new ArrayList<>();
            }
            checkRegEx.add(regEx);
            return this;
        }
        
        /**
         * Sets the log regEx aggregation mode, either conjunction or disjunction.
         * 
         * @param conjunct if {@code true} the default, all regular expressions must match, if {@code false} only one
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder setRegExConjunction(boolean conjunct) {
            this.conjunctLogMatches = conjunct;
            return this;
        }

        /**
         * Sets the process home directory.
         * 
         * @param home the home directory
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder setHome(File home) {
            this.home = home;
            return this;
        }
        
        /**
         * Redirects the process error stream so that it appears as input stream.
         * 
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder redirectErr2In() {
            this.err2In = true;
            return this;
        }
        
        /**
         * Adds the maven command (shall be used for arguments).
         * 
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addMavenCommand() {
            if (SystemUtils.IS_OS_WINDOWS) {
                addArguments(ProcessUnit.WIN_BAT_PREFIX);
                enableArgumentAggregation();
            }
            addArgument(getMavenPath());
            return this;
        }
        
        /**
         * Returns the maven path.
         * 
         * @return the maven path
         */
        private String getMavenPath() {
            String path = getMavenBinPath();
            if (path != null) {
                if (!path.endsWith(File.separator)) {
                    path += File.separator;
                }
            } else {
                path = "";
            }
            return path + "mvn";
        }
        
        /**
         * Prepends {@code path} before {@code cmd} as file system path.
         * 
         * @param path the path, may be empty/null (ignored)
         * @param cmd the command
         * @return the composed path (if not ignored) and command separated by a file separator
         */
        private String prependPath(String path, String cmd) {
            String result = cmd;
            if (null != path && path.length() > 0) {
                if (!path.endsWith(File.separator)) {
                    path += File.separator;
                }
                result = path + cmd;
            }
            return result;
        }
        
        /**
         * Changes the nodejs home/bin directory, i.e., the directory where the bin files are located, which is usually 
         * in Windows the home directory, in linux the bin sub-directory.
         * 
         * @param home the home directory, ignored if <b>null</b> or empty
         * @return <b>this</b> for chaining
         */
        public ProcessUnitBuilder setNodeJsHome(String home) {
            if (home != null && home.length() > 0) {
                this.nodejsHome = home;
            }
            return this;
        }
        
        /**
         * Returns the path for NodeJS.
         * 
         * @return the path, may be empty for unknown
         */
        private String getNodeJsPath() {
            String result = nodejsHome;
            if (null == result) {
                result = System.getenv("NODEJS_HOME");
            }
            if (null == result) {
                AtomicReference<String> res = new AtomicReference<>(result);
                splitPath(e -> {
                    if (e.contains("nodejs")) {
                        res.set(e);
                    }                
                });
                result = res.get();
            }
            return result;
        }

        /**
         * Adds a shell command (shall be used for arguments). Prefixes a command processor for windows and
         * a "./" for the others.
         * 
         * @param scriptName the script name without extension (adding "bat" for windows and "sh" else)
         * @return <b>this</b> (builder style)
         * @see #addShellScriptCommand(String, String)
         */
        public ProcessUnitBuilder addShellScriptCommand(String scriptName) {
            return addShellScriptCommand(scriptName, SystemUtils.IS_OS_WINDOWS ? "bat" : "sh");
        }

        /**
         * Adds a shell script command (shall be used for arguments). Prefixes a command processor for windows and
         * a "./" for the others.
         * 
         * @param scriptName the script name without extension
         * @param extension the extension (may be empty, start with "." or just be the extension)
         * @return <b>this</b> (builder style)
         * @see #addShellScriptCommand(String, String)
         */
        public ProcessUnitBuilder addShellScriptCommand(String scriptName, String extension) {
            String cmd = scriptName;
            if (extension != null && extension.length() > 0) {
                String e = extension;
                if (!e.startsWith(".")) {
                    e = "." + e;
                }
                cmd += e;
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                addArguments(ProcessUnit.WIN_BAT_PREFIX);
                enableArgumentAggregation();
                addArgument(cmd);
            } else {
                addArguments("sh", "./" + cmd);
            }
            return this;
        }
        
        /**
         * Enables argument aggregation if needed, i.e., some command lines require complex arguments to be aggregated
         * into a single argument. If no aggregation is needed, arguments are added directly to {@link #args}. 
         */
        private void enableArgumentAggregation() {
            if (SystemUtils.IS_OS_WINDOWS) {
                argAggregate = "";
                argAggregateStart = "\"";
                argAggregateEnd = "\"";
            }
        }
        
        /**
         * Appends an argument by aggregation if enabled through {@link #enableArgumentAggregation()}.
         * 
         * @param arg the argument(s)
         * @return {@code true} if {@code arg} was not processed, {@code false} if {@code arg} was processed and does 
         *     not need further aggregation
         */
        private boolean appendByAggregation(String arg) {
            if (argAggregate != null && arg.length() > 0) {
                if (argAggregate.length() > 0) {
                    argAggregate += " ";
                }
                argAggregate += arg;
            }
            return argAggregate == null;
        }

        /**
         * Adds {@code cmd} as a script (also if in {@link ProcessUnit#SCRIPT_NAMES}) 
         * or as argument.
         * 
         * @param cmd the script name
         * @return <b>this</b> (builder style)
         * @see #addArgumentOrScriptCommand(boolean, String)
         */
        public ProcessUnitBuilder addArgumentOrScriptCommand(String cmd) {
            return addArgumentOrScriptCommand(false, cmd);
        }

        /**
         * Adds {@code cmd} as a script (also if in {@link ProcessUnit#SCRIPT_NAMES}) 
         * or as argument.
         * 
         * @param cmdAsScript is {@code cmd} is a script command
         * @param cmd the script name
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addArgumentOrScriptCommand(boolean cmdAsScript, String cmd) {
            if (SCRIPT_NAMES.contains(cmd)) {
                if (SystemUtils.IS_OS_WINDOWS) {
                    addArguments(ProcessUnit.WIN_BAT_PREFIX);
                    enableArgumentAggregation();
                }
                if ("ng".equals(cmd) || "npm".equals(cmd)) {  // unqualified, try to qualify
                    cmd = prependPath(getNodeJsPath(), cmd);
                }
                addArgument(cmd);
            } else if (cmdAsScript) {
                addShellScriptCommand(cmd);
            } else {
                if ("mvn".equals(cmd)) { // unqualified, try to qualify
                    cmd = getMavenPath();
                }
                addArgument(cmd);
            }
            return this;
        }
        
        /**
         * Adds a conditional command line argument.
         * 
         * @param enable the condition result
         * @param argument the argument
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addArgument(boolean enable, Object argument) {
            if (enable) {
                addArgument(argument);
            }
            return this;
        }
        
        /**
         * Adds a command line argument.
         * 
         * @param argument the argument
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addArgument(Object argument) {
            String arg = argument.toString();
            if (appendByAggregation(arg)) {
                args.add(arg);
            }
            return this;
        }

        /**
         * Adds command line arguments.
         * 
         * @param arguments the arguments (may be <b>null</b>, ignored)
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addArguments(List<String> arguments) {
            if (null != arguments) {
                if (appendByAggregation(CollectionUtils.toStringSpaceSeparated(arguments))) {
                    args.addAll(arguments);
                }
            }
            return this;
        }

        /**
         * Adds command line arguments.
         * 
         * @param arguments the arguments
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addArguments(String... arguments) {
            return addArguments(CollectionUtils.toList(arguments));            
        }
        
        /**
         * Sets the lifetime/timeout of the process.
         * 
         * @param timeout the timeout in milliseconds, default is 0, ignored if not positive
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets whether the process listener shall be informed on a complete log regEx match, terminating the process
         * if there is no listener. 
         * 
         * @param notifyByLogMatch notify the listener by a complete log match or not (default is <code>true</code>)
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder setNotifyListenerByLogMatch(boolean notifyByLogMatch) {
            this.notifyByLogMatch = notifyByLogMatch;
            return this;
        }
        
        /**
         * Sets a termination listener.
         * 
         * @param listener the listener, may be <b>null</b> for none
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder setListener(TerminationListener listener) {
            this.listener = listener;
            return this;
        }
        
        /**
         * Builds the process.
         * 
         * @return the process unit
         * @throws MojoExecutionException if creating the process fails
         */
        public ProcessUnit build4Mvn() throws MojoExecutionException {
            try {
                return build();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        /**
         * Builds the process.
         * 
         * @return the process unit
         * @throws IOException if creating the process fails
         */
        public ProcessUnit build() throws IOException {
            String info = "";
            if (null != argAggregate) {
                args.add(argAggregateStart + argAggregate + argAggregateEnd);
                argAggregate = null;
                argAggregateStart = null;
                argAggregateEnd = null;
            }
            ProcessBuilder builder = new ProcessBuilder(args);
            if (null != home) {
                builder.directory(home);
                info =  " in " + home;
            }
            String path = JavaUtils.getJavaPath();
            if (null != path && null != getMavenBinPath()) {
                path += File.pathSeparator + getMavenBinPath();
            }
            if (null != path) {
                if (builder.environment().get("PATH") != null) {
                    path += File.pathSeparator + builder.environment().get("PATH");
                }
                builder.environment().put("PATH", path); // scripts are started through shell
                info += " with " + path + " in PATH";
            }
            logger.info("Starting " + CollectionUtils.toStringSpaceSeparated(args) + info);
            Process proc = builder.start();
            ProcessUnit result = new ProcessUnit(description, proc, timeout, listener, checkRegEx, logger);
            if (null != proc) {
                Consumer<String> inConsumer = null;
                Consumer<String> errConsumer = null;
                if (null != logFile) {
                    try {
                        PrintWriter logWriter = new PrintWriter(new FileWriter(logFile));
                        inConsumer = l -> logWriter.println(l);
                        errConsumer = inConsumer;
                        result.attach(logWriter);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
                if (null == inConsumer) {
                    inConsumer = l -> logger.info(l);
                }
                if (null == errConsumer) {
                    if (err2In) {
                        errConsumer = l -> logger.info(l);
                    } else {
                        errConsumer = l -> logger.error(l);
                    }
                }
                Consumer<Pattern> matchConsumer;
                if (conjunctLogMatches) {
                    matchConsumer = new ConjunctiveLogRegExConsumer(checkRegEx, result, notifyByLogMatch); 
                } else {
                    matchConsumer = m -> result.notifyLogMatches(notifyByLogMatch);
                }
                result.attach(new InputStreamHandler(proc.getInputStream(), inConsumer, checkRegEx, matchConsumer));
                result.attach(new InputStreamHandler(proc.getErrorStream(), errConsumer, checkRegEx, matchConsumer));
            }
            return result;
        }
        
    }
    
    /**
     * Splits the OS path into path elements.
     * 
     * @param pathEltConsumer the element consumer
     */
    private static void splitPath(Consumer<String> pathEltConsumer) {
        String path = System.getProperty("java.library.path", "");
        StringTokenizer tokens = new StringTokenizer(path, File.pathSeparator);
        while (tokens.hasMoreTokens()) {
            pathEltConsumer.accept(tokens.nextToken());
        }
    }    
    
    /**
     * Consumes matching log regEx until all required are matched. Informs then the associated process unit.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ConjunctiveLogRegExConsumer implements Consumer<Pattern> {

        private List<Pattern> requiredPatterns;
        private Set<Pattern> patterns;
        private ProcessUnit unit;
        private boolean notifyByLogMatch;
        
        /**
         * Creates a consumer.
         * 
         * @param patterns the patterns that must be matched; may be <b>null</b> or empty for any, i.e., {@code unit} 
         *     will never be informed then 
         * @param unit the process unit instance
         * @param notifyByLogMatch whether the listener in {@code unit} shall be informed on a conjunctive match
         */
        ConjunctiveLogRegExConsumer(List<Pattern> patterns, ProcessUnit unit, boolean notifyByLogMatch) {
            this.unit = unit;
            this.notifyByLogMatch = notifyByLogMatch;
            if (patterns != null && patterns.size() > 0) {
                this.requiredPatterns = Collections.unmodifiableList(patterns);
                this.patterns = new HashSet<>();
            }
        }
        
        @Override
        public void accept(Pattern pattern) {
            if (patterns != null) {
                patterns.remove(pattern);
                if (patterns.isEmpty()) {
                    unit.notifyLogMatches(notifyByLogMatch);
                    patterns.addAll(requiredPatterns); // reset for next round
                }
            }
        }
        
    }

    /**
     * Stops the process.
     * 
     * @return the {@link ProcessUnit#getExitValue() exit status}
     */
    public synchronized int stop() {
        if (null != timeoutTask) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
        if (null != process) {
            logger.info("Stopping process '" + description + "'");
            // since Java 9
            ProcessHandle
                .of(process.pid())
                .ifPresent(p -> p.descendants()
                    .forEach(ProcessHandle::destroyForcibly));
            process.destroyForcibly();
            process = null; // prevent wrong exit status
        }
        if (null != closeables) {
            for (Closeable c : closeables) {
                try {
                    c.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            closeables = null;
        }
        return getExitValue();
    }
    
    /**
     * Handles an input stream.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class InputStreamHandler implements Runnable, Closeable {
        
        private InputStream in;
        private Consumer<String> logger;
        private List<Pattern> checkRegEx;
        private Consumer<Pattern> checkResultConsumer;

        /**
         * Creates an input stream handler.
         * 
         * @param in the input stream
         * @param logger the logging consumer
         * @param checkRegEx log checking regular expressions, may be <b>null</b> for none
         * @param checkResultConsumer consumes matching results
         */
        private InputStreamHandler(InputStream in, Consumer<String> logger, List<Pattern> checkRegEx, 
            Consumer<Pattern> checkResultConsumer) {
            this.in = in;
            this.logger = logger;
            this.checkRegEx = checkRegEx;
            this.checkResultConsumer = checkResultConsumer;
        }
        
        @Override
        public void run() {
            Scanner sc = new Scanner(in);
            while (null != logger && sc.hasNextLine()) {
                String line = sc.nextLine();
                if (null != line) {
                    if (null != logger) {
                        logger.accept(line);
                    }
                    if (null != checkRegEx) {
                        for (Pattern p : checkRegEx) {
                            if (p.matcher(line).matches()) {
                                checkResultConsumer.accept(p);
                            }
                        }
                    }
                }
            }
            sc.close();
        }

        @Override
        public void close() throws IOException {
            logger = null;
        }
        
    }
    
}
