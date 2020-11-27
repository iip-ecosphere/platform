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
package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.impl.DirectMemoryTransferTransportConnector;
import org.junit.Assert;

public class DirectMemoryTransportConnectorTest {

    /**
     * Does nothing, just for testing the creation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class FakeConnector implements TransportConnector {

        @Override
        public void syncSend(String stream, Object data) throws IOException {
        }

        @Override
        public void asyncSend(String stream, Object data) throws IOException {
        }

        @Override
        public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        }

        @Override
        public String composeStreamName(String parent, String name) {
            return null;
        }

        @Override
        public void connect(TransportParameter params) throws IOException {
        }

        @Override
        public void disconnect() throws IOException {
        }
        
    }
    
    private static final DirectMemoryTransferTransportConnector MY_DM_CONNECTOR 
        = new DirectMemoryTransferTransportConnector();
    private static final TransportConnector MY_FAKE_CONNECTOR = new FakeConnector();
    
    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testMemoryConnector() throws IOException {
        // just for the test as it is based on the factory
        ConnectorCreator dmc = new ConnectorCreator() {

            @Override
            public TransportConnector createConnector() {
                return MY_DM_CONNECTOR;
            }
        };
        
        ConnectorCreator fake = new ConnectorCreator() {

            @Override
            public TransportConnector createConnector() {
                return MY_FAKE_CONNECTOR;
            }
            
        };

        ConnectorCreator mainOld = TransportFactory.setMainImplementation(dmc);
        ConnectorCreator ipcOld = TransportFactory.setIpcImplementation(fake);
        ConnectorCreator dmOld = TransportFactory.setDmImplementation(fake);

        // as we have constants above, 
        Assert.assertTrue(TransportFactory.createConnector() == MY_DM_CONNECTOR);
        Assert.assertTrue(TransportFactory.createDirectMemoryConnector() == MY_FAKE_CONNECTOR);
        Assert.assertTrue(TransportFactory.createIpcConnector() == MY_FAKE_CONNECTOR);

        AbstractTransportConnectorTest.doTest("", 0, ProductJsonSerializer.class);
        MY_DM_CONNECTOR.clear(); // just as we want to have constants
        AbstractTransportConnectorTest.doTest("", 0, ProductProtobufSerializer.class);
        
        TransportFactory.setMainImplementation(mainOld);
        TransportFactory.setMainImplementation(ipcOld);
        TransportFactory.setMainImplementation(dmOld);
    }

}
