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
import java.util.HashMap;
import java.util.Map;

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
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

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
    private ConnectorParameter params;

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
            this.params = params;
            // BaSyx... stays HTTP, no TLS on the registry!!!
            Schema schema = params.getSchema();
            String epPath = params.getEndpointPath();
            int pos = epPath.indexOf(':');
            if (pos > 0 && pos < epPath.length()) {
                String tmp = epPath.substring(0, pos);
                epPath = epPath.substring(pos + 1);
                try {
                    schema = Schema.valueOf(tmp);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Implicit schema in endpoint path '" + tmp + "' unknown. Ignored. Using " + schema);
                }
            }
            Endpoint regEp = new Endpoint(Schema.HTTP, params.getHost(), params.getPort(), epPath);
            connectedAAS = factory.obtainRegistry(regEp, schema).retrieveAas(params.getApplicationId());
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
    protected Object read() throws IOException {
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
        private ElementsAccess base;
        private AasModelAccess parent;
        private Map<String, ElementsAccess> elements = new HashMap<>();

        /**
         * Creates the instance and binds the listener to the creating connector instance.
         */
        protected AasModelAccess() {
            super(AasConnector.this);
        }
        
        /**
         * Creates the instance and binds the listener to the creating connector instance.
         * Sets a resolution context by the given submodel/collection.
         * 
         * @param base the element to resolve on
         * @param parent the return parent for {@link #stepOut()}
         */
        protected AasModelAccess(ElementsAccess base, AasModelAccess parent) {
            this();
            this.base = base;
            this.parent = parent;
        }
        
        @Override
        public String topInstancesQName() {
            return ""; // none
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
         * @throws IOException if the property cannot be found/retrieved
         */
        private Property findProperty(String qName) throws IOException {
            Property result = null;
            int pos = qName.lastIndexOf(SEPARATOR_CHAR);
            ElementsAccess element;
            String propName = qName;
            if (pos > 1) {
                element = retrieveElement(qName.substring(0, pos));
                propName = qName.substring(pos + 1);
            } else {
                element = base; // context case
            }
            if (element != null) {
                result = element.getProperty(propName);
            } 
            if (null == result) {
                throw new IOException("Cannot find property " + qName);
            }
            return result;
        }
        
        /**
         * Retrieves a node starting at the root of the OPC UA model based on the node's qualified name {@code qName}.
         * Takes into account {@link #nodes the nodes cache}.
         * 
         * @param qName the qualified node name
         * @return the node
         * @throws IOException if no node can be found for {@code qName}
         */
        private ElementsAccess retrieveElement(String qName) throws IOException {
            ElementsAccess result = elements.get(qName);
            if (null == result) {
                result = base;
                String path = qName;
                if (null == result) {
                    int pos = qName.indexOf(SEPARATOR_CHAR);
                    if (pos > 0) {
                        result = connectedAAS.getSubmodel(qName.substring(0, pos));
                        path = qName.substring(pos + 1);
                    } else {
                        result = connectedAAS.getSubmodel(qName);
                        path = null;
                    }
                } 
                if (path != null && result != null) {
                    result = retrieveElement(result, path);    
                }
                if (null == result) {
                    throw new IOException("No element found for " + qName);
                } else {
                    elements.put(qName, result);
                }
            }
            return result;
        }
        
        /**
         * Retrieves a node starting at {@code current} recursively following the path given by {@code qName}.
         * 
         * @param current the current not to start searching for
         * @param qName the qualified node name
         * @return the node or <b>null</b> for none found
         */
        private ElementsAccess retrieveElement(ElementsAccess current, String qName) {
            ElementsAccess result = null;
            int pos = qName.indexOf(SEPARATOR_CHAR);
            String nodeName;
            String remainder = null;
            if (pos > 0) {
                nodeName = qName.substring(0, pos);
                if (pos + 1 < qName.length()) {
                    remainder = qName.substring(pos + 1);
                }
            } else {
                nodeName = qName;
            }
            ElementsAccess tmp = current.getSubmodelElementCollection(nodeName);
            if (null != tmp) {
                if (null == remainder) {
                    result = tmp;
                } else {
                    result = retrieveElement(tmp, remainder);
                }
            }
            return result;
        }
        
        // checkstyle: stop exception type check
        
        @Override
        public Object call(String qName, Object... args) throws IOException {
            Object result = null;
            Operation operation = null;
            int pos = qName.lastIndexOf(SEPARATOR_CHAR);
            ElementsAccess element;
            String opName = qName;
            if (pos > 1) {
                element = retrieveElement(qName.substring(0, pos));
                opName = qName.substring(pos + 1);
            } else {
                element = base; // context case
            }
            if (element != null) {
                operation = element.getOperation(opName);
            }
            if (operation != null) {
                try {
                    result = operation.invoke(args);
                } catch (Exception e) {
                    throw new IOException("While calling " + qName + ": " + e.getMessage(), e);
                }
            } else {
                throw new IOException("Cannot find operation " + qName);
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
        public void monitor(int notificationInterval, String... qName) throws IOException {
            throw new IOException("Event-based monitoring is not supported. Please use polling.");
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            throw new IOException("Event-based monitoring is not supported. Please use polling.");
        }

        @Override
        protected ConnectorParameter getConnectorParameter() {
            return params;
        }

        @Override
        public AasModelAccess stepInto(String name) throws IOException {
            AasModelAccess result = null;
            if (null == base) {
                Submodel submodel = connectedAAS.getSubmodel(name);
                if (null == submodel) {
                    throw new IOException("Submodel " + name + " not found. Cannot resolve further.");
                }
                result = new AasModelAccess(submodel, this);
            } else {
                SubmodelElementCollection coll = base.getSubmodelElementCollection(name);
                if (null == coll) {
                    throw new IOException("Collection " + name + " not found. Cannot resolve further.");
                }
                result = new AasModelAccess(coll, this);
            }
            return result;
        }

        @Override
        public AasModelAccess stepOut() {
            return parent;
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
