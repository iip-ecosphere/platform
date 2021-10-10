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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import io.moquette.BrokerConstants;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * A simple embedded Moquette-based MQTT test server for testing/experiments. This class works with Java 8.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestMoquetteServer extends AbstractTestServer {
    
    public static final String KEYSTORE_PASSWORD = "a1234567";
    public static final String KEY_ALIAS = "qpid";
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
    
    /*
    *  ssl_provider: defines the SSL implementation to use, default to "JDK"
    *            supported values are "JDK", "OPENSSL" and "OPENSSL_REFCNT"
    *            By choosing one of the OpenSSL implementations the crypto
    *            operations will be performed by a native Open SSL library.
    *
    *  jks_path: define the file that contains the Java Key Store,
    *            relative to the current broker home
    *
    *  key_store_type: defines the key store container type, default to "jks"
    *            supported values are "jceks", "jks" and "pkcs12"
    *            The "jceks" and "jks" key stores are a propietary SUN
    *            implementations. "pkcs12" is defined in PKCS#12 standard.
    *
    *  key_store_password: is the password used to open the keystore
    *
    *  key_manager_password: is the password used to manage the alias in the
    *            keystore
    */
    
    @Override
    public Server start() {
        if (null == mqttBroker) {
            //File hiveTmp = FileUtils.createTmpFolder("moquette_v3");

            Properties properties = new Properties();
            properties.setProperty("host", addr.getHost());
            properties.setProperty("allow_anonymous", "true");
            
            File cfgDir = getConfigDir("./src/test");
            File f = new File(cfgDir, "keystore.jks");
            if (f.exists()) {
                properties.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, f.getAbsolutePath());
                properties.setProperty(BrokerConstants.SSL_PROVIDER, "JDK");
                properties.setProperty(BrokerConstants.KEY_STORE_TYPE, "jks");
                properties.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, KEYSTORE_PASSWORD);
                properties.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, KEYSTORE_PASSWORD);
                properties.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, String.valueOf(addr.getPort()));
                properties.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(NetUtils.getEphemeralPort()));
            } else {
                properties.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(addr.getPort()));
            }
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
        BasicConfigurator.configure();
        TestMoquetteServer server = new TestMoquetteServer(new ServerAddress(Schema.IGNORE, getInteger(args, 8883)));
        server.start();
    }

}
