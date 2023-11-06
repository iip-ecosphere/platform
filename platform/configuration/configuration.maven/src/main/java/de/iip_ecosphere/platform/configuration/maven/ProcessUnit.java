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
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.CollectionUtils;

/**
 * Represents a process.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessUnit {
    
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
                    if (null != listener) {
                        listener.notifyTermination(TerminationReason.TIMEOUT);
                    }
                    stop();
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
        TIMEOUT,
        
        /**
         * Termination when all required log regular expressions are matched.
         */
        MATCH_COMPLETE
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
         */
        public void notifyTermination(TerminationReason reason);

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
            if (null != listener) {
                listener.notifyTermination(TerminationReason.MATCH_COMPLETE);
            }
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
    public static class ProcessUnitBuilder implements Builder<ProcessUnit> {
        
        private String description;
        private List<String> args = new ArrayList<>();
        private File home;
        private Logger logger;
        private long timeout;
        private File logFile;
        private List<Pattern> checkRegEx;
        private TerminationListener listener;
        private boolean terminateByLogMatch = true;
        private boolean conjunctLogMatches = true;
        private String argAggregate;
        private String argAggregateStart;
        private String argAggregateEnd;

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
         * Adds the maven command (shall be used for arguments).
         * 
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addMavenCommand() {
            if (SystemUtils.IS_OS_WINDOWS) {
                addArguments(ProcessUnit.WIN_BAT_PREFIX);
                enableArgumentAggregation();
            }
            addArgument("mvn");
            return this;
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
         * Adds {@code cmd} is a script (also if in {@link ProcessUnit#SCRIPT_NAMES}) 
         * or as argument.
         * 
         * @param cmdAsScript is {@code cmd} is a script command
         * @param cmd the script name
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder addArgumentOrScriptCommand(boolean cmdAsScript, String cmd) {
            if (cmdAsScript || SCRIPT_NAMES.contains(cmd)) {
                addShellScriptCommand(cmd);
            } else {
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
         * @param arguments the arguments
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
         * Sets whether the process shall be terminated if a log regEx matches. Then no exit status 
         * of the process will be returned.
         * 
         * @param terminateByLogMatch terminate the process by a log match or not (default is <code>true</code>)
         * @return <b>this</b> (builder style)
         */
        public ProcessUnitBuilder setTerminateByLogMatch(boolean terminateByLogMatch) {
            this.terminateByLogMatch = terminateByLogMatch;
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

        @Override
        public ProcessUnit build() {
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
            logger.info("Starting " + CollectionUtils.toStringSpaceSeparated(args) + info);
            Process proc;
            try {
                proc = builder.start();
            } catch (IOException e) {
                proc = null;
                logger.error(e);
            }
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
                    errConsumer = l -> logger.error(l);
                }
                Consumer<Pattern> matchConsumer;
                if (conjunctLogMatches) {
                    matchConsumer = new ConjunctiveLogRegExConsumer(checkRegEx, result, terminateByLogMatch); 
                } else {
                    matchConsumer = m -> result.notifyLogMatches(terminateByLogMatch);
                }
                result.attach(new InputStreamHandler(proc.getInputStream(), inConsumer, checkRegEx, matchConsumer));
                result.attach(new InputStreamHandler(proc.getErrorStream(), errConsumer, checkRegEx, matchConsumer));
            }
            return result;
        }
        
    }
    
    /**
     * Consumes matching log regEx until all required are matched. Informs then the associated process unit.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ConjunctiveLogRegExConsumer implements Consumer<Pattern> {

        private Set<Pattern> patterns;
        private ProcessUnit unit;
        private boolean terminateByLogMatch;
        
        /**
         * Creates a consumer.
         * 
         * @param patterns the patterns that must be matched; may be <b>null</b> or empty for any, i.e., {@code unit} 
         *     will never be informed then 
         * @param unit the process unit instance
         * @param terminateByLogMatch whether the process in {@code unit} shall be terminated on a conjunctive match
         */
        ConjunctiveLogRegExConsumer(List<Pattern> patterns, ProcessUnit unit, boolean terminateByLogMatch) {
            this.unit = unit;
            this.terminateByLogMatch = terminateByLogMatch;
            if (patterns != null && patterns.size() > 0) {
                this.patterns = new HashSet<>();
                this.patterns.addAll(patterns);
            }
        }
        
        @Override
        public void accept(Pattern pattern) {
            if (patterns != null) {
                patterns.remove(pattern);
                if (patterns.isEmpty()) {
                    unit.notifyLogMatches(terminateByLogMatch);
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
    
    /**
     * Simple logger interface to avoid maven classes in here.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Logger {

        /**
         * Logs a warning.
         * 
         * @param warning the warning
         */
        public void warn(String warning);

        /**
         * Logs an error.
         * 
         * @param error the error
         */
        public void error(String error);

        /**
         * Logs a throwable.
         * 
         * @param throwable the throwable
         */
        public void error(Throwable throwable);

        /**
         * Logs an information.
         * 
         * @param info the information
         */
        public void info(String info);

    }
    
    /**
     * Simple logger implementation, e.g., for tests.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class StandardLogger implements Logger {

        @Override
        public void warn(String warning) {
            System.out.println("[WARN] " + warning);
        }

        @Override
        public void error(String error) {
            System.err.println("[ERROR] " + error);
        }

        @Override
        public void error(Throwable throwable) {
            System.err.println("[ERROR] " + throwable.getMessage());
        }

        @Override
        public void info(String info) {
            System.out.println("[INFO] " + info);
        }
        
    }

}
