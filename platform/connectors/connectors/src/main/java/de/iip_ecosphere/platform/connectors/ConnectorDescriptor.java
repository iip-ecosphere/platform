/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * Describes a connector without instantiating it. Required to use the Java services mechanism to
 * silently register connector classes with the {@link ConnectorRegistry}. Per connector type 
 * ({@link #getConnectorType()}) there shall be only a single descriptor (instance)!
 */
public interface ConnectorDescriptor {
    
    public static final String PLUGIN_ID_PREFIX = "connector-";
    public static final String PLUGIN_TEST_ID_PREFIX = PLUGIN_ID_PREFIX + "test-";

    /**
     * Returns the name of the connector.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * Returns the type of the connector.
     * 
     * @return the type
     */
    public Class<?> getConnectorType();

    /**
     * Creates a connector instance.
     * 
     * @param <O> the output type from the underlying machine/platform/external sinksource
     * @param <I> the input type to the underlying machine/platform/external sinksource
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @param <A> the protocol adapter type
     * @param params the connector parameters supplier
     * @param adapter the protocol adapters to create the connector for
     * @return the created connector
     */
    @SuppressWarnings("unchecked")
    public default <O, I, CO, CI, A extends ProtocolAdapter<O, I, CO, CI>> 
        Connector<O, I, CO, CI> createConnector(Supplier<ConnectorParameter> params, A... adapter) {
        return createConnector(null, params, adapter);
    }
    
    /**
     * Creates a connector instance.
     * 
     * @param <O> the output type from the underlying machine/platform/external sinksource
     * @param <I> the input type to the underlying machine/platform/external sinksource
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @param <S> the protocol selector type
     * @param <A> the protocol adapter type
     * @param selector the protocol selector, may be <b>null</b> for none
     * @param params the connector parameters supplier
     * @param adapter the protocol adapters to create the connector for
     * @return the created connector
     */
    @SuppressWarnings("unchecked")
    public <O, I, CO, CI, S extends AdapterSelector <O, I, CO, CI>, A extends ProtocolAdapter<O, I, CO, CI>> 
        Connector<O, I, CO, CI> createConnector(S selector, Supplier<ConnectorParameter> params, A... adapter);
    
}