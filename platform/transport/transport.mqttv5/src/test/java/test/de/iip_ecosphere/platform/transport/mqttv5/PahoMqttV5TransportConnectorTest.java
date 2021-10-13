/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.transport.mqttv5;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnector;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.transport.ProductProtobufSerializer;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;
import test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer;

/**
 * Tests the {@link PahoMqttV5TransportConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV5TransportConnectorTest {

    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test(timeout = 180 * 1000)
    public void testPahoConnector() throws IOException {
        TestHiveMqServer.setConfigDir(null);
        doTest(null);
    }

    /**
     * Tests the TLS connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test(timeout = 180 * 1000)
    public void testPahoTlsConnector() throws IOException {
        File secCfg = new File("./src/test/secCfg");
        TestHiveMqServer.setConfigDir(secCfg);
        doTest(new TransportParameterConfigurer() {
            
            @Override
            public void configure(TransportParameterBuilder builder) {
                builder.setKeystore(new File(secCfg, "client-trust-store.jks"), TestHiveMqServer.KEYSTORE_PASSWORD);
                builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
            }
        });
    }

    
    /**
     * Performs the test.
     * 
     * @param configurer the test configurer
     * @throws IOException in case that connection/communication fails
     */
    private void doTest(TransportParameterConfigurer configurer) throws IOException {
        Assert.assertEquals(PahoMqttV5TransportConnector.NAME, TransportFactory.getConnectorName());
        ServerAddress addr = new ServerAddress(Schema.IGNORE); // localhost, ephemeral port
        TestHiveMqServer server = new TestHiveMqServer(addr);
        server.start();
        AbstractTransportConnectorTest.doTest(addr, ProductJsonSerializer.class, configurer);
        AbstractTransportConnectorTest.doTest(addr, ProductProtobufSerializer.class, configurer);
        server.stop(true);
    }

}
