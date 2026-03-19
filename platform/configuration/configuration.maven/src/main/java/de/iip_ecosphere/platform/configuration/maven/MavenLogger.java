/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.maven;

import java.io.PrintStream;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import de.iip_ecosphere.platform.support.logging.FallbackLogger;
import de.iip_ecosphere.platform.support.logging.ILoggerFactory;
import de.iip_ecosphere.platform.support.logging.LogLevel;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A wrapping maven logger mapping oktoflow log calles to maven.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenLogger extends FallbackLogger {
    
    private static Log log;
    
    /**
     * Creates an instance.
     * 
     * @param name the name of the logger
     */
    public MavenLogger(String name) {
        super(name);
        setEmitter((lvl, n, msg, th, out) -> emit(lvl, msg, th, out));
        setLevel(LogLevel.ALL);
    }

    /**
     * Installs the logger, wrapping {@code logger}.
     * 
     * @param logger the logger
     */
    static void install(Log logger) {
        log = logger;
        LoggerFactory.setLoggerFactory(new ILoggerFactory() {

            private Map<String, LogLevel> initialLevels;

            @Override
            public void initialLevels(Map<String, LogLevel> levels) {
                this.initialLevels = levels;
            }
            
            @Override
            public Logger createLogger(String name) {
                MavenLogger result = new MavenLogger(name);
                if (initialLevels != null) {
                    LogLevel initialLevel = initialLevels.get(name);
                    if (null != initialLevel) {
                        result.setLevel(initialLevel);
                    }
                }
                return result;
            }
        });
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
        switch (level) {
        case WARN: 
            log.warn(msg);
            break;
        case TRACE:
        case DEBUG:
            log.debug(msg);
            break;
        case ERROR:
            log.error(msg);
            break;
        case INFO:
            log.info(msg);
            break;
        default:
            break;
        }
    }

}
