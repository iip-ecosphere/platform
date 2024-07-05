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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import de.iip_ecosphere.platform.connectors.MachineConnectorSupportedQueries;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.events.PatternTriggerQuery;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.CollectionUtils;
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
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElement;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElementFactory;

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
 * This class is based on {@link QualifiedElementFactory} for IDTA-style property "lists".
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 *
 * @author Holger Eichelberger, SSE
 * @author Jan Cepok, SSE
 */
@MachineConnector(hasModel = true, supportsModelStructs = false, supportsEvents = false, 
    specificSettings = {"PLUGINID"}, supportsMultiValued = true)
@MachineConnectorSupportedQueries({PatternTriggerQuery.class})
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
        configureModelAccess(new AasModelAccess(this));
    }
    
    /**
     * Returns the AAS for {@code aasId}.
     * 
     * @param aasId the AASid
     * @return the AAS or <b>null</b>
     */
    private Aas getAas(String aasId) {
        return connectedAAS.get(aasId);
    }

    // checkstyle: stop exception type check
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        if (connectedAAS.isEmpty()) {
            this.params = params;
            Schema schema = params.getSchema();
            String epPath = params.getEndpointPath();
            String pluginId = params.getSpecificStringSetting("PLUGINID");
            if (null != pluginId) {
                Plugin<AasFactory> plugin = PluginManager.getPlugin(pluginId, AasFactory.class);
                if (null != plugin) {
                    this.factory = plugin.getInstance();
                }
            }
            
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
            LOGGER.info("Connected to AAS registry: {}, notification interval {}", regEp.toUri(), 
                params.getNotificationInterval());
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
                        received(DEFAULT_CHANNEL, data);
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
     * Specialized model input converter.
     * 
     * @author Jan Cepok, SSE
     */
    private static class AasInputConverter extends ModelInputConverter {

        @Override
        public long toLong(Object data) throws IOException {
            if (data.getClass() == Long.class) {
                return (long) data;
            } else if (data.getClass() == Integer.class) { // AAS declares long but BaSyx uses int
                return (int) data;
            } else if (data instanceof Number) { // just in case
                return ((Number) data).longValue();
            } else {
                return 0; // no number???
            }
        }

        @Override
        public byte toByte(Object data) throws IOException {
            if (data.getClass() == Byte.class) {
                return (byte) data;
            } else if (data instanceof Number) { // AAS declares byte but BaSyx uses int, twice casting needed anyway
                return ((Number) data).byteValue();
            } else {
                return 0; // no number???
            }
        }

        @Override
        public short toShort(Object data) throws IOException {
            if (data.getClass() == Short.class) {
                return (short) data;
            } else if (data instanceof Number) { // AAS declares short but BaSyx uses int, twice casting needed anyway
                return ((Number) data).shortValue();
            } else {
                return 0; // no number???
            }
        }

    }
    
    /**
     * Implements the model access for AAS.
     * 
     * @author Holger Eichelberger, SSE
     * @author Jan Cepok, SSE
     */
    private static class AasModelAccess extends AbstractModelAccess {

        private static final char SEPARATOR_CHAR = '/';
        private static final String SEPARATOR_STRING = "/";
        private ElementsAccess base;
        private String basePath;
        private AasModelAccess parent;
        private AasInputConverter inputConverter = new AasInputConverter();
        private Map<String, Map<String, ElementsAccess>> elements = new HashMap<>();
        private AasConnector<?, ?> conn;
        
        /**
         * Creates the instance and binds the listener to the creating connector instance.
         * 
         * @param conn the connector
         */
        protected AasModelAccess(AasConnector<?, ?> conn) {
            super(conn);
            this.conn = conn;
        }
        
        /**
         * Creates the instance and binds the listener to the creating connector
         * instance. Sets a resolution context by the given submodel/collection.
         * 
         * @param conn     the parent connector
         * @param base     the element to resolve on
         * @param parent   the return parent for {@link #stepOut()}
         * @param basePath the base path
         * @param elements already known elements
         */
        protected AasModelAccess(AasConnector<?, ?> conn, ElementsAccess base, AasModelAccess parent, String basePath,
            Map<String, Map<String, ElementsAccess>> elements) {
            this(conn);
            this.base = base;
            this.parent = parent;
            this.basePath = basePath;
            this.elements = elements;
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
         * Returns the input converter instance.
         * 
         * @return the input converter
         */
        public ModelInputConverter getInputConverter() {
            return inputConverter;
        }

        /**
         * Returns the base access instance.
         * 
         * @return the base instance
         */
        ElementsAccess getBase() {
            return base;
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
         * Returns the current AAS id depending on whether polling or (parallel) non-polling accesses happen.
         * 
         * @return the AAS id
         */
        private String getAasId() {
            return conn.pollingThread == Thread.currentThread() ? conn.pollingAas : conn.nonPollingAas;
        }
        
        /**
         * Retrieves a node starting at the root of the OPC UA model based on the node's qualified name {@code qName}.
         * 
         * @param qName the qualified node name
         * @return the node
         * @throws IOException if no node can be found for {@code qName}
         */
        private ElementsAccess retrieveElement(String qName) throws IOException {
            String aasId = getAasId();
            Map<String, ElementsAccess> cache = elements.get(aasId);
            ElementsAccess result = null == cache ? null : cache.get(qName);
            if (null == result) {
                result = base;
                String path = qName;
                if (null == result) {
                    int pos = qName.indexOf(SEPARATOR_CHAR);
                    Aas aas = conn.getAas(aasId);
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
                    if (elements.get(aasId) != null) {
                        elements.get(aasId).put(qName, result);
                    }
                }
            }
            return result;
        }
        
        /**
         * Retrieves a node starting at {@code current} recursively following the path
         * given by {@code qName}.
         * 
         * @param current the current not to start searching for
         * @param qName   the qualified node name
         * @return the node or <b>null</b> for none found
         */
        private ElementsAccess retrieveElement(ElementsAccess current, String qName) {
            String aasId = getAasId();
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
            SubmodelElementCollection tmp = current.getSubmodelElementCollection(nodeName);
            if (null != tmp) {
                if (null == remainder) {
                    result = tmp;
                    Iterator<SubmodelElement> testIterator = tmp.elements().iterator();
                    retrieveUnderlyingElements(testIterator, basePath, aasId);
                } else {
                    result = retrieveElement(tmp, remainder);
                }
            }
            return result;
        }
        
        /**
         * Recursively retrieves nested elements.
         * 
         * @param subModelsIterator the elements to retrieve as iterator
         * @param actualPath the actual path where the retrieval starts
         * @param aasId the aasId for caching
         */
        private void retrieveUnderlyingElements(Iterator<SubmodelElement> subModelsIterator, String actualPath,
            String aasId) {
            Map<String, ElementsAccess> cachedElements = elements.get(aasId);
            if (cachedElements == null) {
                cachedElements = new HashMap<>();
            }
            while (subModelsIterator.hasNext()) {
                SubmodelElement elt = subModelsIterator.next();
                if (!(elt instanceof Property)) {
                    cachedElements.put(actualPath + "/" + elt.getIdShort(), (ElementsAccess) elt);
                    elements.put(aasId, cachedElements);
                    SubmodelElementCollection tmp = (SubmodelElementCollection) elt;
                    retrieveUnderlyingElements(tmp.elements().iterator(), actualPath + "/" + elt.getIdShort(), aasId);
                }
            }
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
        public <C> C getStruct(String qName, Class<C> type) throws IOException {
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
        public ConnectorParameter getConnectorParameter() {
            return conn.params;
        }

        @Override
        public AasModelAccess stepInto(String name) throws IOException {
            AasModelAccess result = null;
            
            if (!name.contains("/") && basePath == null) {
                if (null == base) {
                    Aas aas = conn.getAas(getAasId());
                    if (null == aas) {
                        throw new IOException("AAS " + conn.pollingAas + " not found. Cannot resolve further.");
                    }
                    Submodel submodel = aas.getSubmodel(name);
                    if (null == submodel) {
                        throw new IOException("Submodel " + name + " not found. Cannot resolve further.");
                    }
                    result = new AasModelAccess(conn, submodel, this, null, elements);
                } else {
                    SubmodelElementCollection coll = base.getSubmodelElementCollection(name);
                    if (null == coll) {
                        throw new IOException("Collection " + name + " not found. Cannot resolve further.");
                    }
                    result = new AasModelAccess(conn, coll, this, null, elements);
                }
            } else {
                if (!name.contains("/")) {
                    name = basePath + "/" + name;
                }
                if (!elements.isEmpty()) {
                    String aasId = getAasId();
                    if (elements.get(aasId).containsKey(name)) {
                        result = new AasModelAccess(conn, elements.get(aasId).get(name), this, name, elements);
                    }
                }
                if (result == null) {
                    if (null == base) {
                        basePath = name;
                        result = new AasModelAccess(conn, retrieveElement(name), this, basePath, elements);
                    }
                }
            }
            return result;    
        }

        @Override
        public AasModelAccess stepOut() {
            return parent;
        }
        
        @Override
        public <C> List<QualifiedElement<C>> getMultiValue(Class<C> eltCls, String name, 
            boolean enumerated, String... qualifier) throws IOException {
            ElementsAccess eltAccess = getBase();
            List<QualifiedElement<C>> result = null;
            if (enumerated) {
                int index = 1;
                Object value;
                do {
                    try {
                        String idShort = Utils.getCountingIdShort(name, index);
                        value = get(idShort);
                        String semId = null;
                        if (null != eltAccess) {
                            SubmodelElement elt = eltAccess.getSubmodelElement(idShort);
                            if (null != elt) {
                                semId = elt.getSemanticId();
                            }
                        }
                        result = addResult(result, eltCls, value, semId);
                        index++;
                    } catch (IOException e) {
                        if (index == 1) {
                            throw e;
                        }
                        break;
                    }
                } while (value != null);
            } else if (null != eltAccess) {
                Set<String> semId = CollectionUtils.toSet(qualifier);
                for (SubmodelElement elt : eltAccess.submodelElements()) {
                    if (semId.contains(elt.getSemanticId())) {
                        try {
                            Object value = get(elt.getIdShort());
                            result = addResult(result, eltCls, value, elt.getSemanticId());
                        } catch (IOException e) {
                            throw e;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Adds a result {@code value} to {@code result} if {@code value} complies with {@code cls}.
         * 
         * @param <C> the element value type
         * @param result the result list, may be <b>null</b>
         * @param eltCls the class of the element value type
         * @param value the value to add
         * @param qualifier the semantic id of the value/property
         * @return {@code result} or a new instance, possibly containing {@code value}
         */
        private static <C> List<QualifiedElement<C>> addResult(List<QualifiedElement<C>> result, Class<C> eltCls, 
            Object value, String qualifier) {
            if (value != null) {
                if (null == result) {
                    result = new ArrayList<QualifiedElement<C>>();
                }
                if (eltCls.isInstance(value)) {
                    QualifiedElement<C> elt = QualifiedElementFactory.createElement(eltCls);
                    elt.setValue(eltCls.cast(value));
                    elt.setQualifier(qualifier);
                    result.add(elt); // converter?
                } else {
                    result.add(null);
                }
            }
            return result;
        }
        
        @Override
        public void setMultiValue(String name, boolean enumerated, List<QualifiedElement<?>> elements) 
            throws IOException {
            ElementsAccess eltAccess = getBase();
            // unclear how to write it otherways
            for (int i = 0; i < elements.size(); i++) {
                String idShort = Utils.getCountingIdShort(name, i);
                set(idShort, elements.get(i));
                if (null != eltAccess) {
                    String semId = elements.get(i).getQualifier();
                    if (null != semId) {
                        SubmodelElement elt = eltAccess.getSubmodelElement(idShort);
                        if (null != elt) {
                            elt.setSemanticId(semId);
                        }
                    }
                }
            }
            
            if (null != eltAccess) { // assumes subsequent listing
                int i = elements.size();
                SubmodelElement elt;
                do {
                    String idShort = Utils.getCountingIdShort(name, i);
                    elt = eltAccess.getSubmodelElement(idShort);
                    if (null != eltAccess) {
                        eltAccess.deleteElement(idShort);
                    }
                    i++;
                } while (elt != null);
            }
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
