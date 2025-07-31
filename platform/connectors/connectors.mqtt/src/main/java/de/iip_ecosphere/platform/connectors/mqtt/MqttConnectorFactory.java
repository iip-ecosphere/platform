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

package de.iip_ecosphere.platform.connectors.mqtt;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;

/**
 * The MQTT connector factory.
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 * @author Holger Eichelberger, SSE
 */
public class MqttConnectorFactory<CO, CI> implements ConnectorFactory<byte[], byte[], CO, CI, 
    ChannelProtocolAdapter<byte[], byte[], CO, CI>> {

    @SuppressWarnings("unchecked")
    @Override
    public Connector<byte[], byte[], CO, CI> createConnector(ConnectorParameter params,
        ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        Connector<byte[], byte[], CO, CI> result;
        if (ConnectorFactory.hasVersion(params) && params.getService().getVersion().getSegment(0) == 5) {
            result = ConnectorFactory.createConnectorByPlugin(ConnectorDescriptor.PLUGIN_ID_PREFIX + "mqtt-v5", 
                () -> params, adapter);
        } else {
            result = ConnectorFactory.createConnectorByPlugin(ConnectorDescriptor.PLUGIN_ID_PREFIX + "mqtt-v3", 
                () -> params, adapter);
        }
        return result;
    }

}
