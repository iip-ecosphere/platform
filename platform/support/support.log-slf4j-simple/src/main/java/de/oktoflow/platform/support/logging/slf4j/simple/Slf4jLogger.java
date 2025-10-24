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

package de.oktoflow.platform.support.logging.slf4j.simple;

import de.iip_ecosphere.platform.support.logging.LogLevel;
import de.iip_ecosphere.platform.support.logging.Logger;

/**
 * Delegating SLF4j logger.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Slf4jLogger implements Logger {

    private org.slf4j.Logger logger;
    private LogLevel level = LogLevel.ALL;

    /**
     * Creates a wrapping logger.
     * 
     * @param logger the original logger
     */
    public Slf4jLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public boolean setLevel(LogLevel level) {
        this.level = level;
        return true;
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }
    
    @Override
    public String getName() {
        return logger.getName();
    }

    /**
     * Execute {@code func} if the given level is enabled.
     * 
     * @param level the target logging level
     * @param func the function to execute
     */
    private void ifEnabled(LogLevel level, Runnable func) {
        if (level.isEnabled(this.level)) {
            func.run();
        }
    }    
    
    @Override
    public void trace(String msg) {
        ifEnabled(LogLevel.TRACE, () -> logger.trace(msg));
    }

    @Override
    public void trace(String format, Object arg) {
        ifEnabled(LogLevel.TRACE, () -> logger.trace(format, arg));
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        ifEnabled(LogLevel.TRACE, () -> logger.trace(format, arg1, arg2));
    }

    @Override
    public void trace(String format, Object... arguments) {
        ifEnabled(LogLevel.TRACE, () -> logger.trace(format, arguments));
    }

    @Override
    public void trace(String msg, Throwable th) {
        ifEnabled(LogLevel.TRACE, () -> logger.trace(msg, th));
    }

    @Override
    public void debug(String msg) {
        ifEnabled(LogLevel.DEBUG, () -> logger.debug(msg));
    }

    @Override
    public void debug(String format, Object arg) {
        ifEnabled(LogLevel.DEBUG, () -> logger.debug(format, arg));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        ifEnabled(LogLevel.DEBUG, () -> logger.debug(format, arg1, arg2));
    }

    @Override
    public void debug(String format, Object... arguments) {
        ifEnabled(LogLevel.DEBUG, () -> logger.debug(format, arguments));
    }

    @Override
    public void debug(String msg, Throwable th) {
        ifEnabled(LogLevel.DEBUG, () -> logger.debug(msg, th));
    }

    @Override
    public void info(String msg) {
        ifEnabled(LogLevel.INFO, () -> logger.info(msg));
    }

    @Override
    public void info(String format, Object arg) {
        ifEnabled(LogLevel.INFO, () -> logger.info(format, arg));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        ifEnabled(LogLevel.INFO, () -> logger.info(format, arg1, arg2));
    }

    @Override
    public void info(String format, Object... arguments) {
        ifEnabled(LogLevel.INFO, () -> logger.info(format, arguments));
    }

    @Override
    public void info(String msg, Throwable th) {
        ifEnabled(LogLevel.INFO, () -> logger.info(msg, th));
    }

    @Override
    public void warn(String msg) {
        ifEnabled(LogLevel.WARN, () -> logger.warn(msg));
    }

    @Override
    public void warn(String format, Object arg) {
        ifEnabled(LogLevel.WARN, () -> logger.warn(format, arg));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        ifEnabled(LogLevel.WARN, () -> logger.warn(format, arg1, arg2));
    }

    @Override
    public void warn(String format, Object... arguments) {
        ifEnabled(LogLevel.WARN, () -> logger.warn(format, arguments));
    }

    @Override
    public void warn(String msg, Throwable th) {
        ifEnabled(LogLevel.WARN, () -> logger.warn(msg, th));
    }

    @Override
    public void error(String msg) {
        ifEnabled(LogLevel.ERROR, () -> logger.error(msg));
    }

    @Override
    public void error(String format, Object arg) {
        ifEnabled(LogLevel.ERROR, () -> logger.error(format, arg));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        ifEnabled(LogLevel.ERROR, () -> logger.error(format, arg1, arg2));
    }

    @Override
    public void error(String format, Object... arguments) {
        ifEnabled(LogLevel.ERROR, () -> logger.error(format, arguments));
    }

    @Override
    public void error(String msg, Throwable th) {
        ifEnabled(LogLevel.ERROR, () -> logger.error(msg, th));
    }

}
