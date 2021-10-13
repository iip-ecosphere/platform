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

package test.de.iip_ecosphere.platform.connectors.mqttv5;

import java.io.File;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.mqttv5.PahoMqttv5Connector;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnector;
import test.de.iip_ecosphere.platform.transport.Product;
import test.de.iip_ecosphere.platform.connectors.AbstractSerializingConnectorTest;
import test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer;
import test.de.iip_ecosphere.platform.transport.Command;

/**
 * Implements a test for {@link PahoMqttv5Connector}. Data is sent via the test server from a transport connector
 * to the machine connector which mirrors the data and sends it back in another format. Builds up a 
 * {@link TestHiveMqServer} so that the test is self-contained.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttv5ConnectorTest extends AbstractSerializingConnectorTest {

    @Override
    protected Connector<byte[], byte[], Product, Command> createConnector(
        ChannelProtocolAdapter<byte[], byte[], Product, Command> adapter) {
        return new PahoMqttv5Connector<Product, Command>(adapter);
    }

    @Override
    protected TransportConnector createTransportConnector() {
        return new PahoMqttV5TransportConnector();
    }

    @Override
    protected TransportParameterBuilder configureTransportParameter(TransportParameterBuilder builder) {
        return builder.setApplicationId("cl1");
    }

    @Override
    protected Class<? extends ConnectorDescriptor> getConnectorDescriptor() {
        return PahoMqttv5Connector.Descriptor.class;
    }

    @Override
    protected Server createTestServer(ServerAddress addr, File configDir) {
        TestHiveMqServer.setConfigDir(configDir);
        return new TestHiveMqServer(addr);
    }

    @Override
    protected ConnectorParameterConfigurer getConfigurer() {
        return new ConnectorParameterConfigurer() {
            
            @Override
            public File getConfigDir() {
                return new File("./src/test/secCfg");
            }
            
            @Override
            public void configure(ConnectorParameterBuilder builder) {
                builder.setKeystore(new File(getConfigDir(), "client-trust-store.jks"), 
                    TestHiveMqServer.KEYSTORE_PASSWORD);
                builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
            }
            
            @Override
            public void configure(TransportParameterBuilder builder) {
                builder.setKeystore(new File(getConfigDir(), "client-trust-store.jks"), 
                        TestHiveMqServer.KEYSTORE_PASSWORD);
                    builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
            }

        };
    }

    @Override
    protected boolean implementsEncryption() {
        return true;
    }

}
