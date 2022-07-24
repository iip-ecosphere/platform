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

package test.de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import java.io.IOException;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector;

/**
 * Simple consumer program for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Consumer {

    /**
     * Starts the consumer.
     * 
     * @param args ignored
     * @throws IOException if any I/O problem occurs
     */
    public static void main(String[] args) throws IOException {
        RabbitMqAmqpTransportConnector conn = new RabbitMqAmqpTransportConnector();
        TransportParameter param = TransportParameter.TransportParameterBuilder
            .newBuilder("localhost", 8883)
            .setAuthenticationKey("amqp")
            .build();
        conn.connect(param);
        conn.setReceptionCallback("TEST", new ReceptionCallback<String>() {

            @Override
            public void received(String data) {
                System.out.println("RCV " + data);
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                conn.disconnect();
            } catch (IOException e) {
            }
        }));
        System.out.println("Consumer running");
        while (true) {
            TimeUtils.sleep(500);
        }
    }

}
