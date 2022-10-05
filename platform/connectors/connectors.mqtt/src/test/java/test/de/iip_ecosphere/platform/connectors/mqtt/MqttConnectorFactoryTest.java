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

package test.de.iip_ecosphere.platform.connectors.mqtt;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.mqtt.MqttConnectorFactory;
import de.iip_ecosphere.platform.connectors.mqttv3.PahoMqttv3Connector;
import de.iip_ecosphere.platform.connectors.mqttv5.PahoMqttv5Connector;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import test.de.iip_ecosphere.platform.connectors.ConnectorFactoryTest;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup.Service;

/**
 * Tests {@link MqttConnectorFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttConnectorFactoryTest {
    
    /**
     * Tests {@link MqttConnectorFactory}.
     */
    @Test
    public void testFactory() {
        ConnectorParameter v0p = ConnectorParameterBuilder.newBuilder("l", 0).build();

        Service s3 = new Service();
        s3.setVersion(new Version(3));
        ConnectorParameter v3p = ConnectorParameterBuilder.newBuilder("l", 0).setService(s3).build();
        
        Service s5 = new Service();
        s5.setVersion(new Version(5));
        ConnectorParameter v5p = ConnectorParameterBuilder.newBuilder("l", 0).setService(s5).build();

        Connector<byte[], byte[], Object, Object> mc = ConnectorFactory.createConnector(
            MqttConnectorFactory.class.getName(), () -> v5p, new ConnectorFactoryTest.MyMqttProtocolAdapter());
        Assert.assertTrue(mc instanceof PahoMqttv5Connector);
        mc = ConnectorFactory.createConnector(
            MqttConnectorFactory.class.getName(), () -> v3p, new ConnectorFactoryTest.MyMqttProtocolAdapter());
        Assert.assertTrue(mc instanceof PahoMqttv3Connector);
        mc = ConnectorFactory.createConnector(
            MqttConnectorFactory.class.getName(), () -> v0p, new ConnectorFactoryTest.MyMqttProtocolAdapter());
        Assert.assertTrue(mc instanceof PahoMqttv3Connector);
    }
    
}
