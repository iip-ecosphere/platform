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

package de.iip_ecosphere.platform.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * This is a poor man's fallback logger. Why not just a default implementation of SL4J. Because the logging 
 * implementation shall only be added during integration (architectural constraint C10), we may end up running the 
 * platform instantiator without any logging (as production code and we are not permitted to add a logging 
 * implementation). If there is a logging implementation at runtime, fine. If there is none, EASy will not complain 
 * about any error, not so good. This is the case for this poor man's logger, which is instantiated and used in the
 * platform instantiator/lifecycle descriptor if there is no logging implementation (and the default one would disable 
 * all logging). 
 * 
 * While it delegates all logging to some rather simple (formatting) function, it completely ignores markers and 
 * cause throwables. If somebody needs this, please add. In it's current version, this logger is not intended for reuse,
 * but who knows the future.
 * 
 * Public for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FallbackLogger implements Logger {

    private LoggingLevel level;
        
    /**
     * Defines the basic logging levels in here.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum LoggingLevel {
        DEBUG,
        INFO, 
        WARN, 
        ERROR,
        TRACE,
        OFF
    }

    /**
     * Creates a logger with logging level.
     * 
     * @param level the level to use
     */
    public FallbackLogger(LoggingLevel level) {
        this.level = level;
    }
    
    /**
     * Creates a logger instance if {@code logger} is <b>null</b>. This may be an official one for {@code cls}
     * or, if only disabled default loggers are available, a fallback logger with given {@code fallbackLevek}.
     * 
     * @param logger the actual logger, may be <b>null</b> for none
     * @param cls the class to return an official logger for
     * @param fallbackLevel if there is no configured official logger, return a fallback logger for the given level
     * @return the new logger instance or {@code logger}
     */
    public static Logger getLogger(Logger logger, Class<?> cls, LoggingLevel fallbackLevel) {
        if (null == logger) {
            try {
                Class.forName("org.slf4j.impl.StaticLoggerBinder");
                logger = LoggerFactory.getLogger(cls);
            } catch (ClassNotFoundException e) {
                logger = new FallbackLogger(fallbackLevel);
            }
        }
        return logger;
    }
    
    @Override
    public String getName() {
        return "Simple Fallback Logger";
    }

    /**
     * Emit a message on a given logging level.
     * 
     * @param level the level
     * @param message the message
     */
    private void emit(LoggingLevel level, String message) {
        if (level.ordinal() >= this.level.ordinal()) {
            System.out.println(level + " " + message);
        }
    }

    /**
     * Emit a formatted message on a given logging level.
     * 
     * @param level the level
     * @param format the message to be formatted
     * @param arguments the arguments
     */
    private void emitWithArgs(LoggingLevel level, String format, Object... arguments) {
        if (level.ordinal() >= this.level.ordinal()) {
            String tmp = format;
            for (Object a: arguments) {
                tmp = tmp.replaceFirst("{}", a.toString());
            }
            emit(level, tmp);
        }
    }


    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        emit(LoggingLevel.TRACE, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        emitWithArgs(LoggingLevel.TRACE, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        emitWithArgs(LoggingLevel.TRACE, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable th) {
        emit(LoggingLevel.TRACE, msg);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public void trace(Marker marker, String msg) {
        emit(LoggingLevel.TRACE, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        emitWithArgs(LoggingLevel.TRACE, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        emitWithArgs(LoggingLevel.TRACE, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable th) {
        emit(LoggingLevel.TRACE, msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String msg) {
        emit(LoggingLevel.DEBUG, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        emitWithArgs(LoggingLevel.DEBUG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        emitWithArgs(LoggingLevel.DEBUG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable th) {
        emit(LoggingLevel.DEBUG, msg);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg) {
        emit(LoggingLevel.DEBUG, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        emitWithArgs(LoggingLevel.DEBUG, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        emitWithArgs(LoggingLevel.DEBUG, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable th) {
        emit(LoggingLevel.DEBUG, msg);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        emit(LoggingLevel.INFO, msg);
    }

    @Override
    public void info(String format, Object arg) {
        emitWithArgs(LoggingLevel.INFO, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.INFO, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        emitWithArgs(LoggingLevel.INFO, format, arguments);
    }

    @Override
    public void info(String msg, Throwable th) {
        emit(LoggingLevel.INFO, msg);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(Marker marker, String msg) {
        emit(LoggingLevel.INFO, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        emitWithArgs(LoggingLevel.INFO, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.INFO, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        emitWithArgs(LoggingLevel.INFO, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable th) {
        emit(LoggingLevel.INFO, msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        emit(LoggingLevel.WARN, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        emitWithArgs(LoggingLevel.WARN, format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        emitWithArgs(LoggingLevel.WARN, format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable th) {
        emit(LoggingLevel.WARN, msg);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(Marker marker, String msg) {
        emit(LoggingLevel.WARN, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        emitWithArgs(LoggingLevel.WARN, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        emitWithArgs(LoggingLevel.WARN, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable th) {
        emit(LoggingLevel.WARN, msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        emitWithArgs(LoggingLevel.ERROR, msg);
    }

    @Override
    public void error(String format, Object arg) {
        emitWithArgs(LoggingLevel.ERROR, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        emitWithArgs(LoggingLevel.ERROR, format, arguments);
    }

    @Override
    public void error(String msg, Throwable th) {
        emit(LoggingLevel.ERROR, msg);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(Marker marker, String msg) {
        emit(LoggingLevel.ERROR, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        emitWithArgs(LoggingLevel.ERROR, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        emitWithArgs(LoggingLevel.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        emitWithArgs(LoggingLevel.ERROR, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable th) {
        emit(LoggingLevel.ERROR, msg);
    }

}
