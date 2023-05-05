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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * Implements a generic converter from transport stream entries to AAS.
 * 
 * @param <T> the data type
 * @author Holger Eichelberger, SSE
 */
public abstract class TransportToAasConverter<T> {

    public static final ValueConverter IDENTITY_CONVERTER = v -> v;
    public static final ValueConverter JSON_CONVERTER = v -> JsonUtils.toJson(v);
    public static final ValueConverter SHORT2INT_CONVERTER = v -> Integer.valueOf((Short) v);
    public static final ValueConverter STRING_CONVERTER = v -> String.valueOf(v);
    public static final ValueConverter ENUM_NAME_CONVERTER = v -> ((Enum<?>) v).name();
    
    private static final Map<Class<?>, TypeConverter> DEFAULT_CONVERTERS = new HashMap<>();
    private static final String PREFIX_GETTER = "get";
    private static final Set<String> METHODS_TO_IGNORE = new HashSet<>();

    private Map<Class<?>, TypeConverter> converters = new HashMap<>();
    private long timeout = 20 * 60 * 1000; // cleanup after 20 minutes
    private long lastCleanup = System.currentTimeMillis();
    private long cleanupTimeout = 5 * 1000; // when the next cleanup shall be considered
    private TraceRecordReceptionCallback callback;
    private List<IOConsumer<T>> notifier = new ArrayList<>();
    private Supplier<Boolean> aasEnabledSupplier = () -> true;
    
    private String submodelIdShort;
    private String transportStream;
    private Class<T> dataType;
    private AasSetup aasSetup;
    private boolean aasStarted;
    private transient Aas aas; // temporary, cache
    
    static {
        DEFAULT_CONVERTERS.put(String.class, new TypeConverter(Type.STRING, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Boolean.TYPE, new TypeConverter(Type.BOOLEAN, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Boolean.class, new TypeConverter(Type.BOOLEAN, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Integer.TYPE, new TypeConverter(Type.INTEGER, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Integer.class, new TypeConverter(Type.INTEGER, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Long.TYPE, new TypeConverter(Type.INTEGER, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Long.class, new TypeConverter(Type.INTEGER, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Float.TYPE, new TypeConverter(Type.DOUBLE, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Float.class, new TypeConverter(Type.DOUBLE, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Double.TYPE, new TypeConverter(Type.DOUBLE, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Double.class, new TypeConverter(Type.DOUBLE, IDENTITY_CONVERTER));
        DEFAULT_CONVERTERS.put(Short.TYPE, new TypeConverter(Type.INTEGER, SHORT2INT_CONVERTER));
        DEFAULT_CONVERTERS.put(Short.class, new TypeConverter(Type.INTEGER, SHORT2INT_CONVERTER));
        DEFAULT_CONVERTERS.put(int[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(long[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(float[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(double[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(byte[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(boolean[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(String[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        
        // fallback, nothing specific, just turn to JSON
        DEFAULT_CONVERTERS.put(Object.class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        
        METHODS_TO_IGNORE.add("getClass");
    }

    /**
     * Creates a service instance.
     *
     * @param submodelIdShort the id short of the submodel to be maintained
     * @param transportStream the transport stream to be observed (name of the stream)
     * @param dataType the type of the data in the transport stream
     */
    public TransportToAasConverter(String submodelIdShort, String transportStream, Class<T> dataType) {
        this.converters.putAll(DEFAULT_CONVERTERS);
        this.submodelIdShort = submodelIdShort;
        this.transportStream = transportStream;
        this.dataType = dataType;
    }
    
    /**
     * Changes the optional supplier for the AAS enabled state.
     * 
     * @param enabledSupplier the new supplier, ignored if <b>null</b>
     */
    public void setAasEnabledSupplier(Supplier<Boolean> enabledSupplier) {
        if (null != enabledSupplier) {
            this.aasEnabledSupplier = enabledSupplier;
        }
    }
    
    /**
     * Returns whether the AAS functionality shall be enabled.
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    protected boolean isAasEnabled() {
        return aasEnabledSupplier.get();
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
     * Changes the timeout until trace events are deleted.
     * 
     * @param timeout the timeout in ms
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Changes the cleanup timeout, i.e., the time between two cleanups.
     * 
     * @param cleanupTimeout the timeout in ms
     */
    public void setCleanupTimeout(long cleanupTimeout) {
        this.cleanupTimeout = cleanupTimeout;
    }
    
    /**
     * Returns the timeout.
     * 
     * @return the timeout
     */
    public long getTimeout() { 
        return timeout;
    }
    
    /**
     * Adds/overwrites a converter.
     * 
     * @param cls the class the converter applies to
     * @param converter the converter instance
     */
    protected void addConverter(Class<?> cls, TypeConverter converter) {
        converters.put(cls, converter);
    }
    
    /**
     * Returns the AAS idShort of the AAS represented by this service/application.
     * 
     * @return the idShort
     */
    public abstract String getAasId();

    /**
     * Returns the AAS URN of the AAS represented by this service/application.
     * 
     * @return the URN
     */
    public abstract String getAasUrn();
    
    /**
     * Encapsulates a Java-to-AAS type converter.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TypeConverter implements ValueConverter {
        
        private ValueConverter conv;
        private Type type;
        
        /**
         * Creates the converter instance.
         * 
         * @param type the AAS type
         * @param conv the value converter
         */
        public TypeConverter(Type type, ValueConverter conv) {
            this.type = type;
            this.conv = conv;
        }
        
        @Override
        public Object convert(Object value) {
            return conv.convert(value);
        }
        
        /**
         * Returns the AAS type.
         * 
         * @return the AAS type
         */
        public Type getType() {
            return type;
        }
        
    }

    /**
     * Converts a Java value to an AAS value.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ValueConverter {

        /**
         * Performs the conversion.
         * 
         * @param value the value to convert
         * @return the converted value
         */
        public Object convert(Object value);
        
    }
    
    /**
     * Handles a new trace record and cleans up outdated ones. Called upon arrival. [protected for mocking]
     * 
     * @param data the trace record data
     * @see #getSubmodelElementIdFunction()
     * @see #populateSubmodelElementCollection(SubmodelElementCollectionBuilder, Object)
     */
    protected void handleNew(T data) {
        // add new record
        if (isAasEnabled()) {
            try {
                if (null == aas) {
                    // do not populate, we just add/remove in this class
                    aas = AasPartRegistry.retrieveAas(aasSetup, getAasUrn(), false);                    
                }
                // bypass without propagation
                aas.getSubmodel(submodelIdShort).create(b -> {
                    SubmodelElementCollectionBuilder smcBuilder = b.createSubmodelElementCollectionBuilder(
                        getSubmodelElementIdFunction().apply(data), true, true); 
                    populateSubmodelElementCollection(smcBuilder, data);
                    smcBuilder.build();
                }, false);
                cleanup(aas);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot obtain AAS {}: {}", getAasUrn(), e.getMessage());
            }
        }
    }
    
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
        handleNew(data);
    }
    
    
    /**
     * Creates the submodel element representing a single received data value.
     *  
     * @param smcBuilder the builder for the submodel element collection representing the data value
     * @param data the data that may be used to create the element
     */
    protected abstract void populateSubmodelElementCollection(SubmodelElementCollectionBuilder smcBuilder, T data);
    
    /**
     * Returns a function turning a data instance into an id of the submodel representing the data instance.
     * 
     * @return the function
     */
    protected abstract Function<T, String> getSubmodelElementIdFunction();
    
    /**
     * Creates the generic properties and contents for the given payload object.
     *  
     * @param payloadBuilder the payload builder
     * @param payload the payload to be presented
     */
    protected void createPayloadEntries(SubmodelElementCollectionBuilder payloadBuilder, Object payload) {
        if (null != payload) {
            Class<?> cls = payload.getClass();
            for (Method m : cls.getMethods()) {
                if (isGetter(m)) {
                    String field = m.getName().substring(PREFIX_GETTER.length());
                    Class<?> valueCls = null;
                    Object value = null;
                    TypeConverter tConv = converters.get(m.getReturnType());
                    if (null == tConv) { // not found, could be Object, try via actual type
                        value = getValue(payload, m, field);
                        if (null != value) {
                            valueCls = value.getClass();
                            tConv = converters.get(valueCls);
                        }
                    }
                    if (null != tConv) {
                        if (null == valueCls) { // not called so far
                            value = getValue(payload, m, field);
                        }
                        payloadBuilder.createPropertyBuilder(AasUtils.fixId(field))
                            .setValue(tConv.getType(), tConv.convert(value))
                            .build();
                    } else {
                        if (!METHODS_TO_IGNORE.contains(m.getName())) {
                            String type = cls.getName();
                            if (null != valueCls) {
                                type += "/" + valueCls;
                            }
                            LoggerFactory.getLogger(getClass()).warn(
                                "Cannot map value of operation {}/field {} of type {} to AAS: No converter defined", 
                                m.getName(), field, type);
                        }
                    }
                }
            }
            payloadBuilder.build();
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
    private Object getValue(Object object, Method method, String field) {
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
    private static boolean isGetter(Method method) {
        int modifier = method.getModifiers();
        boolean pubNonStatic = Modifier.isPublic(modifier) && !Modifier.isStatic(modifier);
        return method.getName().startsWith(PREFIX_GETTER) && method.getParameterCount() == 0 && pubNonStatic; 
    }
    
    /**
     * Pursues a cleanup of the (internally known) AAS.
     * 
     * @return whether a cleanup process was executed (not whether elements were deleted)
     */
    public boolean cleanup() {
        boolean done = false;
        if (null != aas) {
            done = cleanup(aas);
        }
        return done;
    }
    
    /**
     * Cleans up outdated trace entries. Called in {@link #handleNew(Object)} if regular input is expected,
     * may be called regularly by an external timer.
     * 
     * @param aas the AAS to clean up
     * @return whether a cleanup process was executed (not whether elements were deleted)
     * @see #getCleanupPredicate()
     * @see #cleanup()
     */
    public boolean cleanup(Aas aas) {
        // remove outdated ones
        boolean done = false;
        long now = System.currentTimeMillis();
        if (now - lastCleanup > cleanupTimeout) {
            long timestamp = now - timeout;
            Submodel sm = aas.getSubmodel(submodelIdShort);
            final CleanupPredicate delPred = getCleanupPredicate();
            sm.iterate(e -> {
                boolean cont;
                if (delPred.test(e, timestamp)) {
                    sm.deleteElement(e);
                    cont = true;
                } else {
                    cont = false; // assumption sorted
                }
                return cont;
            }, SubmodelElementCollection.class);
            lastCleanup = now;
            done = true;
        }
        return done;
    }

    /**
     * Predicate to determine whether a submodel elements collection shall be deleted.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface CleanupPredicate {

        /**
         * Returns whether {@code coll} based on the given {@code borderTimestamp} shall be deleted.
         * 
         * @param coll the collection
         * @param borderTimestamp the maximum timestamp
         * @return {@code true} if the collection shall be deleted, {@code false} else
         */
        public boolean test(SubmodelElementCollection coll, long borderTimestamp);
        
    }

    /**
     * Returns the cleanup predicate.
     * 
     * @return the predicate
     */
    public abstract CleanupPredicate getCleanupPredicate();

    /**
     * A trace reception callback calling {@link TransportToAasConverter#handleNew(Object)} 
     * in own threads.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class TraceRecordReceptionCallback implements ReceptionCallback<T> {
        
        @Override
        public void received(T data) {
            new Thread(() -> handleNewAndNotify(data)).start(); // thread pool?
        }

        @Override
        public Class<T> getType() {
            return dataType;
        }
        
    }

    /**
     * Returns whether the AAS was started/startup is done.
     * 
     * @return {@code true} for started, {@code false} else
     */
    public boolean isAasStarted() {
        return aasStarted;
    }

    /**
     * Starts the transport tracer.
     * 
     * @param aasSetup the AAS setup to use
     * @param deploy whether the AAS represented by this converter shall be deployed
     */
    public void start(AasSetup aasSetup, boolean deploy) {
        this.aasSetup = aasSetup;
        if (isAasEnabled()) {
            new Thread(() -> { // may block
                try {
                    AasFactory factory = AasFactory.getInstance();
                    AasBuilder aasBuilder = factory.createAasBuilder(getAasId(), getAasUrn());
                    buildUpAas(aasBuilder);
                    aasBuilder.createSubmodelBuilder(submodelIdShort, null).build();
                    Aas aas = aasBuilder.build();
                    if (deploy) {
                        List<Aas> aasList = CollectionUtils.addAll(new ArrayList<Aas>(), aas);
                        AasPartRegistry.remoteDeploy(aasSetup, aasList);
                    }
                    this.aasStarted = true;
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Creating AAS: " + e.getMessage());
                }
            }).start();
        }
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
     * Builds up the AAS. May not be called if {@link #setAasEnabledSupplier(Supplier) AAS enabled supplier} signals
     * that there shall not be an AAS.
     * 
     * @param aasBuilder the AAS builder to use
     * @return {@code true} for success, {@code false} else
     */
    protected boolean buildUpAas(AasBuilder aasBuilder) {
        return true;
    }

    /**
     * Stops the transport, deletes the AAS.
     */
    public void stop() {
        try {
            TransportConnector conn = Transport.getConnector();
            if (null != conn) {
                conn.detachReceptionCallback(transportStream, callback);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Detaching transport connector: " + e.getMessage());
        }
        if (isAasEnabled()) {
            try {
                Aas aas = AasPartRegistry.retrieveAas(aasSetup, getAasUrn());
                aas.delete(aas.getSubmodel(submodelIdShort));
                cleanUpAas(aas);
            } catch (IOException e ) {
                LoggerFactory.getLogger(getClass()).error("Cleaning up AAS: " + e.getMessage());
            }
        }
    }

    /**
     * Cleans up the AAS. Last action, may delete the AAS itself. May not be called if 
     * {@link #setAasEnabledSupplier(Supplier) AAS enabled supplier} signals that there shall not be an AAS.
     * 
     * @param aas the AAS to clean up
     * @return {@code true} for success, {@code false} else
     */
    protected boolean cleanUpAas(Aas aas) {
        return true;
    }

}
