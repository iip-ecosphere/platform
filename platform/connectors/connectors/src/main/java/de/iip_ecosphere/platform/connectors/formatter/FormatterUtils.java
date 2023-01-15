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

package de.iip_ecosphere.platform.connectors.formatter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.ClassLoaderUtils;

/**
 * Output formatter utility methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FormatterUtils {
    
    /**
     * Convenience method for creating (custom) formatter instances.
     * 
     * @param loader the class loader to load the class with
     * @param className the name of the formatter class (must implement {@link OutputFormatter} and provide a single 
     *     String constructor taking the charset encoding or as fallback a no-argument constructor)
     * @param charset the name of the charset encoding
     * @return the formatter instance (an instance of {@link DummyFormatter} if the parser cannot be found/initialized)
     */
    public static OutputFormatter<?> createInstance(ClassLoader loader, String className, String charset) {
        OutputFormatter<?> result = null;
        try {
            Class<?> parserClass = loader.loadClass(className);
            Object instance = null;
            try {
                Constructor<?> cons = parserClass.getConstructor(String.class);
                instance = cons.newInstance(charset);
            } catch (NoSuchMethodException e) {
                // see null == instance
            } catch (InvocationTargetException e) {
                LoggerFactory.getLogger(FormatterUtils.class).error("While instantiating " + className + ": " 
                    + e.getMessage() + ", falling back to default constructor");
            }
            if (null == instance) {
                instance = parserClass.getDeclaredConstructor().newInstance();
            }
            result = (OutputFormatter<?>) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException 
            | NoSuchMethodException | InvocationTargetException e) {
            String loaders = ClassLoaderUtils.hierarchyToString(loader);
            LoggerFactory.getLogger(FormatterUtils.class).error("Cannot instantiate formatter of type '" 
                + className + " via " + loaders + "': " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
        if (null == result) {
            LoggerFactory.getLogger(FormatterUtils.class).warn("No formatter instance created. Using " 
                + DummyFormatter.class.getName() + " as fallback");
            result = new DummyFormatter();
        }
        return result;
    }

}
