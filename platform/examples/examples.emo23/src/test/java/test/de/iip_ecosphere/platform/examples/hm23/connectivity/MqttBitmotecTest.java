/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.hm23.connectivity;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector;

/**
 * Tests the connection to the Bitmotec MQTT broker.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttBitmotecTest {

    /**
     * Main program.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        // from AppAas
        TransportParameter.TransportParameterBuilder tpBuilder = 
            TransportParameter.TransportParameterBuilder.newBuilder("192.168.2.12", 1883);
        tpBuilder.setAuthenticationKey("onlogic:1883");
        tpBuilder.setHostnameVerification(false);
        TransportParameter params = tpBuilder.build();

        PahoMqttV3TransportConnector conn = new PahoMqttV3TransportConnector();
        conn.connect(params);
        LoggerFactory.getLogger(MqttBitmotecTest.class).info("MQTT-Out connector created");
        conn.asyncSend("iip-test/test", "TEST");
        conn.disconnect();
    }
    
}
