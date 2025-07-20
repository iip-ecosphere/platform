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

package de.iip_ecosphere.platform.connectors.events;

import java.lang.reflect.InvocationTargetException;

import de.iip_ecosphere.platform.connectors.parser.InputParser;
import de.iip_ecosphere.platform.support.ClassLoaderUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Connector event utility methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorEventUtils {
    
    /**
     * Convenience method for creating (custom) input handler instances.
     * 
     * @param <T> the data type to be handled by the connector
     * @param loader the class loader to load the class with
     * @param className the name of the parser class (must implement {@link InputParser} and provide a non-argument 
     *     constructor
     * @param type the type to be handled by the connector
     * @return the parser instance (<b>null</b> if the parser cannot be found/initialized)
     */
    @SuppressWarnings("unchecked")
    public static <T> ConnectorInputHandler<T> createInputHandlerInstance(ClassLoader loader, String className, 
        Class<T> type) {
        ConnectorInputHandler<T> result = null;
        try {
            Class<?> handlerClass = loader.loadClass(className);
            Object instance = handlerClass.getDeclaredConstructor().newInstance();
            result = (ConnectorInputHandler<T>) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException 
            | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            String loaders = ClassLoaderUtils.hierarchyToString(loader);
            LoggerFactory.getLogger(ConnectorEventUtils.class).error("Cannot instantiate input handler of type '{} via "
                + "{}': {} {}. Events of type {} will not be handled!", className, loaders, 
                e.getClass().getSimpleName(), e.getMessage(), type.getName());
        }
        return result;
    }

    /**
     * Convenience method for creating (custom) data time difference provider instances.
     * 
     * @param <T> the data type to be handled by the connector
     * @param loader the class loader to load the class with
     * @param className the name of the parser class (must implement {@link InputParser} and provide a non-argument 
     *     constructor
     * @param type the type to be handled by the connector
     * @return the parser instance (<b>null</b> if the parser cannot be found/initialized)
     */
    @SuppressWarnings("unchecked")
    public static <T> DataTimeDifferenceProvider<T> createDataTimeDifferenceProvider(ClassLoader loader, 
        String className, Class<T> type) {
        DataTimeDifferenceProvider<T> result = null;
        try {
            Class<?> handlerClass = loader.loadClass(className);
            Object instance = handlerClass.getDeclaredConstructor().newInstance();
            result = (DataTimeDifferenceProvider<T>) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException 
            | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            String loaders = ClassLoaderUtils.hierarchyToString(loader);
            LoggerFactory.getLogger(ConnectorEventUtils.class).error("Cannot instantiate data time difference provider "
                + "of type '{} via {}': {} {}. Data instances of type {} will not be considered!", className, loaders, 
                e.getClass().getSimpleName(), e.getMessage(), type.getName());
        }
        return result;
    }

}
