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

package de.iip_ecosphere.platform.connectors.aas;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * A generic Asset Administration Shell connector. We use hierarchical names to identify sub-models
 * and elements within. Requires the model URN as {@link ConnectorParameter#getApplicationId()}, e.g., 
 * "urn:::AAS:::testMachines#" and the registry URL part, e.g. "registry" in 
 * {@link ConnectorParameter#getEndpointPath()}. 
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 *
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = true, supportsModelStructs = false, supportsEvents = false)
public class AasConnector<CO, CI> extends AbstractConnector<Object, Object, CO, CI> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasConnector.class);
    private static final Object DUMMY = new Object();

    private Aas connectedAAS;
    private AasFactory factory;

    /**
     * The descriptor of this connector (see META-INF/services).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return "Generic AAS connector";
        }

        @Override
        public Class<?> getType() {
            return AasConnector.class;
        }
        
    }
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public AasConnector(ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        this(null, null, adapter);
    }
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param factory define the AasFactory to use, if <b>null</b> use {@link AasFactory#getInstance()}
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public AasConnector(AasFactory factory, ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        this(factory, null, adapter);
    }
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param factory define the AasFactory to use, if <b>null</b> use {@link AasFactory#getInstance()}
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public AasConnector(AasFactory factory, AdapterSelector<Object, Object, CO, CI> selector, 
        ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        super(selector, adapter);
        if (null == factory) {
            this.factory = AasFactory.getInstance();
        } else {
            this.factory = factory;
        }
        configureModelAccess(new AasModelAccess());
    }

    // checkstyle: stop exception type check
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        if (null == connectedAAS) {
            Endpoint regEp = new Endpoint(Schema.HTTP, params.getHost(), params.getPort(), params.getEndpointPath());
            connectedAAS = factory.obtainRegistry(regEp).retrieveAas(params.getApplicationId());
            if (null == connectedAAS) {
                throw new IOException("No AAS retrieved!");
            }
        }
    }
    
    // checkstyle: resume exception type check

    @Override
    protected void disconnectImpl() throws IOException {
        // if anything to be cleaned up, do it here
        connectedAAS = null; 
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }

    @Override
    public String getName() {
        return "AAS via factory " + factory.getName();
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        // not needed, we do this via model access
    }

    @Override
    public Object read() throws IOException {
        return DUMMY; // allow for polling, no change information so far
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message, th);
    }

    /**
     * Implements the model access for AAS.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class AasModelAccess extends AbstractModelAccess {

        private static final char SEPARATOR_CHAR = '/';
        private static final String SEPARATOR_STRING = "/";

        /**
         * Creates the instance and binds the listener to the creating connector instance.
         */
        protected AasModelAccess() {
            super(AasConnector.this);
        }

        @Override
        public String getQSeparator() {
            return SEPARATOR_STRING;
        }

        /**
         * Finds an AAS property.
         * 
         * @param qName the qualified name of the property
         * @return the property
         * @throws IOException if the poperty cannot be found/retrieved
         */
        private Property findProperty(String qName) throws IOException {
            Property result = null;
            int pos = qName.indexOf(SEPARATOR_CHAR);
            if (pos > 1) {
                String subModelName = qName.substring(0, pos);
                String elementName = qName.substring(pos + 1);
                Submodel subModel = connectedAAS.getSubmodel(subModelName);
                if (null != subModel) {
                    Property prop = subModel.getProperty(elementName);
                    if (null == prop) {
                        throw new IOException("Property " + elementName + " in " + qName + " does not exist");
                    } else {
                        result = prop;
                    }
                } else {
                    throw new IOException("Submodel " + subModelName + "in " + qName + " does not exist");    
                }
            } else {
                throw new IOException("No qualification/submodel given in " + qName);
            }
            return result;
        }
        
        // checkstyle: stop exception type check
        
        @Override
        public Object call(String qName, Object... args) throws IOException {
            Object result = null;
            int pos = qName.indexOf(SEPARATOR_CHAR);
            if (pos > 1) {
                String subModelName = qName.substring(0, pos);
                String operationName = qName.substring(pos + 1);
                Submodel subModel = connectedAAS.getSubmodel(subModelName);
                if (null != subModel) {
                    Operation operation = subModel.getOperation(operationName);
                    if (null != operation) {
                        try {
                            result = operation.invoke(args);
                        } catch (Exception e) {
                            throw new IOException("While calling " + operationName + ": " + e.getMessage(), e);
                        }
                    } else {
                        throw new IOException("Operation " + operationName + " in " + qName + " does not exist");
                    }
                } else {
                    throw new IOException("Submodel " + subModelName + "in " + qName + " does not exist");    
                }
            } else {
                throw new IOException("No qualification/submodel given in " + qName);
            }
            return result;
        }
        
        @Override
        public Object get(String qName) throws IOException {
            try {
                return findProperty(qName).getValue();
            } catch (Exception e) {
                throw new IOException("Accessing " + qName + ": " + e.getMessage(), e);
            }
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            try {
                findProperty(qName).setValue(value); // writes into model, may not be reflected further
            } catch (Exception e) {
                throw new IOException("Accessing " + qName + ": " + e.getMessage(), e);
            }
        }

        // checkstyle: resume exception type check

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            throw new IOException("Structs are not implemented in AAS"); // see @MachineConnector
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
            throw new IOException("Structs are not implemented in AAS"); // see @MachineConnector
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
            throw new IOException("Structs are not implemented in AAS"); // see @MachineConnector
        }

        @Override
        public void monitor(String... qName) throws IOException {
            throw new IOException("Event-based monitoring is not supported. Please use polling.");
        }
        
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
