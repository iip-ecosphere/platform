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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

/**
 * Implements a generic converter from transport stream entries to AAS. Specific types may require an own
 * type/value converter to AAS which can be attached to the converter instance. The actual implementation relies on
 * dedicated entries in a given submodel, which may be resource consuming and cause conflicts with the
 * AAS server when regularly cleaning up the entries. Thus, it is not recommended to use this until the
 * underlying AAS implementation offers application-specific AAS events. The related {@link Watcher}
 * ignores the attachable consumer and uses {@link #doWatch(SubmodelElementCollection, long)} instead to
 * avoid converting submodel elements back to instances of the data type {@code T}.
 * 
 * Recommendation: If adequate, use {@link TransportToWsConverter} instead.
 * 
 * @param <T> the data type
 * @author Holger Eichelberger, SSE
 */
public abstract class TransportToAasConverter<T> extends TransportConverter<T> {

    public static final ValueConverter IDENTITY_CONVERTER = v -> v;
    public static final ValueConverter JSON_CONVERTER = v -> JsonUtils.toJson(v);
    public static final ValueConverter SHORT2INT_CONVERTER = v -> Integer.valueOf((Short) v);
    public static final ValueConverter STRING_CONVERTER = v -> String.valueOf(v);
    public static final ValueConverter ENUM_NAME_CONVERTER = v -> ((Enum<?>) v).name();
    
    private static final Map<Class<?>, TypeConverter> DEFAULT_CONVERTERS = new HashMap<>();
    private static final Set<String> METHODS_TO_IGNORE = new HashSet<>();
    private Map<Class<?>, TypeConverter> converters = new HashMap<>();

    private long timeout = 20 * 60 * 1000; // cleanup after 20 minutes
    private long lastCleanup = System.currentTimeMillis();
    private long cleanupTimeout = 5 * 1000; // when the next cleanup shall be considered
    private long aasFailedTimestamp = -1;
    private long aasFailedTimeout = 30 * 1000;
    
    private String submodelIdShort;
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
        super(transportStream, dataType);
        this.converters.putAll(DEFAULT_CONVERTERS);
        this.submodelIdShort = submodelIdShort;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
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
    
    @Override
    protected void handleNew(T data) {
        // add new record
        long now = System.currentTimeMillis();
        if (isAasEnabled() && aasFailedTimestamp > 0 && now - aasFailedTimestamp < aasFailedTimeout) {
            aasFailedTimestamp = -1; // allow for re-try
        }
        if (isAasEnabled() && aasFailedTimestamp < 0) {
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
                aasFailedTimestamp = now;
            }
        }
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
     * Adds/overwrites a converter.
     * 
     * @param cls the class the converter applies to
     * @param converter the converter instance
     */
    protected void addConverter(Class<?> cls, TypeConverter converter) {
        converters.put(cls, converter);
    }
    
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
     * Creates the generic properties and contents for the given payload object.
     *  
     * @param payloadBuilder the payload builder
     * @param payload the payload to be presented
     * @see #createPayloadEntry(String, Class, Class)
     */
    protected void createPayloadEntries(SubmodelElementCollectionBuilder payloadBuilder, Object payload) {
        if (null != payload) {
            Class<?> cls = payload.getClass();
            for (Method m : cls.getMethods()) {
                if (isGetter(m)) {
                    String fieldName = m.getName();
                    String field = fieldName.substring(PREFIX_GETTER.length());
                    if (isExcludedField(fieldName)) {
                        continue;
                    }
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

    @Override
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
        if (cleanupTimeout > 0 && now - lastCleanup > cleanupTimeout) {
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

    @Override
    public boolean isAasStarted() {
        return aasStarted;
    }

    @Override
    public void start(AasSetup aasSetup) {
        this.aasSetup = aasSetup;
        super.start(aasSetup);
    }

    @Override
    public void stop() {
        super.stop();
        if (isAasEnabled()) {
            try {
                cleanUpAas(AasPartRegistry.retrieveAas(aasSetup, getAasUrn()));
            } catch (IOException e ) {
                LoggerFactory.getLogger(getClass()).error("Cleaning up AAS: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isTraceInAas() {
        return true;
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

    /**
     * Used in {@link AasWatcher} to regularly watch the status entires.
     * 
     * @param coll the collection representing an entry
     * @param lastRun the last run of the watcher
     */
    protected abstract void doWatch(SubmodelElementCollection coll, long lastRun);
    
    /**
     * A regular AAS watcher.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class AasWatcher implements Watcher<T> {

        private Timer timer;
        private int period;
        private long lastRun = System.currentTimeMillis();
        
        /**
         * Creates a watcher instance.
         * 
         * @param period the watching period in ms
         */
        private AasWatcher(int period) {
            this.period = period;
        }
        
        @Override
        public Watcher<T> start() {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        if (null == aas) {
                            // do not populate, we just add/remove in this class
                            aas = AasPartRegistry.retrieveAas(aasSetup, getAasUrn(), false);                    
                        }
                        Submodel submodel = aas.getSubmodel(submodelIdShort);                    
                        submodel.iterate(coll -> {
                            doWatch(coll, lastRun);
                            return true;
                        }, SubmodelElementCollection.class);
                        lastRun = System.currentTimeMillis();
                    } catch (IOException e) {
                        LoggerFactory.getLogger(getClass()).error("Cannot obtain AAS {}: {}", 
                            getAasUrn(), e.getMessage());
                    }
                }
                
            }, 0, period);
            return this;
        }

        @Override
        public Watcher<T> stop() {
            timer.cancel();
            return this;
        }

        @Override
        public void setConsumer(Consumer<T> consumer) {
            // ignore for doWatch
        }
        
    }
    
    @Override
    public Watcher<T> createWatcher(int period) {
        return new AasWatcher(period);
    }

}
