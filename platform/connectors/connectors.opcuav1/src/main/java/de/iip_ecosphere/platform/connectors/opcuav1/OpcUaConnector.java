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

package de.iip_ecosphere.platform.connectors.opcuav1;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.SignedIdentityToken;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDefaultBinaryEncoding;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UNumber;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.AnonymousIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.ContentFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.EventFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.IssuedIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.SignatureData;
import org.eclipse.milo.opcua.stack.core.types.structured.SimpleAttributeOperand;
import org.eclipse.milo.opcua.stack.core.types.structured.UserIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.UserNameIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.X509IdentityToken;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.net.SslUtils;

/**
 * Implements the generic OPC UA connector. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * For custom types such as structs, the following must apply: <ul>
 *   <li>A class implements the datatype and its fields.</li>
 *   <li>The class declares an accessible static field named {@code BINARY_ENCODING_ID} of type 
 *       {@link ExpandedNodeId}.</li>
 *   <li>The class declares a top-level inner class called "Codec" of type {@link GenericDataTypeCodec} having
 *       an accessible no-arg constructor.</li>
 *   <li>Value constructor parameters and accessors shall be based on Java types rather than OPC/Milo types</li>
 *   <li>Such custom types must be registered through {@link ModelAccess#registerCustomType(Class)} 
 *       in {@link ConnectorOutputTypeTranslator#initializeModelAccess()}.</li>
 * </ul>
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 * @author Holger Eichelberger, SSE
 */
@MachineConnector // default values sufficient
public class OpcUaConnector<CO, CI> extends AbstractConnector<DataItem, Object, CO, CI> {

    /**
     * The name of this connector.
     */
    public static final String NAME = "OPC UA v1";
    
    /**
     * Denotes the top-level folder "Objects".
     */
    public static final String TOP_OBJECTS = "Objects";
    
    /**
     * Denotes the top-level folder "Types".
     */
    public static final String TOP_TYPES = "Types";
    
    /**
     * Denotes the top-level folder "Views".
     */
    public static final String TOP_VIEWS = "Views";
    
    /**
     * Denotes the path separator for qualified model names.
     */
    public static final char SEPARATOR_CHAR = '/';
    
    /**
     * Denotes the path separator for qualified model names (as String).
     */
    public static final String SEPARATOR_STRING = "/";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaConnector.class);
    private static final DataItem DUMMY = new DataItem(null, null);
    private static final String FIELD_BINARY_ENCODING_ID = "BINARY_ENCODING_ID";
    private OpcUaClient client;
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
            return OpcUaConnector.class;
        }
        
    }

    /**
     * Creates an instance and installs the protocol adapter(s).
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public OpcUaConnector(ProtocolAdapter<DataItem, Object, CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates an instance and installs the protocol adapter(s).
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public OpcUaConnector(AdapterSelector<DataItem, Object, CO, CI> selector, 
        ProtocolAdapter<DataItem, Object, CO, CI>... adapter) {
        super(selector, adapter);
        configureModelAccess(new OpcUaModelAccess());
    }

    /**
     * Construct the endpoint URL.
     * 
     * @param params the connector parameters
     * @return the endpoint URL
     */
    private String getEndpointUrl(ConnectorParameter params) {
        return "opc." + params.getSchema().toUri() + params.getHost() + ":" + params.getPort() 
            + "/" + params.getEndpointPath();
    }
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        if (null == client) {
            this.params = params;
            String endpointURL = getEndpointUrl(params);
            LOGGER.info("OPC UA connecting to {}", endpointURL);
            try {
                client = OpcUaClient.create(
                    endpointURL,
                    endpoints -> endpoints.stream()
                        .filter(endpointFilter(params))
                        .findFirst(),
                    configBuilder -> configure(configBuilder).build()
                 );
                client.connect().get();
                LOGGER.info("OPC UA connected to {}", endpointURL);
            } catch (UaException | InterruptedException | ExecutionException e) { // also for interrupted
                client = null;
                throw new IOException(e);
            }
        }
    }

    /**
     * Provides a filter for OPC UA endpoints in case that multiple are present. 
     * 
     * @param params the connector parameters
     * @return the endpoint filter
     */
    protected Predicate<EndpointDescription> endpointFilter(ConnectorParameter params) {
        return e -> params.isFeasibleEndpoint(e.getEndpointUrl(), e.getSecurityLevel().byteValue());
    }
    
    /**
     * Does the basic configuration of the OPC UA client.
     * 
     * @param configBuilder the configuration builder
     * @return {@code configBuilder}
     */
    private OpcUaClientConfigBuilder configure(OpcUaClientConfigBuilder configBuilder) {
        configBuilder
            .setApplicationName(LocalizedText.english(params.getApplicationDescription()))
            .setApplicationUri(params.getApplicationId())
            .setIdentityProvider(getIdentityProvider(params))
            .setRequestTimeout(uint(params.getRequestTimeout()));

        try {        
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(getEndpointUrl(params)).get();        
            EndpointDescription configEndpoint = EndpointUtil.updateUrl(endpoints.get(0), 
                params.getHost(), params.getPort());
            LOGGER.info("Configured for security policy {}", configEndpoint.getSecurityPolicyUri());
            configBuilder.setEndpoint(configEndpoint);        
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.info("Cannot adjust endpoint of {}. Staying with original.", getEndpointUrl(params));
        }
        if (null != params.getKeystore()) {
            try {
                KeyStore keystore = SslUtils.openKeyStore(params.getKeystore(), params.getKeystorePassword());
                String alias = params.getKeyAlias();
                if (null == alias) {
                    try {
                        alias = keystore.aliases().nextElement();
                    } catch (NoSuchElementException e) {
                        // ignore, alias == null
                    }
                }

                if (null != alias) {
                    Certificate cert = keystore.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        configBuilder.setCertificate((X509Certificate) cert);
                        try {                
                            Key key = keystore.getKey(alias, params.getKeystorePassword().toCharArray());
                            if (key instanceof PrivateKey) {
                                configBuilder.setKeyPair(new KeyPair(cert.getPublicKey(), (PrivateKey) key));
                            } else {
                                configBuilder.setKeyPair(new KeyPair(cert.getPublicKey(), null)); // unsure, shall work
                            }
                        } catch (UnrecoverableKeyException | NoSuchAlgorithmException e) {
                            LOGGER.error("Cannot read private key alias '{}': {}: Trying without TLS.", alias, 
                                e.getMessage());
                        }                    
                    } else {
                        LOGGER.error("Certificate for alias '{}' is not of type X509. Trying without TLS.", alias);
                    }
                } else {
                    LOGGER.error("No certificate found, no alias given. Trying without TLS.");
                }

            } catch (IOException | KeyStoreException e) {
                LOGGER.error("Cannot read from keystore '{}': {} Trying without TLS.", 
                    params.getKeystore(), e.getMessage());
            }
        }
        return configBuilder;
    }

    /**
     * Returns the id token for an endpoint URL considering {@link ConnectorParameter#ANY_ENDPOINT}.
     * 
     * @param endpointUrl the endpoint URL
     * @return the identity token
     */
    private IdentityToken getIdToken(String endpointUrl) {
        IdentityToken idToken = params.getIdentityToken(endpointUrl);
        if (null == idToken) {
            idToken = params.getIdentityToken(ConnectorParameter.ANY_ENDPOINT);
        }
        return idToken;
    }
    
    /**
     * A fallback identity provider.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class FallbackIdentityProder implements IdentityProvider {
        
        @Override
        public SignedIdentityToken getIdentityToken(EndpointDescription endpoint, ByteString serverNonce) 
            throws Exception {
            SignedIdentityToken token = null;
            IdentityToken idToken = getIdToken(endpoint.getEndpointUrl());
            if (null != idToken) {
                UserIdentityToken uiToken;
                switch (idToken.getType()) {
                case ISSUED:
                    uiToken = new IssuedIdentityToken(idToken.getTokenPolicyId(), 
                        new ByteString(idToken.getTokenData()), 
                        idToken.getTokenEncryptionAlgorithm());
                    break;
                case USERNAME:
                    uiToken = new UserNameIdentityToken(idToken.getTokenPolicyId(), idToken.getUserName(), 
                        new ByteString(idToken.getTokenData()), idToken.getTokenEncryptionAlgorithm());
                    break;
                case X509:
                    uiToken = new X509IdentityToken(idToken.getTokenPolicyId(), 
                        new ByteString(idToken.getTokenData()));
                    break;
                default: // including ANONYMOUS
                    uiToken = new AnonymousIdentityToken(idToken.getTokenPolicyId());
                    break;
                }
                
                token = new SignedIdentityToken(uiToken, new SignatureData(idToken.getSignatureAlgorithm(), 
                    new ByteString(idToken.getSignature())));
            } else {
                throw new Exception("No token information configured"); 
            }
            return token;
        }

    }
    
    /**
     * Returns the identity provider by translating the token information in {@code params} to Milo specific token 
     * information.
     * 
     * @param params the connector params
     * @return the identity provider
     */
    protected IdentityProvider getIdentityProvider(ConnectorParameter params) {
        IdentityProvider identityProvider;
        if (params.isAnonymousIdentity()) {
            identityProvider = new AnonymousProvider();
        } else { 
            IdentityToken idToken = getIdToken(getEndpointUrl(params));
            if (TokenType.USERNAME == idToken.getType()) {
                String pw = new String(idToken.getTokenData(), StandardCharsets.UTF_8);
                identityProvider = new UsernameProvider(idToken.getUserName(), pw);
            } else {
                identityProvider = new FallbackIdentityProder();
            }
        }
        return identityProvider;
    }

    @Override
    protected void disconnectImpl() throws IOException {
        if (null != client) {
            try {
                client.disconnect().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException(e); // also for interrupted?
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void dispose() {
        Stack.releaseSharedResources();
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        // not needed, we do this via model access
    }

    @Override
    protected DataItem read() throws IOException {
        return DUMMY; // regardless, if we are asked, we do not report the changes; typeTranslator will compose the data
    }
    
    @Override
    public void trigger(ConnectorTriggerQuery query) {
        if (null != query) {
            try {
                received(new DataItem(query), true);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot trigger connector {}: {}", getName(), e.getMessage());
            }
        }
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message, th);
    }
    
    /**
     * Realizes a node cache entry with value lifetime.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class NodeCacheEntry {
        private UaNode node;
        private long valueLifetime = 0;
        private long valueTimestamp;
        private Object value;
        
        /**
         * Creates a cache entry for a pseudo node, e.g., a built-in property.
         */
        private NodeCacheEntry() {
        }
        
        /**
         * Creates a cache entry for a given {@code node}.
         * 
         * @param node the node
         */
        private NodeCacheEntry(UaNode node) {
            this.node = node;
        }

        /**
         * Sets the cached value.
         * 
         * @param value the value to set
         */
        private void setValue(Object value) {
            if (valueLifetime != 0) { // no caching
                this.value = value;
                valueTimestamp = System.currentTimeMillis();
            }
        }
        
        /**
         * Changes the value of a cached entry.
         * 
         * @param value the value to set
         * @param lifetime the lifetime (negative unlimited, 0 request all time, positive caching time in ms) 
         */
        private void setValue(Object value, int lifetime) {
            this.valueLifetime = lifetime;
            setValue(value);
        }

        /**
         * Returns the value of this node considering the caching lifetime.
         * 
         * @return the value of this node
         */
        private Object getValue() {
            Object result = null;
            if (valueLifetime < 0) { // always valid
                result = value;
            } else if (valueLifetime > 0) { // there is a limit
                if ((System.currentTimeMillis() - valueTimestamp) < valueLifetime) {
                    result = value;
                } else { // outdated, reset
                    value = null;
                }
            }
            return result;
        }
        
    }
    
    /**
     * Implements the model access for OPC UA.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class OpcUaModelAccess extends AbstractModelAccess {

        private Map<String, NodeCacheEntry> nodes;
        private NodeCacheEntry base;
        private String basePath;
        private OpcUaModelAccess parent;
        
        /**
         * Creates the instance and binds the listener to the creating connector instance.
         */
        protected OpcUaModelAccess() {
            super(OpcUaConnector.this);
            nodes = new HashMap<>();
            basePath = "";
        }
        
        /**
         * Creates the instance and binds the listener to the creating connector instance.
         * 
         * @param base the context node to resolve non-nested names on
         * @param basePath the base path this context represente
         * @param parent the parent to return to in {@link #stepOut()}
         * @param nodes the (parent) nodes cache to use
         */
        protected OpcUaModelAccess(NodeCacheEntry base, String basePath, OpcUaModelAccess parent, 
            Map<String, NodeCacheEntry> nodes) {
            this();
            this.base = base;
            this.parent = parent;
            this.nodes = nodes;
            this.basePath = basePath;
        }

        @Override
        public String topInstancesQName() {
            return TOP_OBJECTS;
        }
        
        @Override
        public String getQSeparator() {
            return SEPARATOR_STRING;
        }

        @Override
        public Object call(String qName, Object... args) throws IOException {
            Object callResult;
            int pos = qName.lastIndexOf(SEPARATOR_CHAR);
            if (pos > 1) {
                Variant[] a = new Variant[args.length];
                for (int i = 0; i < args.length; i++) {
                    a[i] = new Variant(args[i]);
                }
                try {
                    String nodeName = qName.substring(0, pos);
                    UaNode node = retrieveCacheEntry(nodeName).node;
                    String methodName = qName.substring(pos + 1);
                    UaNode methodNode = retrieveNode(node, methodName);
                    if (null == methodNode) {
                        throw new IOException("Method " + methodName + " does not exist on " + nodeName);
                    }
                    CallMethodRequest request = new CallMethodRequest(node.getNodeId(), methodNode.getNodeId(), a);    
                    Variant cr = client.call(request).thenCompose(result -> {
                        StatusCode statusCode = result.getStatusCode();
    
                        if (statusCode.isGood()) {
                            Variant res;
                            Variant[] results = result.getOutputArguments();
                            if (0 == results.length ) {
                                res = null;
                            } else {
                                res = result.getOutputArguments()[0];
                            }
                            return CompletableFuture.completedFuture(res);
                        } else {
                            CompletableFuture<Variant> f = new CompletableFuture<>();
                            f.completeExceptionally(new UaException(statusCode));
                            return f;
                        }
                    }).get();
                    if (null != cr) {
                        callResult = cr.getValue();
                    } else {
                        callResult = null;
                    }
                } catch (ExecutionException | InterruptedException | UaException e) {
                    throw new IOException(e);
                }
            } else {
                throw new IOException("Cannot access top level operation '" + qName + "'");
            }
            return callResult;
        }

        /**
         * Just for testing, see Identifiers.RootFolder.
         * 
         * @param indent output indent
         * @param browseRoot the node id to start browsing at
         */
        @SuppressWarnings("unused")
        private void browseNode(String indent, NodeId browseRoot) {
            try {
                List<? extends UaNode> nodes = client.getAddressSpace().browseNodes(browseRoot);

                for (UaNode node : nodes) {
                    LOGGER.info("{} Node={} Id={}", indent, node.getBrowseName().getName(), 
                        node.getNodeId().getIdentifier());

                    // recursively browse to children
                    browseNode(indent + "  ", node.getNodeId());
                }
            } catch (UaException e) {
                LOGGER.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
            }
        }

        /**
         * Retrieves an OPC UA variable node from a cache node entry.
         * 
         * @param qName the qualified node name (for exception message)
         * @param entry the cache entry to read the variable node from
         * @return the variable node
         * @throws IOException if no node can be found for {@code qName} or if the found node is not a variable node 
         * @throws UaException if accessing/browsing the OPC UA model fails
         */
        private UaVariableNode retrieveVariableNode(String qName, NodeCacheEntry entry) 
            throws UaException, IOException {
            UaVariableNode result = null;
            UaNode n = null == entry ? null : entry.node;
            if (n instanceof UaVariableNode) {
                result = (UaVariableNode) n;
            } else {
                throw new IOException("'" + qName + "' does not point to a variable");
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
         * @throws UaException if accessing/browsing the OPC UA model fails
         */
        private NodeCacheEntry retrieveCacheEntry(String qName) throws UaException, IOException {
            NodeCacheEntry cached = nodes.get(qName);
            if (null == cached && basePath.length() > 0) {
                cached = nodes.get(basePath + "/" + qName);
            }
            if (null == cached) {
                UaNode result = retrieveNode(null == base ? null : base.node, qName);
                if (null == result) {
                    throw new IOException("No node found for " + qName);
                } else {
                    cached = new NodeCacheEntry(result);
                    nodes.put(qName, cached);
                }
            }
            return cached;
        }
        
        /**
         * Retrieves a node starting at {@code current} recursively following the path given by {@code qName}.
         * 
         * @param current the current node to start searching for, may be <b>null</b> for top-level
         * @param qName the qualified node name
         * @return the node or <b>null</b> for none found
         * @throws UaException if accessing/browsing the OPC UA model fails
         */
        private UaNode retrieveNode(UaNode current, String qName) throws UaException {
            UaNode result = null;
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
            List<? extends UaNode> nodes;
            if (null == current) {
                nodes = client.getAddressSpace().browseNodes(Identifiers.RootFolder);
            } else {
                nodes = current.browseNodes(); // sync for now
            }
            for (int n = 0; null == result && n < nodes.size(); n++) {
                UaNode tmp = nodes.get(n);
                String nn = tmp.getBrowseName().getName();                
                String qn = basePath + "/" + nn;
                if (!this.nodes.containsKey(qn)) { // implicit caching
                    this.nodes.put(qn, new NodeCacheEntry(tmp));
                }
                if (nodeName.equals(tmp.getBrowseName().getName())) {
                    if (null == remainder) {
                        result = tmp;
                    } else {
                        result = retrieveNode(tmp, remainder);
                    }
                }
            }
            return result;
        }

        @Override
        public Object get(String qName) throws IOException {
            return get(qName, 0);
        }
        
        @Override
        public Object get(String qName, int lifetime) throws IOException {
            Object result = null;
            try {
                NodeCacheEntry cached = retrieveCacheEntry(qName);
                result = cached.getValue();
                if (null == result) {
                    UaVariableNode node = retrieveVariableNode(qName, cached);
                    DataValue value = node.readValue();
                    Variant r = value.getValue();
                    if (null != r) {
                        result = r.getValue();
                        if (result instanceof UNumber) { // simplfied
                            result = ((UNumber) result).intValue();
                        } else if (result instanceof NodeId) {
                            result = result.toString();
                        }
                        cached.setValue(result, lifetime);
                    } else {
                        result = null;
                    }
                }
            } catch (UaException e) {
                throw new IOException(e);
            } catch (IOException e) {
                result = DUMMY;
                int pos = qName.lastIndexOf(SEPARATOR_CHAR); // try to handle buildins
                String slot = qName;
                String nodeName;
                try {
                    NodeCacheEntry cached;
                    if (pos > 0) {
                        slot = qName.substring(pos + 1);
                        nodeName = qName.substring(0, pos);
                        cached = retrieveCacheEntry(nodeName);
                    } else {
                        cached = base;
                        nodeName = basePath;
                    }
                    Object tmp = null == cached ? null : cached.getValue();
                    if (null == tmp) {
                        UaVariableNode node = retrieveVariableNode(nodeName, cached);
                        DataValue value = node.getValue();
                        Variant r = value.getValue();
                        if (null != r) {
                            tmp = r.getValue();
                        }
                    }
                    if (tmp instanceof LocalizedText) {
                        LocalizedText txt = (LocalizedText) tmp;
                        if (slot.equals("locale")) {
                            result = txt.getLocale();
                        } else if (slot.equals("text")) {
                            result = txt.getText();
                        }
                        if (null != result) {
                            NodeCacheEntry ent = new NodeCacheEntry();
                            ent.setValue(result, lifetime);
                            nodes.put(qName, ent);
                        }
                    } else {
                        cached.setValue(tmp, lifetime);
                    }
                } catch (UaException e1) {
                    // ignore
                }
                if (DUMMY == result) {
                    throw e;
                }
            }
            return result;
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            try {
                NodeCacheEntry cached = retrieveCacheEntry(qName);
                cached.setValue(value);
                UaVariableNode node = retrieveVariableNode(qName, cached);
                node.writeValue(new DataValue(new Variant(value)));
                // TODO handle built-ins
            } catch (UaException e) {
                throw new IOException(e);
            }
        }

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            try {
                NodeCacheEntry cached = retrieveCacheEntry(qName);
                UaVariableNode node = retrieveVariableNode(qName, cached);
                DataValue value = node.readValue();
                Variant variant = value.getValue();
                ExtensionObject xo = (ExtensionObject) variant.getValue();
                T decoded = type.cast(xo.decode(
                    client.getDynamicSerializationContext()
                ));
                return decoded;
            } catch (UaException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
            try {
                ExpandedNodeId encodingId = getEncodingId(value.getClass());
                NodeCacheEntry cached = retrieveCacheEntry(qName);
                UaVariableNode node = retrieveVariableNode(qName, cached);
                ExtensionObject modifiedXo = ExtensionObject.encode(
                    client.getDynamicSerializationContext(),
                    value,
                    encodingId,
                    OpcUaDefaultBinaryEncoding.getInstance()
                );
                node.writeValue(new DataValue(new Variant(modifiedXo)));
            } catch (UaException e) {
                throw new IOException(e);
            }
        }
        
        /**
         * Returns the encodingId of a custom type.
         * 
         * @param cls the type class
         * @return the encodingId
         * @throws IOException if the encodingID cannot be found
         */
        private ExpandedNodeId getEncodingId(Class<?> cls) throws IOException {
            ExpandedNodeId encodingId;
            try {
                Field bei = cls.getField(FIELD_BINARY_ENCODING_ID); // TODO document assumption
                encodingId = (ExpandedNodeId) bei.get(null);
            } catch (ClassCastException e) {
                throw new IOException("Field " + FIELD_BINARY_ENCODING_ID + " in class " + cls.getName() 
                    + " is not of type " + ExpandedNodeId.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IOException("Class " + cls.getName() + " does not declare a publicly accessible static "
                    + "field BINARY_ENCODING_ID providing the encoding id.");
            }
            return encodingId;
        }
        
        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
            ExpandedNodeId encodingId = getEncodingId(cls);
            NodeId binaryEncodingId = encodingId
                .toNodeId(client.getNamespaceTable())
                .orElseThrow(() -> new IOException("Client namespace not found"));

            // find codec according to conventions
            GenericDataTypeCodec<?> codec = null;
            Class<?>[] declared = cls.getDeclaredClasses();
            for (Class<?> cl : declared) {
                if (cl.getSimpleName().equals("Codec") && GenericDataTypeCodec.class.isAssignableFrom(cl)) {
                    try {
                        codec = (GenericDataTypeCodec<?>) cl.getConstructor().newInstance();
                    } catch (NoSuchMethodException e) {
                        throw new IOException("Cannot instantiate codec in " + cls.getName() 
                        + ": No accessible no-arg constructor declared");
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                        throw new IOException("Cannot instantiate codec in " + cls.getName() 
                            + ": " + e.getMessage(), e);
                    } 
                }
            }
            if (null != codec) {
                // Register codec with the client DataTypeManager instance
                client.getDynamicDataTypeManager().registerCodec(
                    binaryEncodingId,
                    codec.asBinaryCodec()
                );
            } else {
                throw new IOException("No inner class Codec extending " + GenericDataTypeCodec.class 
                    + " found in " + cls.getName());
            }
        }
        
        @Override
        protected ConnectorParameter getConnectorParameter() {
            return params;
        }

        @Override
        public void monitor(int notificationInterval, String... qName) throws IOException {
            try {
                UaSubscription subscription = client.getSubscriptionManager().createSubscription(
                    notificationInterval).get();
                UInteger clientHandle = subscription.nextClientHandle();
            
                MonitoringParameters parameters = new MonitoringParameters(
                    clientHandle,
                    (double) params.getNotificationInterval(), // sampling interval
                    null,       // filter, null means use default
                    uint(10),   // queue size
                    true        // discard oldest
                );

                List<MonitoredItemCreateRequest> requests = new ArrayList<MonitoredItemCreateRequest>();
                for (String n: qName) {
                    UaNode node = retrieveCacheEntry(n).node;
                    ReadValueId readValueId = new ReadValueId(node.getNodeId(), AttributeId.Value.uid(), 
                        null, QualifiedName.NULL_VALUE);
                    MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                        readValueId,
                        MonitoringMode.Reporting,
                        parameters
                    );
                    requests.add(request);
                }

                UaSubscription.ItemCreationCallback onItemCreated =
                    (item, id) -> item.setValueConsumer(this::onSubscriptionValue); 

                List<UaMonitoredItem> items = subscription.createMonitoredItems(
                    TimestampsToReturn.Both,
                    requests,
                    onItemCreated
                ).get();              
                
                for (UaMonitoredItem item : items) {
                    if (item.getStatusCode().isGood()) {
                        LOGGER.info("Monitoring for nodeId={} activated", item.getReadValueId().getNodeId());
                    } else {
                        LOGGER.warn("Monitoring: Failed to create item for nodeId={} (status={})",
                            item.getReadValueId().getNodeId(), item.getStatusCode());
                    }
                }
            } catch (UaException | ExecutionException | InterruptedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            try {
                UaSubscription subscription = client.getSubscriptionManager().createSubscription(
                    notificationInterval).get();
                UInteger clientHandle = subscription.nextClientHandle();
            
                EventFilter eventFilter = new EventFilter(
                    new SimpleAttributeOperand[]{
                        new SimpleAttributeOperand(
                            Identifiers.BaseModelChangeEventType,
                            new QualifiedName[]{new QualifiedName(0, "Severity")},
                            AttributeId.Value.uid(),
                            null),
                    },
                    new ContentFilter(null)
                );                
                
                MonitoringParameters parameters = new MonitoringParameters(
                    clientHandle,
                    (double) params.getNotificationInterval(), // sampling interval
                    ExtensionObject.encode(client.getDynamicSerializationContext(), eventFilter),
                    uint(10),   // queue size
                    true        // discard oldest
                );

                List<MonitoredItemCreateRequest> requests = new ArrayList<MonitoredItemCreateRequest>();
                ReadValueId readValueId = new ReadValueId(Identifiers.Server, AttributeId.EventNotifier.uid(), 
                        null, QualifiedName.NULL_VALUE);
                MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                    readValueId,
                    MonitoringMode.Reporting,
                    parameters
                );
                requests.add(request);
                
                UaSubscription.ItemCreationCallback onItemCreated =
                    (item, id) -> item.setEventConsumer(this::onEvent);

                List<UaMonitoredItem> items = subscription.createMonitoredItems(
                    TimestampsToReturn.Both,
                    requests,
                    onItemCreated
                ).get();              
                
                for (UaMonitoredItem item : items) {
                    if (item.getStatusCode().isGood()) {
                        LOGGER.info("Monitoring for nodeId={} activated", item.getReadValueId().getNodeId());
                    } else {
                        LOGGER.warn("Monitoring: Failed to create item for nodeId={} (status={})",
                            item.getReadValueId().getNodeId(), item.getStatusCode());
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new IOException(e);
            }
        }
        
        /**
         * Is called on changing a monitored item.
         * 
         * @param item the monitored item
         * @param value the new data value
         */
        private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
            try {
                DataItem details;
                if (isDetailNotifiedItemEnabled()) {
                    Object nodeId = item.getReadValueId().getNodeId().getIdentifier();
                    details = new DataItem(nodeId, value.getValue());
                } else {
                    details = null;
                }
                received(details);
            } catch (IOException e) {
                LOGGER.info("While triggering reception", e);
            }
        }
        
        /**
         * Is called when a monitoring event occurs.
         * 
         * @param item the monitored item
         * @param var the changed values/variants
         */
        private void onEvent(UaMonitoredItem item, Variant[] var) {
            try {
                // unclear about event details, no hint to changed node while it is there in UAExplorer
                received(null);
            } catch (IOException e) {
                LOGGER.info("While triggering reception", e);
            }
        }

        @Override
        public OpcUaModelAccess stepInto(String name) throws IOException {
            try {
                String n = basePath;
                if (n.length() == 0) {
                    n = name;
                } else {
                    n = n + "/" + name;
                }
                return new OpcUaModelAccess(retrieveCacheEntry(name), n, this, nodes);
            } catch (UaException e) {
                throw new IOException(e);
            }
        }

        @Override
        public OpcUaModelAccess stepOut() {
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
