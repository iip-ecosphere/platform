/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.aas;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Tests the connector via TLS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TlsAasConnectorTest extends AasConnectorTest {

    /**
     * Sets the test up by starting an embedded OPC UA server.
     * 
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    @BeforeClass
    public static void init() throws SocketException, UnknownHostException {
        setKeystoreDescriptor(new KeyStoreDescriptor(new File("./src/test/keystore.jks"), "a1234567", "tomcat"));
        AasConnectorTest.init();
    }
    
    /**
     * Shuts down the test server.
     */
    @AfterClass
    public static void shutdown() {
        AasConnectorTest.shutdown();
    }
    
}
