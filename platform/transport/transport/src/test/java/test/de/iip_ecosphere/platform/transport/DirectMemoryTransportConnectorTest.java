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

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.transport.DefaultTransportFactoryDescriptor;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.impl.DirectMemoryTransferTransportConnector;
import org.junit.Assert;

/**
 * A transport connector that uses the memory for transport communication.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst
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
        
        @Override
        public String getName() {
            return "Fake";
        }

        @Override
        public String supportedEncryption() {
            return null;
        }

        @Override
        public String enabledEncryption() {
            return null;
        }
        
    }
    
    private static final DirectMemoryTransferTransportConnector MY_DM_CONNECTOR 
        = new DirectMemoryTransferTransportConnector();
    private static final TransportConnector MY_FAKE_CONNECTOR = new FakeConnector();
    
    /**
     * A descriptor for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    @ExcludeFirst
    public static class TestFactoryDescriptor extends DefaultTransportFactoryDescriptor {

        private ConnectorCreator dmc = new ConnectorCreator() {

            @Override
            public TransportConnector createConnector() {
                return MY_DM_CONNECTOR;
            }

            @Override
            public String getName() {
                return DirectMemoryTransferTransportConnector.NAME;
            }
            
        };
        
        private ConnectorCreator fake = new ConnectorCreator() {

            @Override
            public TransportConnector createConnector() {
                return MY_FAKE_CONNECTOR;
            }

            @Override
            public String getName() {
                return "Fake";
            }

        };
               
        @Override
        public ConnectorCreator getMainCreator() {
            return dmc;
        }

        @Override
        public ConnectorCreator getIpcCreator() {
            return fake;
        }

        @Override
        public ConnectorCreator getDmCreator() {
            return fake;
        }

    }
    
    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testMemoryConnector() throws IOException {
        // Assuming that TestFactoryDescriptor has been loaded
        
        Assert.assertTrue(TransportFactory.createConnector() == MY_DM_CONNECTOR);
        Assert.assertTrue(TransportFactory.createDirectMemoryConnector() == MY_FAKE_CONNECTOR);
        Assert.assertTrue(TransportFactory.createIpcConnector() == MY_FAKE_CONNECTOR);
        Assert.assertEquals(DirectMemoryTransferTransportConnector.NAME, TransportFactory.getConnectorName());
        Assert.assertNull(MY_FAKE_CONNECTOR.enabledEncryption());
        Assert.assertNull(MY_FAKE_CONNECTOR.supportedEncryption());

        ServerAddress addr = new ServerAddress(Schema.IGNORE, "", 0);
        AbstractTransportConnectorTest.doTest(addr, ProductJsonSerializer.class);
        MY_DM_CONNECTOR.clear(); // just as we want to have constants
        AbstractTransportConnectorTest.doTest(addr, ProductProtobufSerializer.class);
    }

}
