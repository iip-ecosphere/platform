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

package de.iip_ecosphere.platform.connectors.basyx;

import java.io.IOException;
import java.util.Map;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.vab.protocol.api.IConnectorProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * A generic Asset Administration Shell/BaSxy connector. We use hierarchical names to identify sub-models
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
public class BaSyxAasConnector<CO, CI> extends AbstractConnector<Object, Object, CO, CI, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxAasConnector.class);
    private static final Object DUMMY = new Object();

    private ConnectedAssetAdministrationShellManager manager;
    private ConnectedAssetAdministrationShell connectedAAS;
    private IAASRegistryService registry;
    private IConnectorProvider connectorProvider;
    @SuppressWarnings("unused")
    private ConnectorParameter params;

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     */
    public BaSyxAasConnector(ProtocolAdapter<Object, Object, CO, CI, Object> adapter) {
        super(adapter);
        adapter.setModelAccess(new BaSyxModelAccess());
    }

    // checkstyle: stop exception type check
    
    @Override
    public void connectImpl(ConnectorParameter params) throws IOException {
        this.params = params;
        if (null == connectedAAS) {
            try {
                String uri = "http://" + params.getHost() + ":" + params.getPort() + "/" + params.getEndpointPath();
                registry = new AASRegistryProxy(uri);
                connectorProvider = new HTTPConnectorProvider();
                manager = new ConnectedAssetAdministrationShellManager(registry, connectorProvider);
                ModelUrn aasURN = new ModelUrn(params.getApplicationId());
                connectedAAS = manager.retrieveAAS(aasURN);
            } catch (Exception e) {
                clear();
                throw new IOException(e); 
            }
        }
    }
    
    // checkstyle: resume exception type check
    
    /**
     * Clears the connection-relevant attributes.
     */
    private void clear() {
        registry = null;
        connectorProvider = null; 
        manager = null;
        connectedAAS = null;
    }

    @Override
    public void disconnect() throws IOException {
        // if anything to be cleaned up, do it here
        clear(); 
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }

    @Override
    public String getName() {
        return "BaSyx/AAS";
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
     * Implements the model access for AAS/BaSyx.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxModelAccess extends AbstractModelAccess<Object> {

        private static final char SEPARATOR_CHAR = '/';
        private static final String SEPARATOR_STRING = "/";

        /**
         * Creates the instance and binds the listener to the creating connector instance.
         */
        protected BaSyxModelAccess() {
            super(BaSyxAasConnector.this);
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
        private IProperty findProperty(String qName) throws IOException {
            IProperty result = null;
            int pos = qName.indexOf(SEPARATOR_CHAR);
            if (pos > 1) {
                String subModelName = qName.substring(0, pos);
                String elementName = qName.substring(pos + 1);
                Map<String, ISubModel> submodels = connectedAAS.getSubModels();
                ISubModel subModel = submodels.get(subModelName);
                if (null != subModel) {
                    Map<String, ISubmodelElement> properties = subModel.getSubmodelElements();
                    Object prop = properties.get(elementName);
                    if (null == prop) {
                        throw new IOException("Property " + elementName + " in " + qName + " does not exist");
                    } else if (prop instanceof IProperty) {
                        result = (IProperty) properties.get(elementName);
                    } else {
                        throw new IOException("Submodel element " + elementName + " in " + qName 
                            + " is not a property rather than a " + prop.getClass().getSimpleName());    
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
                Map<String, ISubModel> submodels = connectedAAS.getSubModels();
                ISubModel subModel = submodels.get(subModelName);
                if (null != subModel) {
                    Map<String, IOperation> operations = subModel.getOperations();
                    IOperation operation = operations.get(operationName);
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
                IProperty property = findProperty(qName);
                return property.get();
            } catch (Exception e) {
                throw new IOException("Accessing " + qName + ": " + e.getMessage(), e);
            }
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            try {
                IProperty property = findProperty(qName);
                property.set(value); // writes into model, may not be reflected further
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
        public Object fromInt(int value) throws IOException {
            return value;
        }

        @Override
        public Object fromString(String value) throws IOException {
            return value;
        }

        @Override
        public Object fromDouble(double value) throws IOException {
            return value;
        }

        @Override
        public int toInt(Object value) throws IOException {
            return (int) value;
        }

        @Override
        public String toString(Object value) throws IOException {
            return (String) value;
        }

        @Override
        public double toDouble(Object value) throws IOException {
            return (double) value;
        }

        @Override
        public void monitor(String... qName) throws IOException {
            throw new IOException("Event-based monitoring is not supported. Please use polling.");
        }
        
    }

}
