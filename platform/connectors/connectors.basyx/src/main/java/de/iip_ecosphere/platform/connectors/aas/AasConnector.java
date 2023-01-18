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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.events.PatternTriggerQuery;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * A generic Asset Administration Shell connector. We use hierarchical names to identify sub-models
 * and elements within. Requires the the registry URL part, e.g. "registry" in 
 * {@link ConnectorParameter#getEndpointPath()}. The {@link ConnectorParameter#getApplicationId()} denotes the AASs
 * to operate on. If the application id is 
 * <ol>
 *     <li>a non-wildcard string, the uniquely denoted AAS is used for reading and writing/calling.</li>
 *     <li>a wildcard string in Java String Regex format, the denoted AAS are used for reading. AAS names are updated
 *       during polling. Currently, only the first matching AAS is enabled for writing/calling.</li>
 * </ol>
 * If the application id starts with a known AAS identifier schema, the id is parsed into that and utilized to obtain 
 * the AAS, e.g., iri:urn:... Without, the connector tries to obtain a registry based on an AAS short id, which 
 * currently may fail.
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

    private Map<String, Aas> connectedAAS = new HashMap<String, Aas>();
    private AasFactory factory;
    private ConnectorParameter params;
    private Pattern pattern;
    private Registry registry;
    private AtomicBoolean inPolling = new AtomicBoolean(false);
    
    private transient String pollingAas = "";
    private transient Thread pollingThread;
    private String nonPollingAas = "";

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
        if (connectedAAS.isEmpty()) {
            this.params = params;
            Schema schema = params.getSchema();
            String epPath = params.getEndpointPath();
            
            // endpoint handling is a bit mixed, old and new stuff
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
            Endpoint regEp = null;
            try {
                Schema regSchema;
                URL url = new URL(params.getEndpointPath());
                try {
                    regSchema = Schema.valueOf(url.getProtocol());
                } catch (IllegalArgumentException e1) {
                    regSchema = schema; // usual in BaSyx, not encrypted
                }
                if (url.getHost().length() > 0 && url.getPort() > 0) {
                    regEp = new Endpoint(regSchema, url.getHost(), url.getPort(), url.getPath());
                }
            } catch (MalformedURLException e) {
            }
            if (null == regEp) { // fallback, use server and epPath
                regEp = new Endpoint(schema, params.getHost(), params.getPort(), epPath);
            }
            
            registry = factory.obtainRegistry(regEp, schema);
            String name = params.getApplicationId();
            if (name.indexOf('?') > 0 || name.indexOf('*') > 0) {
                try {
                    pattern = Pattern.compile(name);
                } catch (PatternSyntaxException e) {
                    LOGGER.error("ApplicationName/AAS pattern not valid: {}", e.getMessage());
                }
                updateAas(pattern, connectedAAS, true);
            } else {
                Aas aas = registry.retrieveAas(name);
                if (null != aas) {
                    nonPollingAas = aas.getIdShort();
                    connectedAAS.put(nonPollingAas, aas);
                }
            }
        }
    }
    
    /**
     * Updates the AAS.
     * 
     * @param pattern the AAS id matching pattern
     * @param connectedAAS the actually known connected AAS for this update
     * @param modifyNotPollingAas whether {@link #nonPollingAas} may be modified by this call as a side effect
     * @return {@code true} if new AAS were added, {@code false} else
     */
    private boolean updateAas(Pattern pattern, Map<String, Aas> connectedAAS, boolean modifyNotPollingAas) {
        boolean foundNew = false;
        if (null != pattern && null != registry) {
            List<String> ids;
            String name = params.getApplicationId(); 
            if (name.startsWith("iri:")) {
                ids = registry.getAasIdentifiers();
            } else {
                ids = registry.getAasIdShorts();
            }
            for (String id: ids) {
                if (!connectedAAS.containsKey(id) && pattern.matcher(id).matches()) {
                    try {
                        Aas aas = registry.retrieveAas(id);
                        if (null != aas) {
                            connectedAAS.put(aas.getIdShort(), aas);
                            if (modifyNotPollingAas && nonPollingAas.length() == 0) {
                                nonPollingAas = aas.getIdShort();
                            }
                            foundNew = true;
                        }
                    } catch (IOException e) {
                        LOGGER.warn("Cannot retrieve AAS '{}': {}. Ignoring.", id, e.getMessage());
                    }
                }
            }
        }
        return foundNew;
    }
    
    /**
     * Calls {@code #received(Object)} for each AAS in {@code connectedAAS}.
     * 
     * @param connectedAAS the received AAS to be ingested
     */
    private void receivedAas(Map<String, Aas> connectedAAS) {
        for (String key : connectedAAS.keySet()) {
            pollingAas = key;
            try {
                Object data = read();
                if (null != data) {
                    if (getCachingStrategy().checkCache(key, data)) {
                        received(data);
                    }
                }
            } catch (IOException e) {
                error("While polling. Data discarded.", e);
            }
        }
    }

    @Override
    protected boolean checkCache(Object data) {
        return true; // don't do a double check
    }
    
    @Override
    protected void doPolling() {
        if (!inPolling.getAndSet(true)) {
            pollingThread = Thread.currentThread();
            updateAas(pattern, connectedAAS, true);
            receivedAas(connectedAAS);
            pollingAas = "";
            pollingThread = null;
            inPolling.set(false);
        }
    }
    
    // checkstyle: resume exception type check

    @Override
    protected void disconnectImpl() throws IOException {
        // if anything to be cleaned up, do it here
        connectedAAS.clear(); 
        nonPollingAas = "";
        registry = null;
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
    public void trigger(ConnectorTriggerQuery query) {
        if (query instanceof PatternTriggerQuery) {
            PatternTriggerQuery q = (PatternTriggerQuery) query;
            Map<String, Aas> connectedAAS = new HashMap<String, Aas>();
            int count = 0;
            while (inPolling.get() && count < 30) {
                TimeUtils.sleep(20);
                count++;
            }
            if (!inPolling.getAndSet(true)) {
                pollingThread = Thread.currentThread();
                updateAas(q.getPattern(), connectedAAS, false);
                receivedAas(connectedAAS);
                pollingAas = "";
                pollingThread = null;
                inPolling.set(false);
            }
        }
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message + ": " + th.getMessage());
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
        private Map<String, Map<String, ElementsAccess>> elements = new HashMap<>();

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
            String aasId = pollingThread == Thread.currentThread() ? pollingAas : nonPollingAas;
            Map<String, ElementsAccess> cache = elements.get(aasId);
            ElementsAccess result = null == cache ? null : cache.get(qName);
            if (null == result) {
                result = base;
                String path = qName;
                if (null == result) {
                    int pos = qName.indexOf(SEPARATOR_CHAR);
                    Aas aas = connectedAAS.get(aasId);
                    if (null != aas) {
                        if (pos > 0) {
                            result = aas.getSubmodel(qName.substring(0, pos));
                            path = qName.substring(pos + 1);
                        } else {
                            result = aas.getSubmodel(qName);
                            path = null;
                        }
                    }
                } 
                if (path != null && result != null) {
                    result = retrieveElement(result, path);    
                }
                if (null == result) {
                    throw new IOException("No element found for " + qName);
                } else {
                    if (null == cache) {
                        cache = new HashMap<>();
                        elements.put(aasId, cache);
                    }
                    cache.put(qName, result);
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
                    throw new IOException("While calling " + qName + ": " + e.getMessage());
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
                throw new IOException("Accessing " + qName + ": " + e.getMessage());
            }
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            try {
                findProperty(qName).setValue(value); // writes into model, may not be reflected further
            } catch (Exception e) {
                throw new IOException("Accessing " + qName + ": " + e.getMessage());
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
                Aas aas = connectedAAS.get(pollingThread == Thread.currentThread() ? pollingAas : nonPollingAas);
                if (null == aas) {
                    throw new IOException("AAS " + pollingAas + " not found. Cannot resolve further.");
                }
                Submodel submodel = aas.getSubmodel(name);
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
