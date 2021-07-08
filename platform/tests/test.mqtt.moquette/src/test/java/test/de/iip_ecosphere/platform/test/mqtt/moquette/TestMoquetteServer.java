/********************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.test.mqtt.moquette;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * A simple embedded Moquette-based MQTT test server for testing/experiments. This class works with Java 8.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestMoquetteServer extends AbstractTestServer {
    
    private io.moquette.broker.Server mqttBroker;
    private ServerAddress addr;

    /**
     * Creates the server instance.
     * 
     * @param addr the server address (schema is ignored)
     */
    public TestMoquetteServer(ServerAddress addr) {
        this.addr = addr;
    }
    
    @Override
    public Server start() {
        if (null == mqttBroker) {
            //File hiveTmp = FileUtils.createTmpFolder("moquette_v3");

            Properties properties = new Properties();
            properties.setProperty("port", String.valueOf(addr.getPort()));
            properties.setProperty("host", addr.getHost());
            properties.setProperty("allow_anonymous", "true");
            
            mqttBroker = new io.moquette.broker.Server();
            try {
                mqttBroker.startServer(properties);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error(e.getMessage());
                mqttBroker = null;
            }
        }
        return this;
    }
    
    @Override
    public void stop(boolean dispose) {
        mqttBroker.stopServer();
        mqttBroker = null;
    }

    /**
     * Starts the server from the command line.
     * 
     * @param args the first argument may be the port number, else 8883 is used
     */
    public static void main(String[] args) {
        TestMoquetteServer server = new TestMoquetteServer(new ServerAddress(Schema.IGNORE, getInteger(args, 8883)));
        server.start();
    }

}
