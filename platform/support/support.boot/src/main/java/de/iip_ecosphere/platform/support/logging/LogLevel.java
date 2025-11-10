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

/**
 * The log levels.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum LogLevel {
    
    ALL,
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF;
    
    /**
     * Returns if this level is enabled if {@code configured} is given
     * as logger level.
     * 
     * @param configured the logger level
     * @return {@code true} for enabled, {@code false} else
     */
    public boolean isEnabled(LogLevel configured) {
        return ordinal() >= configured.ordinal(); 
    }

    /**
     * Returns the default logging level.
     * 
     * @return the default logging level
     */
    public static LogLevel getDefault() {
        LogLevel result = LogLevel.INFO;
        String level = System.getProperty("org.slf4j.simpleLogger.defaultLoggingLevel", result.name());
        try {
            result = LogLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unkown default logging level: " + level);
        }
        return result;
    }
}
