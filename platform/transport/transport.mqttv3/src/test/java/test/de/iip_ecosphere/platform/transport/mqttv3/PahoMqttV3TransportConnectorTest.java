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
package test.de.iip_ecosphere.platform.transport.mqttv3;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.transport.ProductProtobufSerializer;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;
import test.de.iip_ecosphere.platform.test.mqtt.moquette.TestMoquetteServer;

/**
 * Tests the {@link PahoMqttV3TransportConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV3TransportConnectorTest {

    /**
     * Sets up the tests.
     */
    @BeforeClass
    public static void init() {
        BasicConfigurator.configure();
    }
    
    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testPahoConnector() throws IOException {
        TestMoquetteServer.setConfigDir(null);
        doTest(null);
    }

    /**
     * Tests the TLS connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testPahoTlsConnector() throws IOException {
        File secCfg = new File("./src/test/secCfg");
        TestMoquetteServer.setConfigDir(secCfg);
        doTest(new TransportParameterConfigurer() {
            
            @Override
            public void configure(TransportParameterBuilder builder) {
                builder.setKeystore(new File(secCfg, "keystore.jks"), TestMoquetteServer.KEYSTORE_PASSWORD);
                builder.setKeyAlias(TestMoquetteServer.KEY_ALIAS);
                builder.setActionTimeout(3000); // TLS may take a bit longer, on Jenkins
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
        Assert.assertEquals(PahoMqttV3TransportConnector.NAME, TransportFactory.getConnectorName());
        ServerAddress addr = new ServerAddress(Schema.IGNORE); // localhost, ephemeral
        TestMoquetteServer server = new TestMoquetteServer(addr);
        server.start();
        AbstractTransportConnectorTest.doTest(addr, ProductJsonSerializer.class, configurer);
        AbstractTransportConnectorTest.doTest(addr, ProductProtobufSerializer.class, configurer);
        server.stop(true);
    }

}
