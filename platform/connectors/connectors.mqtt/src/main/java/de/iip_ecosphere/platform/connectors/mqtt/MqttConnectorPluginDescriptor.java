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

package de.iip_ecosphere.platform.connectors.mqtt;

import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.AbstractPluginChannelConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ChannelAdapterSelector;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;

/**
 * Describes this connector by delegating to the factory.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttConnectorPluginDescriptor extends AbstractPluginChannelConnectorDescriptor<byte[], byte[]> {

    public static final String NAME = "MQTT";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<?> getConnectorType() {
        return ConnectorFactory.class; // happens via factory
    }

    @Override
    protected String initId(String id) {
        return PLUGIN_ID_PREFIX + "mqtt";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <O, I, CO, CI, S extends ChannelAdapterSelector<byte[], byte[], CO, CI>, 
        A extends ChannelProtocolAdapter<byte[], byte[], CO, CI>> Connector<byte[], byte[], CO, CI> createConnectorImpl(
        S selector, Supplier<ConnectorParameter> params, A... adapter) {
        MqttConnectorFactory<CO, CI> factory = new MqttConnectorFactory<CO, CI>();
        return factory.createConnector(params.get(), adapter);
    }

}
