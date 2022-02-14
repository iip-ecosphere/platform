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

package de.iip_ecosphere.platform.connectors.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

/**
 * Input parser utility methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ParserUtils {
    
    /**
     * Convenience method for creating (custom) parser instances.
     * 
     * @param loader the class loader to load the class with
     * @param className the name of the parser class (must implement {@link InputParser} and provide a single String 
     *     constructor taking the charset encoding or as fallback a no-argument constructor)
     * @param charset the name of the charset encoding
     * @return the parser instance (<b>null</b> if the parser cannot be found/initialized)
     */
    public static InputParser<?> createInstance(ClassLoader loader, String className, String charset) {
        InputParser<?> result = null;
        try {
            Class<?> parserClass = loader.loadClass(className);
            Object instance = null;
            try {
                Constructor<?> cons = parserClass.getConstructor(String.class);
                instance = cons.newInstance(charset);
            } catch (NoSuchMethodException e) {
                // see null == instance
            } catch (InvocationTargetException e) {
                LoggerFactory.getLogger(ParserUtils.class).error("While instantiating " + className + ": " 
                    + e.getMessage() + ", falling back to default constructor");
            }
            if (null == instance) {
                instance = parserClass.newInstance();
            }
            result = (InputParser<?>) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            String loaders = "";
            ClassLoader l = loader;
            while (null != l) {
                if (loaders.length() > 0) {
                    loaders += " -> ";
                }
                loaders += l.getClass().getSimpleName();
                l = l.getParent();
            }
            LoggerFactory.getLogger(ParserUtils.class).error("Cannot instantiate parser of type '" 
                + className + " via " + loaders + "': " + e.getClass().getSimpleName() + " " + e.getMessage() 
                + ". No input parser will be used!");
        }
        return result;
    }

}
