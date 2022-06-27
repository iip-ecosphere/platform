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

package de.iip_ecosphere.platform.test.apps.serviceImpl.routingTest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * The connector of the routing test app. This connector is a generic connector, which is not further specialized
 * in the model or the generation (for demonstration). Thus, the Object type in the template parameters is dictated
 * by the default value of the generation, i.e., Object. The default connector type of the generation is a model 
 * connector, thus we provide an implementation of the {@link ModelAccess} type.
 * 
 * @param <CO> the output type of the connector (towards the platform)
 * @param <CI> the input type of the connector (from the platform)
 * 
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = true, supportsModelStructs = false, supportsEvents = false)
public class ConnectorImpl<CO, CI> extends AbstractConnector<Object, Object, CO, CI> {

    private static final Object DUMMY = new Object();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorImpl.class);
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public ConnectorImpl(ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        this(null, adapter);
    }
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public ConnectorImpl(AdapterSelector<Object, Object, CO, CI> selector, 
        ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        super(adapter);
        configureModelAccess(new ConnModelAccess());
    }
    
    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return "Routing test connector";
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
    protected void connectImpl(ConnectorParameter params) throws IOException {
        // timer is node by parent class
    }

    @Override
    protected void disconnectImpl() throws IOException {
        // timer disconnect is node by parent class
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        // not needed, we do this via model access
    }

    @Override
    protected Object read() throws IOException {
        return DUMMY; // allow for polling, no change information so far
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message + ": " + th.getMessage());
    }

    /**
     * Implements the model access.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ConnModelAccess extends AbstractModelAccess {

        /**
         * Creates the instance and binds the listener to the creating connector instance.
         */
        protected ConnModelAccess() {
            super(ConnectorImpl.this);
        }

        @Override
        public String topInstancesQName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getQSeparator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object call(String qName, Object... args) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object get(String qName) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void monitor(int notificationInterval, String... qNames) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public ModelAccess stepInto(String name) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ModelAccess stepOut() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected ConnectorParameter getConnectorParameter() {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
}
