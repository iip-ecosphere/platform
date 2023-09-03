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

package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.ElementContainer;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * Implements a generic converter from transport stream entries to some protocol.
 * 
 * @param <T> the data type
 * @author Holger Eichelberger, SSE
 */
public abstract class TransportConverter<T> {

    public static final String NAME_COLL_ENDPOINTS = "endpoints";
    public static final String NAME_PROP_SCHEMA = "schema";
    public static final String NAME_PROP_HOST = "host";
    public static final String NAME_PROP_PORT = "port";
    public static final String NAME_PROP_PATH = "path";
    public static final String NAME_PROP_URI = "uri";

    protected static final String PREFIX_GETTER = "get";
    
    private TraceRecordReceptionCallback callback;
    private List<IOConsumer<T>> notifier = new ArrayList<>();
    
    private String transportStream;
    private Class<T> dataType;
    private Predicate<T> handleNewFilter = d -> true;
    private Supplier<Boolean> aasEnabledSupplier;
    private Set<String> excludedFields = new HashSet<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Represents a pair of server/converter created together.
     * 
     * @param <T> the data type
     * @author Holger Eichelberger, SSE
     */
    public static class ConverterInstances<T> {
        
        private Server server;
        private TransportConverter<T> converter;

        /**
         * Creates an instance without server.
         * 
         * @param converter the converter
         */
        public ConverterInstances(TransportConverter<T> converter) {
            this(null, converter);
        }

        /**
         * Creates an instance.
         * 
         * @param server the server (may be <b>null</b> for none)
         * @param converter the converter
         */
        protected ConverterInstances(Server server, TransportConverter<T> converter) {
            this.server = server;
            this.converter = converter;
        }

        /**
         * Returns the server.
         * 
         * @return the server (may be <b>null</b> for none)
         */
        public Server getServer() {
            return server;
        }
        
        /**
        * Returns the converter.
        * 
        * @return the converter
        */
        public TransportConverter<T> getConverter() {
            return converter;
        }
        
    }
    
    
    /**
     * Creates a service instance.
     *
     * @param transportStream the transport stream to be observed (name of the stream)
     * @param dataType the type of the data in the transport stream
     */
    public TransportConverter(String transportStream, Class<T> dataType) {
        this(transportStream, dataType, null);
    }

    /**
     * Creates a service instance.
     *
     * @param transportStream the transport stream to be observed (name of the stream)
     * @param dataType the type of the data in the transport stream
     * @param handleNewFilter filter for {@link #handleNew(Object)}, may be <b>null</b>
     */
    public TransportConverter(String transportStream, Class<T> dataType, Predicate<T> handleNewFilter) {
        this.transportStream = transportStream;
        this.dataType = dataType;
        setHandleNewFilter(handleNewFilter);
    }

    /**
     * Sets a filter for {@link #handleNew(Object)}.
     * 
     * @param handleNewFilter filter for {@link #handleNew(Object)}, may be <b>null</b>
     */
    public void setHandleNewFilter(Predicate<T> handleNewFilter) {
        this.handleNewFilter = null == handleNewFilter ? d -> true : handleNewFilter;
    }

    /**
     * Sets the excluded fields for {@link #handleNew(Object)}.
     * 
     * @param excludedFields the excluded fields, may be <b>null</b> or empty for none
     */
    public void setExcludedFields(Set<String> excludedFields) {
        this.excludedFields.clear();
        if (excludedFields != null) {
            this.excludedFields.addAll(excludedFields);
        }
    }
    
    /**
     * Returns whether {@code fieldName} is excluded.
     * 
     * @param fieldName the name of the field
     * @return {@code true} for excluded, {@code false} else
     */
    public boolean isExcludedField(String fieldName) {
        return excludedFields.contains(fieldName);
    }
    
    /**
     * Returns the excluded fields array.
     * 
     * @return the excluded fields as array
     */
    protected String[] getExcludedFieldsArray() {
        return excludedFields.toArray(new String[excludedFields.size()]);
    }

    /**
     * Defines a new notifier which is called when new data arrives. Currently only one notifier is supported.
     * 
     * @param notifier the notifier, ignored if <b>null</b>
     */
    public void addNotifier(IOConsumer<T> notifier) {
        if (null != notifier) {
            this.notifier.add(notifier);
        } else {
            LoggerFactory.getLogger(getClass()).warn("No notifier given. Ignoring call.");
        }
    }
    
    /**
     * Handles a new trace record. Called upon arrival. [protected for mocking]
     * 
     * @param data the trace record data
     */
    protected abstract void handleNew(T data);
    
    /**
     * Handles a new trace record by calling {@link #handleNew(Object)} upon arrival. Entry point for handling new
     * data, calling {@link #handleNew(Object)} to ensure that {@link #notifier} are processed.
     * 
     * @param data the trace record data
     */
    private void handleNewAndNotify(T data) {
        for (IOConsumer<T> n: notifier) {
            try { // notify independently
                n.accept(data);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot inform notifier: {}", e.getMessage());
            }
        }
        if (handleNewFilter.test(data)) {
            handleNew(data);
        }
    }
    
    /**
     * Obtains the value return by {@code method} on {@code payload}.
     * 
     * @param object the object to call the method on
     * @param method the method to call
     * @param field the represented field (for logging)
     * @return the value of {@code field} via {@code method}
     */
    protected Object getValue(Object object, Method method, String field) {
        Object result;
        try {
            result = method.invoke(object);
        } catch (SecurityException | InvocationTargetException | IllegalAccessException e) {
            result = null;
            LoggerFactory.getLogger(getClass()).error(
                "Cannot obtain value of operation {}/field {} of class {} to AAS: {}", 
                method.getName(), field, method.getDeclaringClass().getName(), e.getMessage());
        }
        return result;
    }
    
    /**
     * Allows for application specific payload type names.
     * 
     * @param cls the type
     * @return the mapped name
     */
    protected String mapPayloadType(Class<?> cls) {
        return cls.getName();
    }
    
    /**
     * Returns whether {@code method} is an usual getter.
     * 
     * @param method the method to analyze
     * @return {@code true} for getter, {@code false} else
     */
    protected static boolean isGetter(Method method) {
        int modifier = method.getModifiers();
        boolean pubNonStatic = Modifier.isPublic(modifier) && !Modifier.isStatic(modifier);
        return method.getName().startsWith(PREFIX_GETTER) && method.getParameterCount() == 0 && pubNonStatic; 
    }
    
    /**
     * A trace reception callback calling {@link TransportConverter#handleNew(Object)} 
     * in own threads.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class TraceRecordReceptionCallback implements ReceptionCallback<T> {
        
        @Override
        public void received(T data) {
            executorService.submit(() -> handleNewAndNotify(data));
        }

        @Override
        public Class<T> getType() {
            return dataType;
        }
        
    }
    
    /**
     * Watches for updates.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Watcher<T> {
        
        /**
         * Starts the watcher.
         * 
         * @return <b>this</b>
         */
        public Watcher<T> start();

        /**
         * Stops the watcher.
         * 
         * @return <b>this</b>
         */
        public Watcher<T> stop();

        /**
        * Defines a consumer for the watched information.
        * 
        * @param consumer the consumer or <b>null</b> for none
        */
        public void setConsumer(Consumer<T> consumer);
        
    }
    
    /**
     * Returns the handled data type.
     * 
     * @return the type
     */
    protected final Class<T> getType() {
        return dataType;
    }

    /**
     * Initializes the submodel.
     * 
     * @param smBuilder the submodel builder
     */
    public void initializeSubmodel(SubmodelBuilder smBuilder) {
    }

    /**
     * Starts the transport tracer.
     * 
     * @param aasSetup the AAS setup to use
     */
    public void start(AasSetup aasSetup) {
        callback = new TraceRecordReceptionCallback();
        TransportConnector conn = Transport.createConnector();
        if (null != conn) {
            try {
                conn.setReceptionCallback(transportStream, callback);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Registring transport callback: " + e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("No transport setup, will not listen to trace records.");
        }
    }
    
    /**
     * Returns whether the converter functionality shall be enabled. [requires renaming]
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    protected boolean isAasEnabled() {
        return null == aasEnabledSupplier ? true : aasEnabledSupplier.get();
    }

    /**
     * Changes the optional supplier for the AAS enabled state. [requires renaming]
     * 
     * @param enabledSupplier the new supplier, ignored if <b>null</b>
     */
    public void setAasEnabledSupplier(Supplier<Boolean> enabledSupplier) {
        if (null != enabledSupplier) {
            this.aasEnabledSupplier = enabledSupplier;
        }
    }

    /**
     * Returns whether the AAS was started/startup is done.
     * 
     * @return {@code true} for started, {@code false} else
     */
    public boolean isAasStarted() {
        return true;
    }
    
    /**
     * Changes the timeout until trace events are deleted.
     * 
     * @param timeout the timeout in ms
     */
    public void setTimeout(long timeout) {
        // ignored
    }

    /**
     * Changes the cleanup timeout, i.e., the time between two cleanups.
     * 
     * @param cleanupTimeout the timeout in ms, disables cleanup if negative
     */
    public void setCleanupTimeout(long cleanupTimeout) {
        // ignored
    }
    
    /**
     * Pursues a cleanup of the internal data structures, if applicable.
     * 
     * @return whether a cleanup process was executed (not whether elements were deleted)
     */
    public boolean cleanup() {
        return true; // pretend success
    }

    /**
     * Stops the transport, deletes the AAS.
     */
    public void stop() {
        executorService.shutdown();
        try {
            TransportConnector conn = Transport.getConnector();
            if (null != conn) {
                conn.detachReceptionCallback(transportStream, callback);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Detaching transport connector: " + e.getMessage());
        }
    }
    
    /**
     * Returns the server endpoint to connect to.
     * 
     * @return the server endpoint, may be <b>null</b> for none, in particular if this connector is hosted by an AAS
     */
    public Endpoint getEndpoint() {
        return null;
    }
    
    /**
     * Returns whether traced data is directly stored in the AAS or not.
     * 
     * @return {@code true} for traced data in AAS, {@code false} else
     */
    public boolean isTraceInAas() {
        return false;
    }
    
    /**
     * Creates a watcher instance.
     * 
     * @param period the watching period in ms
     * @return the watcher
     */
    public abstract Watcher<T> createWatcher(int period);
    
    /**
     * Returns an AAS submodel element collection representing an endpoint.
     * 
     * @param parent the parent submodel or element collection
     * @param path the URI path used as id to represent the endpoint
     * @return the submodel element collection or <b>null</b> for none
     */
    public static SubmodelElementCollection getEndpoint(ElementContainer parent, String path) {
        SubmodelElementCollection endpoint = null;
        SubmodelElementCollection endpoints = parent.getSubmodelElementCollection(NAME_COLL_ENDPOINTS);
        if (null != endpoints) {
            endpoint = parent.getSubmodelElementCollection(toAasEndpointId(path));
        }
        return endpoint;
    }
    
    /**
     * Turns a submodel element collection with endpoint information into an endpoint object.
     * 
     * @param endpoint the submodel, e.g., from {@link #getEndpoint(ElementContainer, String))}
     * @return the endpoint object, may be <b>null</b> if the object cannot be created
     */
    public static Endpoint getEndpoint(SubmodelElementCollection endpoint) {
        Endpoint result = null;
        if (null != endpoint) {
            String schema = AasUtils.getPropertyValueAsStringSafe(endpoint, TransportConverter.NAME_PROP_SCHEMA, null);
            String host = AasUtils.getPropertyValueAsStringSafe(endpoint, TransportConverter.NAME_PROP_HOST, null);
            int port = AasUtils.getPropertyValueAsIntegerSafe(endpoint, TransportConverter.NAME_PROP_PORT, 10000);
            String path = AasUtils.getPropertyValueAsStringSafe(endpoint, TransportConverter.NAME_PROP_PATH, null);
            if (null != schema && null != host && null != path) {
                try {
                    result = new Endpoint(Schema.valueOf(schema), host, port, path);
                } catch (IllegalArgumentException e) {
                    LoggerFactory.getLogger(TransportConverter.class).warn("Cannot convert schema {}: {}", 
                        schema, e.getMessage());
                }
            } else {
                LoggerFactory.getLogger(TransportConverter.class).warn("Cannot create endpoint object as information "
                    + "is missing (schema: {}, host: {}, port: {}, path: {})", schema, host, port, path);
            }
        } else {
            LoggerFactory.getLogger(TransportConverter.class).warn("Cannot create endpoint object as submodel "
                + "element collection is null");
        }
        return result;
    }
    
    /**
     * Turns an URI path into an endpoint AAS idShort.
     * 
     * @param path the path
     * @return the idShort
     */
    public static String toAasEndpointId(String path) {
        String id = path;
        while (id.startsWith("/")) {
            id = id.substring(1);
        }
        if (id.length() == 0) {
            id = String.valueOf(System.currentTimeMillis()); // just as fallback
        }
        return AasUtils.fixId(id);
    }
    
    /**
     * Adds an endpoint to a given (endpoint) submodel/elements collection.
     * 
     * @param smBuilder the submodel/elements collection builder
     * @param endpoint the endpoint, may be <b>null</b>
     */
    public static void addEndpointToAas(SubmodelElementContainerBuilder smBuilder, Endpoint endpoint) {
        if (null != endpoint) {
            SubmodelElementCollectionBuilder endpoints = smBuilder.createSubmodelElementCollectionBuilder(
                NAME_COLL_ENDPOINTS, false, false);
            
            SubmodelElementCollectionBuilder eBuilder = smBuilder.createSubmodelElementCollectionBuilder(
                AasUtils.fixId(toAasEndpointId(endpoint.getEndpoint())), false, false);
            
            eBuilder.createPropertyBuilder(NAME_PROP_SCHEMA)
                .setValue(Type.STRING, endpoint.getSchema().name())
                .build();
            eBuilder.createPropertyBuilder(NAME_PROP_HOST)
                .setValue(Type.STRING, endpoint.getHost())
                .build();
            eBuilder.createPropertyBuilder(NAME_PROP_PORT)
                .setValue(Type.INT32, endpoint.getPort())
                .build();
            eBuilder.createPropertyBuilder(NAME_PROP_PATH)
                .setValue(Type.STRING, endpoint.getEndpoint())
                .build();
            eBuilder.createPropertyBuilder(NAME_PROP_URI)
                .setValue(Type.STRING, endpoint.toUri())
                .build();
            
            eBuilder.build();
            endpoints.build();
        }
    }

}
