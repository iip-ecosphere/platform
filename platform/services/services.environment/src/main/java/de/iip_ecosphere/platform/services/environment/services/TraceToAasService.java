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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ParameterConfigurer;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

/**
 * Implements a generic service that maps {@link TraceRecord} to an (application) AAS.
 * This service is in development/preliminary. The service does not take any data or produce data,
 * it is just meant to create up the trace record AAS entries. It can be used as a sink.
 * 
 * Currently, the service builds up the AAS of an application. However, this functionality
 * shall be moved that the platform is providing the AAS and the service just hooks the traces submodel into.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceToAasService extends AbstractService {

    public static final String VERSION = "0.1.0";
    
    public static final String SUBMODEL_TRACES = "Traces";
    public static final String SUBMODEL_COMMANDS = "Commands"; // commands from extern towards platform
    public static final String SUBMODEL_SERVICES = "Services";
    public static final String PROPERTY_SOURCE = "Source";
    public static final String PROPERTY_ACTION = "Action";
    public static final String PROPERTY_TIMESTAMP = "Timestamp";
    public static final String PROPERTY_PAYLOAD_TYPE = "PayloadType";
    public static final String PROPERTY_PAYLOAD = "Payload";

    public static final ValueConverter IDENTITY_CONVERTER = v -> v;
    public static final ValueConverter JSON_CONVERTER = v -> JsonUtils.toJson(v);

    private static final Map<Class<?>, TypeConverter> DEFAULT_CONVERTERS = new HashMap<>();
    private static final String PREFIX_GETTER = "get";

    private Map<Class<?>, TypeConverter> converters = new HashMap<>();
    private Map<String, ParameterConfigurer<?>> paramConfigurers = new HashMap<>();
    private ApplicationSetup appSetup;
    private YamlArtifact artifact;
    private long timeout = 60 * 60 * 1000; // cleanup after 1 hour
    private long lastCleanup = System.currentTimeMillis();
    private long cleanupTimeout = 5 * 1000;
    private TraceRecordReceptionCallback callback;
    
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
        DEFAULT_CONVERTERS.put(int[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(long[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(float[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(double[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(byte[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
        DEFAULT_CONVERTERS.put(boolean[].class, new TypeConverter(Type.STRING, JSON_CONVERTER));
    }

    /**
     * Creates a service instance.
     *
     * @param app static information about the application
     * @param yaml the service description 
     */
    public TraceToAasService(ApplicationSetup app, YamlService yaml) {
        super(yaml);
        this.converters.putAll(DEFAULT_CONVERTERS);
        this.appSetup = new ApplicationSetup(app); // prevent later changes in app
        // parameter must be declared in this form in model!
        addParameterConfigurer(new ParameterConfigurer<>(
            "timeout", Long.class, TypeTranslators.LONG, t -> timeout = t));
    }

    /**
     * Creates a service instance.
     *
     * @param artifact static information about the artifact this service is member of
     * @param serviceId the id of the service
     */
    public TraceToAasService(YamlArtifact artifact, String serviceId) {
        this(artifact.getApplication(), artifact.getService(serviceId));
        this.artifact = artifact;
    }

    /**
     * Adds a parameter configurer.
     * 
     * @param <T> the type of the parameter
     * @param configurer the configurer instance, ignored if <b>null</b>
     */
    protected <T> void addParameterConfigurer(ParameterConfigurer<T> configurer) {
        if (null != configurer) {
            paramConfigurers.put(configurer.getName(), configurer);
        }
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
    public String getAasId() {
        return AasUtils.fixId("application_" + appSetup.getId());
    }

    /**
     * Returns the AAS URN of the AAS represented by this service/application.
     * 
     * @return the URN
     */
    public String getAasUrn() {
        return  "urn:::AAS:::" + getAasId() + "#";
    }
    
    /**
     * Encapsulates a Java-to-AAS type converter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TypeConverter implements ValueConverter {
        
        private ValueConverter conv;
        private Type type;
        
        /**
         * Creates the converter instance.
         * 
         * @param type the AAS type
         * @param conv the value converter
         */
        private TypeConverter(Type type, ValueConverter conv) {
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
    private interface ValueConverter {

        /**
         * Performs the conversion.
         * 
         * @param value the value to convert
         * @return the converted value
         */
        Object convert(Object value);
        
    }
    
    /**
     * Handles a new trace record and cleans up outdated ones.
     * 
     * @param data the trace record data
     */
    private void handleNew(TraceRecord data) {
        // add new record
        try {
            Aas aas = AasPartRegistry.retrieveAas(Starter.getSetup().getAas(), getAasUrn());
            SubmodelBuilder smBuilder = aas.createSubmodelBuilder(SUBMODEL_TRACES, null);

            SubmodelElementCollectionBuilder smcBuilder = smBuilder.createSubmodelElementCollectionBuilder(
                AasUtils.fixId(data.getSource() + "_" + data.getTimestamp()), true, true);
            smcBuilder.createPropertyBuilder(PROPERTY_SOURCE)
                .setValue(Type.STRING, data.getSource())
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ACTION)
                .setValue(Type.STRING, data.getAction())
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_TIMESTAMP)
                .setValue(Type.INTEGER, data.getTimestamp())
                .build();
            if (null != data.getPayload()) {
                Class<?> cls = data.getPayload().getClass();
                smcBuilder.createPropertyBuilder(PROPERTY_PAYLOAD_TYPE)
                    .setValue(Type.STRING, mapPayloadType(cls))
                    .build();
                SubmodelElementCollectionBuilder payloadBuilder = smcBuilder
                    .createSubmodelElementCollectionBuilder(PROPERTY_PAYLOAD, false, false);
                for (Method m : cls.getMethods()) {
                    if (isGetter(m)) {
                        String field = m.getName().substring(PREFIX_GETTER.length());
                        TypeConverter tConv = converters.get(m.getReturnType());
                        if (null != tConv) {
                            try {
                                payloadBuilder.createPropertyBuilder(AasUtils.fixId(field))
                                    .setValue(tConv.getType(), tConv.convert(m.invoke(data.getPayload())))
                                    .build();
                            } catch (SecurityException | InvocationTargetException | IllegalAccessException e) {
                                LoggerFactory.getLogger(getClass()).error(
                                    "Cannot map value of operation {}/field {} to AAS: {}", 
                                    m.getName(), field, e.getMessage());
                            }
                        } else {
                            LoggerFactory.getLogger(getClass()).warn(
                                "Cannot map value of operation {}/field {} to AAS: No converter is defined", 
                                m.getName(), field);
                        }
                    }
                }
                payloadBuilder.build();
            }
            smcBuilder.build();
            smBuilder.build();
            cleanup(aas);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error(
                "Cannot obtain AAS {}: {}", getAasUrn(), e.getMessage());
        }
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
     * Cleans up outdated trace entries.
     * 
     * @param aas the AAS to clean up
     */
    private void cleanup(Aas aas) {
        // remove outdated ones
        long now = System.currentTimeMillis();
        if (now - lastCleanup > cleanupTimeout) {
            long timestamp = now - timeout;
            Submodel sm = aas.getSubmodel(SUBMODEL_TRACES);
            List<SubmodelElement> delete = new ArrayList<>();
            for (SubmodelElement elt : sm.submodelElements()) {
                if (elt instanceof SubmodelElementCollection) {
                    SubmodelElementCollection coll = (SubmodelElementCollection) elt;
                    Property prop = coll.getProperty(PROPERTY_TIMESTAMP);
                    if (null != prop) {
                        try {
                            Object val = prop.getValue();
                            boolean del = false;
                            if (val instanceof Integer) {
                                del = ((Integer) val) < timestamp;
                            } else if (val instanceof Long) {
                                del = ((Long) val) < timestamp;
                            }
                            if (del) {
                                delete.add(elt);
                            }
                        } catch (ExecutionException e) {
                            
                        }
                    }
                }
            }
            for (SubmodelElement elt : delete) {
                sm.delete(elt);
            }
            lastCleanup = now;
        }
    }

    /**
     * A trace reception callback calling {@link TraceToAas TraceToAasService#handleNew(TraceRecord)} in own threads.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class TraceRecordReceptionCallback implements ReceptionCallback<TraceRecord> {
        
        @Override
        public void received(TraceRecord data) {
            new Thread(() -> handleNew(data)).start(); // thread pool?
        }

        @Override
        public Class<TraceRecord> getType() {
            return TraceRecord.class;
        }
        
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        switch (state) {
        case STARTING:
            try {
                AasFactory factory = AasFactory.getInstance();
                AasBuilder aasBuilder = factory.createAasBuilder(getAasId(), getAasUrn());
                SubmodelBuilder smBuilder = PlatformAas.createNameplate(aasBuilder, appSetup);
                PlatformAas.addSoftwareInfo(smBuilder, appSetup);
                smBuilder.build();
                smBuilder = aasBuilder.createSubmodelBuilder(SUBMODEL_COMMANDS, null);
                augmentCommandsSubmodel(smBuilder);
                smBuilder.build();                
                smBuilder = aasBuilder.createSubmodelBuilder(SUBMODEL_SERVICES, null);
                augmentServicesSubmodel(smBuilder);
                smBuilder.build();
                aasBuilder.createSubmodelBuilder(SUBMODEL_TRACES, null).build();
                List<Aas> aasList = CollectionUtils.addAll(new ArrayList<Aas>(), aasBuilder.build());
                AasPartRegistry.remoteDeploy(Starter.getSetup().getAas(), aasList);
                callback = new TraceRecordReceptionCallback();
                Transport.createConnector().setReceptionCallback(TraceRecord.TRACE_STREAM, callback);
                super.setState(ServiceState.RUNNING);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Creating AAS: " + e.getMessage());
                super.setState(ServiceState.FAILED);
            }
            break;
        case STOPPING:
            super.setState(state);
            try {
                TransportConnector conn = Transport.getConnector();
                if (null != conn) {
                    conn.detachReceptionCallback(TraceRecord.TRACE_STREAM, callback);
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Detaching transport connector: " + e.getMessage());
            }
            try {
                // unclear how to get rid of AAS itself
                Aas aas = AasPartRegistry.retrieveAas(Starter.getSetup().getAas(), getAasUrn());
                aas.delete(aas.getSubmodel(SUBMODEL_TRACES));
                aas.delete(aas.getSubmodel(PlatformAas.SUBMODEL_NAMEPLATE));
                aas.delete(aas.getSubmodel(SUBMODEL_COMMANDS));
            } catch (IOException e ) {
                LoggerFactory.getLogger(getClass()).error("Cleaning up AAS: " + e.getMessage());
                super.setState(ServiceState.FAILED);
            }
            super.setState(ServiceState.STOPPED);
            break;
        default:
            super.setState(state);
            break;
        }
    }

    /**
     * Adds elements to the services submodel if available.
     * 
     * @param smBuilder the submodel builder
     */
    private void augmentServicesSubmodel(SubmodelBuilder smBuilder) {
        if (null != artifact) {
            AasFactory factory = AasFactory.getInstance();
            try {
                Registry reg = factory.obtainRegistry(Starter.getSetup().getAas().getRegistryEndpoint());
                for (YamlService s : artifact.getServices()) {
                    String ep = reg.getEndpoint(AasUtils.fixId("service_" + s.getId()));
                    if (null == ep) {
                        ep = "";
                    }
                    smBuilder.createPropertyBuilder(AasUtils.fixId(s.getId()))
                        .setValue(Type.STRING, ep)
                        .build();
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Building services submodel: {}", e.getMessage());
            }
        }
    }

    /**
     * Allows adding application-specific elements to the command submodel, e.g., operations.
     * 
     * @param smBuilder the builder, do not call {@link SubmodelBuilder#build()} in here!
     */
    protected void augmentCommandsSubmodel(SubmodelBuilder smBuilder) {
    }

    @Override
    public void migrate(String resourceId) throws ExecutionException {
    }

    @Override
    public void update(URI location) throws ExecutionException {
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
    }

    @Override
    public ParameterConfigurer<?> getParameterConfigurer(String paramName) {
        return paramConfigurers.get(paramName);
    }

}
