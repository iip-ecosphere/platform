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

package de.iip_ecosphere.platform.connectors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * Creates a single connector instance or allows to dynamically choose among multiple connector
 * types of the same kind. Implementations must have a publicly accessible no-arg constructor.
 * 
 * @author Holger Eichelberger, SSE
 *
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <A> the protocol adapter type
 */
public interface ConnectorFactory<O, I, CO, CI, A extends ProtocolAdapter<O, I, CO, CI>> {

    /**
     * Creates a connector based on the given connector parameters.
     * 
     * @param params the parameters
     * @param adapter the protocol adapters to create the connector for
     * @return the connector
     */
    @SuppressWarnings("unchecked")
    public Connector<O, I, CO, CI> createConnector(ConnectorParameter params, 
        A... adapter);

    /**
     * Returns whether connector parameter is given and has a service.
     * 
     * @param params the parameters
     * @return {@code true} for service, {@code false else}
     */
    public static boolean hasService(ConnectorParameter params) {
        return null != params && params.getService() != null;
    }

    /**
     * Returns whether connector parameter is given and has service containing a version
     * with version number.
     * 
     * @param params the parameters
     * @return {@code true} for version, {@code false else}
     */
    public static boolean hasVersion(ConnectorParameter params) {
        return hasService(params)
            && params.getService().getVersion() != null 
            && params.getService().getVersion().getSegmentCount() > 0;
    }
    
    /**
     * Creates a connector instance.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @param <A> the adapter type
     * @param cls the class of the connector or of the connector factory indirectly creating 
     *    the connector. Connectors must have at least a one-arg public constructor taking {@code adapters}
     * @param params the connector parameters supplier
     * @param adapter the protocol adapters to create the connector for
     * @return the connector instance or <b>null</b> if none can be created
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <O, I, CO, CI, A extends ProtocolAdapter<O, I, CO, CI>> 
        Connector<O, I, CO, CI> createConnector(String cls, Supplier<ConnectorParameter> params, A... adapter) {
        Connector<O, I, CO, CI> result = null;
        try {
            Class<?> fClass = Class.forName(cls);
            if (ConnectorFactory.class.isAssignableFrom(fClass)) {
                ConnectorFactory<O, I, CO, CI, A> factory = 
                    (ConnectorFactory<O, I, CO, CI, A>) fClass.getConstructor().newInstance();
                result = factory.createConnector(params.get(), adapter);
            } else if (Connector.class.isAssignableFrom(fClass)) {
                Constructor<?> cons = null;
                for (Constructor<?> c: fClass.getDeclaredConstructors()) {
                    if (1 == c.getParameterCount()) {
                        Class<?> p = c.getParameters()[0].getType();
                        if (p.isArray()) { // array type
                            cons = c;
                        }
                    }
                }
                if (null == cons) {
                    throw new NoSuchMethodException();
                }
                result = (Connector<O, I, CO, CI>) cons.newInstance((Object) adapter);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException 
            | IllegalAccessException | InstantiationException e) {
            LoggerFactory.getLogger(ConnectorFactory.class).error("Cannot create connector/factory {}: {}", 
                cls, e.getMessage());
        }
        
        return result;
    }
    
}
