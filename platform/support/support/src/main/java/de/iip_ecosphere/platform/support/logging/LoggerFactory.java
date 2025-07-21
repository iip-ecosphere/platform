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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Creates loggers. By default, provides a fallback logger implementation. The format for parameterized log messages
 * is that of SLF4J, i.e., {} in the message are substituted in the order of given arguments.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class LoggerFactory {
    
    private static final LogLevel LEVEL = LogLevel.getDefault();
    private static Map<String, Logger> loggers = Collections.synchronizedMap(new HashMap<>());
    private static final ILoggerFactory FALLBACK_FACTORY = new ILoggerFactory() {

        @Override
        public Logger createLogger(String name) {
            Logger logger = new FallbackLogger(name);
            logger.setLevel(LEVEL);
            return logger;
        }
        
    };
    private static ILoggerFactory factory = FALLBACK_FACTORY;

    static {
        considerPlugin();
    }
    
    /**
     * Loads the logger factory plugin if present.
     */
    public static void considerPlugin() {
        ILoggerFactory f = PluginManager.getPluginInstance(ILoggerFactory.class, LoggerFactoryDescriptor.class);
        if (null != f) { // be careful
            factory = f;
            loggers.clear(); // reset
            System.out.println("Using logger factory " + factory.getClass().getName());
        }
    }

    /**
     * Changes the logger factory.
     * 
     * @param fcty the new factory, ignored if <b>null</b>
     */
    public static void setLoggerFactory(ILoggerFactory fcty) {
        if (null != fcty) {
            factory = fcty;
        }
    }
    
    /**
     * Return a logger named according to the name parameter using the
     * statically bound {@link ILoggerFactory} instance.
     * 
     * @param name
     *            The name of the logger.
     * @return logger
     */
    public static Logger getLogger(String name) {
        if (null == name) {
            name = Logger.ROOT_LOGGER_NAME;
        }
        Logger result = loggers.get(name);
        if (null == result) {
            result = factory.createLogger(name);
            loggers.put(name, result);
        }
        return result;
    }

    /**
     * Return a logger named corresponding to the class passed as parameter.
     * 
     * @param clas the returned logger will be named after clazz
     * @return the logger instance
     */
    public static Logger getLogger(Class<?> cls) {
        return getLogger(cls == null ? null : cls.getName());
    }

    /**
     * Return a logger named corresponding to the object passed as parameter. [convenience]
     * 
     * @param object the object the logger will be named after; if {@code object} is a class, then the object is 
     *     directly passed to {@link #getLogger(Class)}, else if not <b>null</b> the class of object is taken
     * @return the logger instance
     */
    public static Logger getLogger(Object object) {
        Class<?> cls;
        if (object instanceof Class) {
            cls = (Class<?>) object;
        } else {
            cls = null == object ? null : object.getClass();
        }
        return getLogger(cls);
    }

}
