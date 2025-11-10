/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.logging;

import java.io.PrintStream;

/**
 * A logger. Basic interface taken over from SLF4j except for markers. The format for parameterized log messages
 * is that of SLF4J, i.e., {} in the message are substituted in the order of given arguments.
 * 
 * @author Holger Eichelberger, SSE
 * @author SLF4J
 */
public interface Logger {
    
    /**
     * Case insensitive String constant used to retrieve the name of the root logger.
     */
    public final String ROOT_LOGGER_NAME = "ROOT";
    
    /**
     * Finally emits the log message.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Emitter {

        /**
         * Finally emits the log message.
         * 
         * @param level the logging level
         * @param name the name of the logger
         * @param msg the logging message
         * @param th the throwable detailing the message (may be <b>null</b>)
         * @param out the target output stream
         */
        public void emit(LogLevel level, String name, String msg, Throwable th, PrintStream out);
        
    }
    
    /**
     * Sets the emitter so that the output format can be changed programmatically. May not be supported by all loggers.
     * 
     * @param emitter the emitter, shall be ignored if <b>null</b>
     * @return {@code true} if accepted, {@code false} if ignored
     */
    public default boolean setEmitter(Emitter emitter) {
        return false;
    }
    
    /**
     * Sets the log level.
     * 
     * @param level the new log level
     * @return {@code true} if successful and changed, {@code false} if not changed or ignored
     */
    public boolean setLevel(LogLevel level);
    
    /**
     * Returns the log level.
     * 
     * @return the log level, may be <b>null</b> if not accessible/unknown
     */
    public LogLevel getLevel();
    
    /**
     * Returns the name of the logger.
     * 
     * @return the name of the logger
     */
    public String getName();
    
    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    public void trace(String msg);

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void trace(String format, Object arg);

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void trace(String format, Object arg1, Object arg2);

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void trace(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param th   the exception (throwable) to log
     */
    public void trace(String msg, Throwable th);

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    public void debug(String msg);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void debug(String format, Object arg);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void debug(String format, Object arg1, Object arg2);

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void debug(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param th   the exception (throwable) to log
     */
    public void debug(String msg, Throwable th);

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    public void info(String msg);

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void info(String format, Object arg);

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void info(String format, Object arg1, Object arg2);

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void info(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param th   the exception (throwable) to log
     */
    public void info(String msg, Throwable th);

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    public void warn(String msg);

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void warn(String format, Object arg);

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void warn(String format, Object arg1, Object arg2);

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void warn(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param th   the exception (throwable) to log
     */
    public void warn(String msg, Throwable th);

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    public void error(String msg);

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void error(String format, Object arg);

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void error(String format, Object arg1, Object arg2);

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void error(String format, Object... arguments);

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param th   the exception (throwable) to log
     */
    public void error(String msg, Throwable th);

    /**
     * Log a message at the given level.
     *
     * @param level the logging level to use ({@link LogLevel#ALL} and {@link LogLevel#OFF} are ignored)
     * @param msg the message string to be logged
     */
    public default void log(LogLevel level, String msg) {
        switch(level) {
        case DEBUG:
            debug(msg);
            break;
        case ERROR:
            error(msg);
            break;
        case INFO:
            info(msg);
            break;
        case TRACE:
            trace(msg);
            break;
        case WARN:
            warn(msg);
            break;
        default:
            // nothing
            break;
        }
    }
    
    /**
     * Log a message at the given level.
     *
     * @param level the logging level to use ({@link LogLevel#ALL} and {@link LogLevel#OFF} are ignored)
     * @param format the format string
     * @param arg    the argument
     */
    public default void log(LogLevel level, String format, Object arg) {
        switch(level) {
        case DEBUG:
            debug(format, arg);
            break;
        case ERROR:
            error(format, arg);
            break;
        case INFO:
            info(format, arg);
            break;
        case TRACE:
            trace(format, arg);
            break;
        case WARN:
            warn(format, arg);
            break;
        default:
            // nothing
            break;
        }
    }
    
    /**
     * Log a message at the given level.
     *
     * @param level the logging level to use ({@link LogLevel#ALL} and {@link LogLevel#OFF} are ignored)
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public default void log(LogLevel level, String format, Object arg1, Object arg2) {
        switch(level) {
        case DEBUG:
            debug(format, arg1, arg2);
            break;
        case ERROR:
            error(format, arg1, arg2);
            break;
        case INFO:
            info(format, arg1, arg2);
            break;
        case TRACE:
            trace(format, arg1, arg2);
            break;
        case WARN:
            warn(format, arg1, arg2);
            break;
        default:
            // nothing
            break;
        }
    }    

    /**
     * Log a message at the given level.
     *
     * @param level the logging level to use ({@link LogLevel#ALL} and {@link LogLevel#OFF} are ignored)
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    public default void log(LogLevel level, String format, Object... arguments) {
        switch(level) {
        case DEBUG:
            debug(format, arguments);
            break;
        case ERROR:
            error(format, arguments);
            break;
        case INFO:
            info(format, arguments);
            break;
        case TRACE:
            trace(format, arguments);
            break;
        case WARN:
            warn(format, arguments);
            break;
        default:
            // nothing
            break;
        }
    }    

    /**
     * Log a message at the given level.
     *
     * @param level the logging level to use ({@link LogLevel#ALL} and {@link LogLevel#OFF} are ignored)
     * @param msg the message accompanying the exception
     * @param th   the exception (throwable) to log
     */
    public default void log(LogLevel level, String msg, Throwable th) {
        switch(level) {
        case DEBUG:
            debug(msg, th);
            break;
        case ERROR:
            error(msg, th);
            break;
        case INFO:
            info(msg, th);
            break;
        case TRACE:
            trace(msg, th);
            break;
        case WARN:
            warn(msg, th);
            break;
        default:
            // nothing
            break;
        }
    }    
    
}
