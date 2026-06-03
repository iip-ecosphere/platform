/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.mqttv5;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.mqttv5.PahoMqttv5Connector;
import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.IdentityTokenBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.StringSerializer;

/**
 * Simple static connection exercise test. No regression test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectTest {
    
    /**
     * Runs the test.
     * 
     * @param args ignored
     * @throws IOException if connecting to the broker fails
     */
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int port = 1883;
        String outChannel = "shellypro3em-NEF400/events/rpc";
        String inChannel = "shellypro3em-NEF400/events/rpc";
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        Connector<byte[], byte[], String, String> connector = new PahoMqttv5Connector<>(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], String, String>(
                outChannel,   new ConnectorOutputTypeAdapter<String>(new StringSerializer()), 
                inChannel, new ConnectorInputTypeAdapter<String>(new StringSerializer())));
        Map<String, IdentityToken> identities = new HashMap<>();
        identities.put(ConnectorParameter.ANY_ENDPOINT, IdentityTokenBuilder.newBuilder()
            .setUsernameToken("user", "user".getBytes(), IdentityToken.ENC_PLAIN_UTF_8)
            .build());
        ConnectorParameter params = ConnectorParameter.ConnectorParameterBuilder.newBuilder(host, port)
            .setIdentities(identities)
            .build();
        connector.setReceptionCallbackSafe(new AbstractReceptionCallback<String>(String.class) {
            
            @Override
            public void received(String data) {
                System.out.println("RECEIVED " + data);
            }
            
        });
        connector.connect(params);
        while (true) {
            TimeUtils.sleep(500);
        }
    }

}
