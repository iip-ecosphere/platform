/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * Implements a testing model connector.
 * 
 * @param <CO> the connector output type
 * @param <CI> the connector input type
 * 
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = true, supportsEvents = false, supportsHierarchicalQNames = true, 
    supportsModelCalls = false, supportsModelProperties = false, supportsModelStructs = false)
public class MyModelConnector<CO, CI> extends AbstractConnector<Object, Object, CO, CI> {

    public static final String NAME = "MyModelConnector";
    private Deque<Object> offers = new LinkedBlockingDeque<Object>();
    private Deque<Object> received = new LinkedBlockingDeque<Object>();
    private ConnectorParameter params;
    
    /**
     * The descriptor of this connector (see META-INF/services).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getType() {
            return MyModelConnector.class;
        }
        
    };

    /**
     * Creates a model connector instance.
     * 
     * @param adapter the protocol adapter(s)
     */
    @SafeVarargs
    public MyModelConnector(ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        super(adapter);
        configureModelAccess(new MyModelAccess(params));
    }

    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        this.params = params;
    }

    @Override
    public void disconnectImpl() throws IOException {
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        // handled by translators, anyway record
        received.offer(data);
    }

    @Override
    protected Object read() throws IOException {
        return offers.pollFirst(); 
    }

    @Override
    protected void error(String message, Throwable th) {
        System.out.println(message);
    }
    
    /**
     * Mimics a simple model access instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyModelAccess extends AbstractModelAccess {

        /**
         * Represents a model entry.
         * 
         * @author Holger Eichelberger, SSE
         */
        private class Entry {
            private Object value;
            private boolean notify;

            /**
             * Creates an entry.
             * 
             * @param value the value
             * @param notify notify by sending
             */
            private Entry(Object value, boolean notify) {
                this.value = value;
                this.notify = notify;
            }
            
        }
        
        private Map<String, Entry> model = new HashMap<>();
        private Map<String, Entry> structs = new HashMap<>();
        private ConnectorParameter params;
        
        /**
         * Creates an instance.
         * 
         * @param params connector params used during connect
         */
        protected MyModelAccess(ConnectorParameter params) {
            super(MyModelConnector.this);
            this.params = params;
        }
        
        @Override
        public String topInstancesQName() {
            return ""; // none
        }

        @Override
        public String getQSeparator() {
            return "/";
        }

        @Override
        public ModelDataType call(String qName, Object... args) throws IOException {
            return null;
        }

        @Override
        public Object get(String qName) throws IOException {
            if (model.containsKey(qName)) {
                return model.get(qName).value;
            } else {
                throw new IOException("Element " + qName + " does not exist");
            }
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            if (structs.containsKey(qName)) {
                throw new IOException(qName + " is already registered for a struct");
            }
            Entry entry;
            if (model.containsKey(qName)) {
                entry = model.get(qName);
                entry.value = value;
            } else {
                entry = new Entry(value, false);
                model.put(qName, entry);
            }
            notify(entry);
        }

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            T result;
            Entry ent = structs.get(qName);
            if (null == ent) {
                throw new IOException("Element " + qName + " does not exist");
            } else if (type.isInstance(ent.value)) {
                result = type.cast(ent.value);
            } else {
                throw new IOException("Cannot cast " + qName + " struct to " + type.getName());
            }
            return result;
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
            if (model.containsKey(qName)) {
                throw new IOException(qName + " is already registered for primitive property");
            }
            Entry entry;
            if (structs.containsKey(qName)) {
                entry = structs.get(qName);
                entry.value = value;
            } else {
                entry = new Entry(value, false);
                structs.put(qName, entry);
            }
            notify(entry);
        }

        /**
         * Performs the notification if registered.
         * 
         * @param entry the entry to notify for
         */
        private void notify(Entry entry) {
            if (entry.notify) {
                try {
                    received(entry.value);
                } catch (IOException e) {
                    error("While notifying" , e);
                }
            }
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
            // for serialization...
        }

        @Override
        public void monitor(int notificationInterval, String... qName) throws IOException {
            for (String n : qName) {
                Entry me = model.get(n);
                if (null != me) {
                    me.notify = true;
                } else {
                    Entry se = structs.get(n);
                    if (null != se) {
                        se.notify = true;
                    } else {
                        throw new IOException("Element " + qName + " does not exist");
                    }
                }
            }
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            // more server-sided notifications
        }

        @Override
        protected ConnectorParameter getConnectorParameter() {
            return params;
        }
        
    }
    
    // for testing
    
    /**
     * Put something into the queue to trigger a "model modification".
     * 
     * @throws IOException if offering fails for some reason
     */
    public void trigger() throws IOException {
        Object trigger = new Object();
        if (isPolling()) {
            offers.offer(trigger);
        } else {
            received(trigger);
        }
    }

    /**
     * Polls a received data element.
     * 
     * @return the received element, <b>null</b> for nothing received
     */
    public Object pollReceived() {
        return received.poll();
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
