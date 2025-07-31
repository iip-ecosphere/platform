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

package de.iip_ecosphere.platform.connectors;

import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * Basic connector descriptor implementation. Concrete implementations must redefine the plugin id. Delegates the 
 * connector creation to potentially adapted output and input types of a typical connector implementation.
 * 
 * @param <TO> adapted external output type, shall be taken from implementation
 * @param <TI> adapted external input type, shall be taken from implementation
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractPluginChannelConnectorDescriptor<TO, TI> 
    extends SingletonPluginDescriptor<ConnectorDescriptor> implements ConnectorDescriptor {

    /**
     * Creates a descriptor instance. Concrete implementations must redefine the plugin id.
     */
    public AbstractPluginChannelConnectorDescriptor() {
        super("connector", null, ConnectorDescriptor.class, null);
    }
    
    @Override
    protected PluginSupplier<ConnectorDescriptor> initPluginSupplier(
        PluginSupplier<ConnectorDescriptor> pluginSupplier) {
        return p -> this;
    }
    
    @Override
    protected abstract String initId(String id);
    
    @Override
    @SuppressWarnings("unchecked")
    public <O, I, CO, CI, S extends AdapterSelector<O, I, CO, CI>, A extends ProtocolAdapter<O, I, CO, CI>> 
        Connector<O, I, CO, CI> createConnector(S selector, Supplier<ConnectorParameter> params, A... adapter) {
        return (Connector<O, I, CO, CI>) createConnectorImpl((ChannelAdapterSelector<TO, TI, CO, CI>) selector, 
            params, (ChannelProtocolAdapter<TO, TI, CO, CI>[]) adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, I, CO, CI, A extends ProtocolAdapter<O, I, CO, CI>> 
        Connector<O, I, CO, CI> createConnector(Supplier<ConnectorParameter> params, A... adapter) {
        return (Connector<O, I, CO, CI>) createConnectorImpl(params, 
            (ChannelProtocolAdapter<TO, TI, CO, CI>[]) adapter);
    }

    /**
     * Creates a connector instance based on adapted types.
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
    protected <O, I, CO, CI, A extends ChannelProtocolAdapter<TO, TI, CO, CI>> 
        Connector<TO, TI, CO, CI> createConnectorImpl(Supplier<ConnectorParameter> params, A... adapter) {
        return createConnectorImpl(null, params, adapter);
    }

    /**
     * Creates a connector instance based on adapted types.
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
    protected abstract <O, I, CO, CI, S extends ChannelAdapterSelector<TO, TI, CO, CI>, 
        A extends ChannelProtocolAdapter<TO, TI, CO, CI>> 
        Connector<TO, TI, CO, CI> createConnectorImpl(S selector, Supplier<ConnectorParameter> params, A... adapter);
        
}
