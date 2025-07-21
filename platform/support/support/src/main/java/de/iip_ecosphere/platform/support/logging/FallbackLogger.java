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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * A fallback logger to sysout/syserr.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FallbackLogger implements Logger {

    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final String PLACEHOLDER = "{}";
    
    private String name;
    private LogLevel level;
    
    /**
     * Creates an instance.
     * 
     * @param name the name of the logger
     */
    FallbackLogger(String name) {
        this.name = name;
    }
    
    @Override
    public boolean setLevel(LogLevel level) {
        this.level = level;
        return true;
    }

    @Override
    public void trace(String msg) {
        log(LogLevel.TRACE, msg, null, System.out);
    }

    @Override
    public void trace(String format, Object arg) {
        logArgs(LogLevel.TRACE, format, System.out, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logArgs(LogLevel.TRACE, format, System.out, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logArgs(LogLevel.TRACE, format, System.out, arguments);
    }

    @Override
    public void trace(String msg, Throwable th) {
        log(LogLevel.TRACE, msg, th, System.out);
    }

    @Override
    public void debug(String msg) {
        log(LogLevel.DEBUG, msg, null, System.out);
    }

    @Override
    public void debug(String format, Object arg) {
        logArgs(LogLevel.DEBUG, format, System.out, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logArgs(LogLevel.DEBUG, format, System.out, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logArgs(LogLevel.DEBUG, format, System.out, arguments);
    }

    @Override
    public void debug(String msg, Throwable th) {
        log(LogLevel.DEBUG, msg, th, System.out);
    }

    @Override
    public void info(String msg) {
        log(LogLevel.INFO, msg, null, System.out);
    }

    @Override
    public void info(String format, Object arg) {
        logArgs(LogLevel.INFO, format, System.out, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logArgs(LogLevel.INFO, format, System.out, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logArgs(LogLevel.INFO, format, System.out, arguments);
    }

    @Override
    public void info(String msg, Throwable th) {
        log(LogLevel.INFO, msg, th, System.out);
    }

    @Override
    public void warn(String msg) {
        log(LogLevel.WARN, msg, null, System.out);
    }

    @Override
    public void warn(String format, Object arg) {
        logArgs(LogLevel.WARN, format, System.out, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logArgs(LogLevel.WARN, format, System.out, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logArgs(LogLevel.WARN, format, System.out, arguments);
    }

    @Override
    public void warn(String msg, Throwable th) {
        log(LogLevel.WARN, msg, th, System.out);
    }

    @Override
    public void error(String msg) {
        log(LogLevel.ERROR, msg, null, System.err);
    }

    @Override
    public void error(String format, Object arg) {
        logArgs(LogLevel.ERROR, format, System.err, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logArgs(LogLevel.ERROR, format, System.err, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logArgs(LogLevel.ERROR, format, System.err, arguments);
    }

    @Override
    public void error(String msg, Throwable th) {
        log(LogLevel.ERROR, msg, th, System.err);
    }

    /**
     * Returns whether the given level is enabled.
     * 
     * @param level the target logging level
     * @return {@code true} for enabled, {@code false} else
     */
    private boolean isEnabled(LogLevel level) {
        return level.isEnabled(this.level);
    }
    
    /**
     * Emits the given message {@code msg} with no argument, possibly with the given throwable {@code th}.
     * 
     * @param level the target loglevel
     * @param msg the message, logged is it is, prefixed with logger name, logger level and time
     * @param th optional throwable to be logged, may be <b>null</b>
     * @param out the target output stream
     */
    private void emit(LogLevel level, String msg, Throwable th, PrintStream out) {
        out.print(DATEFORMAT.format(new Date()));
        out.print(" [");
        out.print(Thread.currentThread().getName());
        out.print("] ");
        out.print(level);
        out.print(" ");
        out.print(abbreviate(name));
        out.print(" - ");
        out.println(msg);
        if (null != th) {
            th.printStackTrace(out);
        }
    }
    

    /**
     * Logs the given message {@code msg} with no argument, possibly with the given throwable {@code th} if 
     * {@code level} is enabled.
     * 
     * @param level the target loglevel
     * @param msg the message, logged is it is, prefixed with logger name, logger level and time
     * @param th optional throwable to be logged, may be <b>null</b>
     * @param out the target output stream
     */
    private void log(LogLevel level, String msg, Throwable th, PrintStream out) {
        if (isEnabled(level)) {
            emit(level, msg, th, out);
        }
    }
    
    /**
     * Abbreviates a qualified class name.
     * 
     * @param name the name
     * @return the abbreviated name
     */
    public static String abbreviate(String name) {
        StringBuilder result = new StringBuilder(name);
        int startPos = 1;
        while (startPos < result.length()) {
            int dotPos = result.indexOf(".", startPos);
            if (dotPos > 0) {
                if (dotPos - 1 >= startPos) {
                    result.delete(startPos, dotPos);
                }
            } else {
                break;
            }
            startPos += 2; // . and next
        }
        return result.toString();
    }
    
    /**
     * Turns an argument object to a string value.
     * 
     * @param arg the argument, may be <b>null</b>
     * @return the string value
     */
    private String toString(Object arg) {
        return null == arg ? "null" : arg.toString();
    }

    /**
     * Logs with one argument if {@code level} is enabled.
     * 
     * @param level the target loglevel
     * @param format the logging format, placeholders as {@value #PLACEHOLDER}
     * @param out the target output stream
     * @param arg the argument
     */
    private void logArgs(LogLevel level, String format, PrintStream out, Object arg) {
        if (isEnabled(level)) {
            String msg = StringUtils.replaceOnce(format, PLACEHOLDER, toString(arg));
            log(level, msg, null, out);
        }
    }

    /**
     * Logs with two arguments if {@code level} is enabled.
     * 
     * @param level the target loglevel
     * @param format the logging format, placeholders as {@value #PLACEHOLDER}
     * @param out the target output stream
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    private void logArgs(LogLevel level, String format, PrintStream out, Object arg1, Object arg2) {
        if (isEnabled(level)) {
            String msg = StringUtils.replaceOnce(format, PLACEHOLDER, toString(arg1));
            msg = StringUtils.replaceOnce(msg, PLACEHOLDER, toString(arg2));
            log(level, msg, null, out);
        }
    }

    /**
     * Logs with arbitrary arguments if {@code level} is enabled.
     * 
     * @param level the target loglevel
     * @param format the logging format, placeholders as {@value #PLACEHOLDER}
     * @param out the target output stream
     * @param args the arguments
     */
    private void logArgs(LogLevel level, String format, PrintStream out, Object[] args) {
        if (isEnabled(level)) {
            String msg = format;
            for (int a = 0; a < args.length; a++) {
                msg = StringUtils.replaceOnce(msg, PLACEHOLDER, toString(args[a]));
            }
            log(level, msg, null, out);
        }
    }

}
