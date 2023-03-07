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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.transport.AppIntercom;
import de.iip_ecosphere.platform.transport.DefaultTransportFactoryDescriptor;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.connectors.impl.DirectMemoryTransferTransportConnector;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.ComponentTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.StatusMessageSerializer;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

import org.junit.Assert;

/**
 * A transport connector that uses the memory for transport communication. Tests also {@link StatusMessage} 
 * and {@link StatusMessageSerializer} as well as {@link Transport} as a simple, working transport connector is
 * available in this test.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst
public class DirectMemoryTransportConnectorTest {
    
    private static boolean factoryUseDmcAsTransport = false;

    /**
     * Does nothing, just for testing the creation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class FakeConnector implements TransportConnector {

        private Map<String, ReceptionCallback<?>> callbacks = new HashMap<>();
        
        @Override
        public void syncSend(String stream, Object data) throws IOException {
            serializeDeserializeNotify(stream, data);
        }

        @Override
        public void asyncSend(String stream, Object data) throws IOException {
            serializeDeserializeNotify(stream, data);
        }
        
        /**
         * Does a full round-trip what a usual connector does in at least two steps: serialize, look for callback,
         * take callback deserializer, deserialize and notify. Notification happens in sequence, not in parallel.
         * 
         * @param <T> the data type
         * @param stream the stream name
         * @param data the data to handle
         * @throws IOException if serialization/deserialization fails for some reason
         */
        @SuppressWarnings("unchecked")
        private <T> void serializeDeserializeNotify(String stream, T data) throws IOException {
            Class<T> cls = (Class<T>) data.getClass();
            Serializer<T> serializer = SerializerRegistry.getSerializer(cls);
            if (null != serializer) {
                byte[] tmp = serializer.to(data);
                ReceptionCallback<T> callback = (ReceptionCallback<T>) callbacks.get(stream);
                if (null != callback) {
                    Serializer<T> deserializer = SerializerRegistry.getSerializer(callback.getType());
                    if (null != deserializer) {
                        callback.received(deserializer.from(tmp));
                    } else {
                        System.out.println("No deserializer found for " + callback.getType().getName());
                    }
                } else {
                    System.out.println("No callback found for " + stream);
                }
            } else {
                System.out.println("No serializer found for " + cls.getName());
            }
        }

        @Override
        public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
            callbacks.put(stream, callback);
        }

        @Override
        public void unsubscribe(String stream, boolean delete) throws IOException {
            callbacks.remove(stream);
        }

        @Override
        public String composeStreamName(String parent, String name) {
            return parent + "/" + name;
        }

        @Override
        public void connect(TransportParameter params) throws IOException {
            // ignore
        }

        @Override
        public void disconnect() throws IOException {
            // ignore
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

        @Override
        public void detachReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
            unsubscribe(stream, true);
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
                return factoryUseDmcAsTransport ? MY_DM_CONNECTOR : MY_FAKE_CONNECTOR;
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
    
    /**
     * Tests "sending"/"receiving" {@link StatusMessage} via a direct memory connector. Involves serialization.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testStatusMessage() throws IOException {
        // Assuming that TestFactoryDescriptor has been loaded
        
        AtomicReference<StatusMessage> received = new AtomicReference<>();
        TransportConnector conn = TransportFactory.createDirectMemoryConnector();
        ReceptionCallback<StatusMessage> callback = new ReceptionCallback<StatusMessage>() {

            @Override
            public void received(StatusMessage data) {
                received.set(data);
            }

            @Override
            public Class<StatusMessage> getType() {
                return StatusMessage.class;
            }
        };
        conn.setReceptionCallback(StatusMessage.STATUS_STREAM, callback);
        // no connect needed, direct transfer
        StatusMessage msg = new StatusMessage(ComponentTypes.CONTAINER, ActionTypes.ADDED, "AAA", "BBB", "CCC");
        msg.send(conn);
        // no disconnect needed, direct transfer
        
        StatusMessage rcv = received.get();
        Assert.assertNotNull(rcv);
        Assert.assertEquals(msg.getComponentType(), rcv.getComponentType());
        Assert.assertEquals(msg.getAction(), rcv.getAction());
        Assert.assertEquals(msg.getId(), rcv.getId());
        Assert.assertEquals(msg.getDeviceId(), rcv.getDeviceId());
        Assert.assertArrayEquals(msg.getAliasIds(), rcv.getAliasIds());
    }

    /**
     * Tests a singular resource status message (different creation).
     */
    @Test
    public void testResourceStatusMessage() {
        StatusMessage msg = new StatusMessage(ActionTypes.REMOVED, "AAA", "BBB", "CCC");
        Assert.assertEquals(msg.getComponentType(), ComponentTypes.DEVICE);
        Assert.assertEquals(msg.getAction(), ActionTypes.REMOVED);
        Assert.assertEquals(msg.getId(), "AAA");
        Assert.assertEquals(msg.getDeviceId(), "AAA");
        Assert.assertArrayEquals(msg.getAliasIds(), new String[]{"BBB", "CCC"});
    }
    
    /**
     * Tests {@link Transport}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testTransport() throws IOException {
        factoryUseDmcAsTransport = true; // use a different default connector as required by Monitor
        Transport.setTransportSetup(() -> null); // not needed here
        AtomicInteger statusReceivedCount = new AtomicInteger();
        AtomicInteger traceReceivedCount = new AtomicInteger();
        MY_DM_CONNECTOR.setReceptionCallback(StatusMessage.STATUS_STREAM, new ReceptionCallback<StatusMessage>() {

            @Override
            public void received(StatusMessage data) {
                statusReceivedCount.getAndIncrement();
            }

            @Override
            public Class<StatusMessage> getType() {
                return StatusMessage.class;
            }
        });
        MY_DM_CONNECTOR.setReceptionCallback(TraceRecord.TRACE_STREAM, new ReceptionCallback<TraceRecord>() {

            @Override
            public void received(TraceRecord data) {
                traceReceivedCount.getAndIncrement();
            }

            @Override
            public Class<TraceRecord> getType() {
                return TraceRecord.class;
            }
        });
        Transport.setTransportSetup(() -> new TransportSetup()); // info not needed by connector, just the instance
        Transport.sendResourceStatus(ActionTypes.ADDED);
        Transport.sendContainerStatus(ActionTypes.CHANGED, "Container-1");
        Transport.sendServiceStatus(ActionTypes.REMOVED, "Service-1");
        Transport.sendServiceArtifactStatus(ActionTypes.REMOVED, "ServiceArtifact-1");
        Transport.sendTraceRecord(new TraceRecord("src", "act", null));
        
        try {
            Set<String> data = new HashSet<>();
            AppIntercom<String> intercom = new AppIntercom<>(d->data.add(d), String.class);
            intercom.start();
            intercom.asyncSend("async");
            intercom.syncSend("sync");
            intercom.stop();
            TimeUtils.sleep(1000); // asyncSend
            // TODO asserts
        } catch (ExecutionException e) {
            Assert.fail("Exception thrown: " + e);
        }

        Transport.releaseConnector(); // prevent reconnects by default
        Transport.sendResourceStatus(ActionTypes.ADDED); // shall not be sent/received

        factoryUseDmcAsTransport = false;
        Assert.assertEquals(4, statusReceivedCount.get());
        Assert.assertEquals(1, traceReceivedCount.get());
    }

}
