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

import org.apache.log4j.BasicConfigurator;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A simple embedded Moquette-based MQTT TLS test server for testing/experiments. This class works with Java 8.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestMoquetteTlsServer extends TestMoquetteServer {

    /**
     * Creates the server instance.
     * 
     * @param addr the server address (schema is ignored)
     */
    public TestMoquetteTlsServer(ServerAddress addr) {
        super(addr);
    }
    
    /**
     * Starts the server from the command line.
     * 
     * @param args the first argument may be the port number, else 8883 is used
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        setConfigDir(new File("./src/test/secCfg"));
        TestMoquetteTlsServer server = new TestMoquetteTlsServer(
            new ServerAddress(Schema.IGNORE, getInteger(args, 8883)));
        server.start();
    }

}
