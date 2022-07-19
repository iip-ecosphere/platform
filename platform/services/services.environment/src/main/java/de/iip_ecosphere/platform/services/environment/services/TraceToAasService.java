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
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ParameterConfigurer;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TransportToAasConverter.TypeConverter;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
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

    private Map<String, ParameterConfigurer<?>> paramConfigurers = new HashMap<>();
    private ApplicationSetup appSetup;
    private YamlArtifact artifact;
    private Converter converter = createConverter();

    /**
     * Creates a service instance.
     *
     * @param app static information about the application
     * @param yaml the service description 
     */
    public TraceToAasService(ApplicationSetup app, YamlService yaml) {
        super(yaml);
        this.appSetup = new ApplicationSetup(app); // prevent later changes in app
        // parameter must be declared in this form in model!
        addParameterConfigurer(new ParameterConfigurer<>(
            "timeout", Long.class, TypeTranslators.LONG, t -> converter.setTimeout(t)));
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public TraceToAasService(String serviceId, InputStream ymlFile) {
        this(new YamlConstructionInfo(serviceId, ymlFile));
    }
    
    /**
     * Intermediary constructor based on {@link YamlConstructionInfo}.
     * 
     * @param info the information instance
     */
    private TraceToAasService(YamlConstructionInfo info) {
        this(info.app, info.service);
    }
    
    /**
     * Represents construction information.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class YamlConstructionInfo {
        private ApplicationSetup app;
        private YamlService service;

        /**
         * Creates an instance by reading {@code yamlFile}.
         * 
         * @param serviceId the service id
         * @param ymlFile the YML file containing the YAML artifact with the service descriptor
         */
        protected YamlConstructionInfo(String serviceId, InputStream ymlFile) {
            YamlArtifact art = YamlArtifact.readFromYamlSafe(ymlFile);
            this.app = art.getApplication();
            this.service = art.getServiceSafe(serviceId);
        }
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
     * Creates the actual converter instance.
     * 
     * @return the converter
     */
    protected Converter createConverter() {
        return new Converter();
    }

    /**
     * Changes the timeout until trace events are deleted.
     * 
     * @param timeout the timeout in ms
     */
    protected void setTimeout(long timeout) {
        converter.setTimeout(timeout);
    }
    
    /**
     * Returns the application setup.
     * 
     * @return the application setup
     */
    public ApplicationSetup getApplicationSetup() {
        return appSetup;
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
        this.converter.addConverter(cls, converter);
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
     * Allows for application specific payload type names.
     * 
     * @param cls the type
     * @return the mapped name
     */
    protected String mapPayloadType(Class<?> cls) {
        return cls.getName();
    }
    
    @Override
    protected ServiceState start() throws ExecutionException {
        boolean ok = converter.start(Starter.getSetup().getAas(), true); // deploy, it's the App AAS
        ServiceState result = super.start();
        if (!ok) {
            result = ServiceState.FAILED;
        }
        return result;
    }

    @Override
    protected ServiceState stop() {
        ServiceState result = super.stop();
        if (!converter.stop()) {
            result = ServiceState.FAILED;
        }
        return result;
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
    
    /**
     * A configured transport to AAS converter for {@link TraceRecord}.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class Converter extends TransportToAasConverter<TraceRecord> {

        /**
         * Creates a configured converter instance.
         */
        public Converter() {
            super(SUBMODEL_TRACES, TraceRecord.TRACE_STREAM, TraceRecord.class);
        }

        @Override
        public String getAasId() {
            return TraceToAasService.this.getAasId();
        }

        @Override
        public String getAasUrn() {
            return TraceToAasService.this.getAasUrn();
        }

        @Override
        protected Function<TraceRecord, String> getSubmodelElementIdFunction() {
            return data -> AasUtils.fixId(data.getSource() + "_" + data.getTimestamp());
        }

        @Override
        public CleanupPredicate getCleanupPredicate() {
            return (coll, timestamp) -> {
                boolean del = false;
                Property prop = coll.getProperty(PROPERTY_TIMESTAMP);
                if (null != prop) {
                    try {
                        Object val = prop.getValue();
                        if (val instanceof Integer) {
                            del = ((Integer) val) < timestamp;
                        } else if (val instanceof Long) {
                            del = ((Long) val) < timestamp;
                        }
                    } catch (ExecutionException e) {
                    }
                }
                return del;
            };
        }
        
        @Override
        protected boolean buildUpAas(AasBuilder aasBuilder) {
            SubmodelBuilder smBuilder = PlatformAas.createNameplate(aasBuilder, appSetup);
            PlatformAas.addSoftwareInfo(smBuilder, appSetup);
            smBuilder.build();
            smBuilder = aasBuilder.createSubmodelBuilder(SUBMODEL_COMMANDS, null);
            augmentCommandsSubmodel(smBuilder);
            smBuilder.build();                
            smBuilder = aasBuilder.createSubmodelBuilder(SUBMODEL_SERVICES, null);
            augmentServicesSubmodel(smBuilder);
            smBuilder.build();
            return true;
        }
        
        @Override
        protected boolean cleanUpAas(Aas aas) {
            aas.delete(aas.getSubmodel(PlatformAas.SUBMODEL_NAMEPLATE));
            aas.delete(aas.getSubmodel(SUBMODEL_COMMANDS));
            return true;
        }
        
        @Override
        protected void populateSubmodelElementCollection(SubmodelElementCollectionBuilder smcBuilder, 
            TraceRecord data) {
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
                createPayloadEntries(payloadBuilder, data.getPayload());
            }
        }
        
        @Override
        protected String mapPayloadType(Class<?> cls) {
            return TraceToAasService.this.mapPayloadType(cls);
        }        
        
    }

}
