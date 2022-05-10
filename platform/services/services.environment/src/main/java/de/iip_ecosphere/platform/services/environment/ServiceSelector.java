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

package de.iip_ecosphere.platform.services.environment;

import org.slf4j.LoggerFactory;

/**
 * Code plugin to select a service from a given type. To be hooked into through configuration model and code generation.
 * 
 * @param <T> selection input type
 * @author Holger Eichelberger, SSE
 */
public interface ServiceSelector<T> {

    /**
     * Selects a service based on the input.
     * 
     * @param input the input to use
     * @return the service id, <b>null</b> for staying with the actual service
     */
    public String select(T input);

    /**
     * Selects a service based on the input and a qualifier (only used if explicitly configured).
     * 
     * @param input the input to use
     * @param qualifier the qualifier to use, may be a string, a number, whatever defined in the model
     * @return the service id, <b>null</b> for staying with the actual service
     */
    public default String select(T input, Object qualifier) {
        return select(input); 
    }
    
    /**
     * Creates a selector instance with fallback.
     * 
     * @param <T> the input type of the selector
     * @param loader the class loader to use
     * @param cls the class name of the selector to instantiate
     * @param type the input type of the selector
     * @param dflt the default return value if the selector cannot be instantiated
     * @return a (default) selector
     */
    @SuppressWarnings("unchecked")
    public static <T> ServiceSelector<T> createInstance(ClassLoader loader, String cls, Class<T> type, String dflt) {
        ServiceSelector<T> result = null;
        try {
            Class<?> c = loader.loadClass(cls);
            result = (ServiceSelector<T>) c.newInstance();
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(ServiceSelector.class).warn(
                "Cannot load selector {}: Falling back to '{}'", cls, dflt);
        } catch (InstantiationException | IllegalAccessException e) {
            LoggerFactory.getLogger(ServiceSelector.class).warn(
                "Cannot instantiate selector {}: {}. Falling back to '{}'", cls, e.getMessage(), dflt);
        } catch (ClassCastException e) {
            LoggerFactory.getLogger(ServiceSelector.class).warn(
                "Instance of {} is not a selector type. Falling back to '{}'", cls, dflt);
        }
        if (result == null) {
            result = i -> dflt;
        }
        return result;
    }
    
}
