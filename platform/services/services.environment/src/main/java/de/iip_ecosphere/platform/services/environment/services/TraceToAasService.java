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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ParameterConfigurer;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter.ConverterInstances;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.services.environment.testing.DataRecorder;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProvider;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.transport.status.TraceRecordSerializer;

/**
 * Implements a generic service that maps {@link TraceRecord} to an (application) AAS.
 * This service is in development/preliminary. The service does not take any data or produce data,
 * it is just meant to create up the trace record AAS entries. It can be used as a sink.
 * 
 * Currently, the service builds up the AAS of an application. However, this functionality
 * shall be moved that the platform is providing the AAS and the service just hooks the traces submodel into.
 * 
 * Can optionally send AAS data to a transport channel (see {@link #createTransport(BasicSerializerProvider)}, 
 * {@link #getAasTransportChannel()}, {@link #getTransportParameter()}).
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceToAasService extends AbstractService {

    public static final String VERSION = "0.1.0";
    public static final String TRANSPORT_CHANNEL_PREFIX = "iip/";
    
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
    private TransportConverter<TraceRecord> converter;
    private TransportConnector outTransport;
    private TransportParameter outTransportParameter;
    private DataRecorder recorder;
    private Server server;

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
        registerParameterConfigurers();
        recorder = createDataRecorder();
        ConverterInstances<TraceRecord> inst = createConverter();
        converter = inst.getConverter();
        converter.setAasEnabledSupplier(() -> isAasEnabled());
        server = inst.getServer();
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
     * Returns the local gateway server.
     * 
     * @return the server instance, may be <b>null</b> for none
     */
    public Server getGatewayServer() {
        return server;
    }
    
    /**
    * Registers own parameter configurers. Called by constructor. Use 
    * {@link #addParameterConfigurer(ParameterConfigurer)} to declare a parameter.
    */
    protected void registerParameterConfigurers() {
    }    
    
    /**
     * Creates an optional data recorder instance. 
     * 
     * @return the data recorder instance, may be <b>null</b> for none
     * @see #createDataRecorderOrig()
     */
    protected DataRecorder createDataRecorder() {
        return createDataRecorderOrig();
    }

    /**
     * Creates a default data recorder instance (writes to target in JSON format). Cannot be overriden to be accessible 
     * to subclasses although {@link #createDataRecorder()} is overridden.  
     * 
     * @return the data recorder instance, may be <b>null</b> for none
     * @see #createDataRecorderOrig()
     */
    protected final DataRecorder createDataRecorderOrig() {
        return new DataRecorder(new File("target/recordings/appAas-" + getId() + "-recorded.txt"), 
            DataRecorder.JSON_FORMATTER);
    }
    
    /**
     * Creates the actual converter instance.
     * 
     * @return the converter, the default one goes for web sockets (due to AAS performance problems)
     */
    protected ConverterInstances<TraceRecord> createConverter() {
        //return new ConverterInstances<TraceRecord>(new Converter());
        return TransportToWsConverter.createInstances(TraceRecord.TRACE_STREAM, TraceRecord.class, 
            getGatewayServer(), Starter.getSetup().getTransport(), this);
    }

    /**
     * Changes the timeout until trace events are deleted.
     * 
     * @param timeout the timeout in ms
     */
    public void setTimeout(long timeout) {
        converter.setTimeout(timeout);
    }
    
    /**
     * Changes the cleanup timeout, i.e., the time between two cleanups.
     * 
     * @param cleanupTimeout the timeout in ms
     */
    public void setCleanupTimeout(long cleanupTimeout) {
        converter.setCleanupTimeout(cleanupTimeout);
    }
    
    /**
     * Pursues a cleanup of the (internally known) AAS.
     * 
     * @return whether a cleanup process was executed (not whether elements were deleted)
     */
    public boolean cleanup() {
        return converter.cleanup();
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
     * Returns whether the AAS is enabled and shall be set up (the default) or whether it is 
     * intentionally deactivated and shall not be started/modified.
     * 
     * @return {@code true} for enabled (default), {@code false} for disabled
     */
    protected boolean isAasEnabled() {
        return true;
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
        if (null != server) {
            server.start();
        }
        
        AasSetup aasSetup = Starter.getSetup().getAas();
        new Thread(() -> { // may block
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
                SubmodelBuilder convSubmodel = aasBuilder.createSubmodelBuilder(SUBMODEL_TRACES, null);
                converter.initializeSubmodel(convSubmodel);
                convSubmodel.build();
                Aas aas = aasBuilder.build();
                List<Aas> aasList = CollectionUtils.addAll(new ArrayList<Aas>(), aas);
                AasPartRegistry.remoteDeploy(aasSetup, aasList);
                converter.start(aasSetup); 
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Creating AAS: " + e.getMessage());
            }
        }).start();
        
        ServiceState result = super.start();
        outTransport = createTransport(getConfiguredSerializationProvider());
        if (null != outTransport) {
            converter.addNotifier(d -> outTransport.asyncSend(getAasTransportChannel(), d));
        }
        if (null != recorder) {
            converter.addNotifier(d -> {
                recorder.record(getAasTransportChannel(), d);
            });        
        }
        return result;
    }

    /**
     * Allows to record arbitrary data.
     * 
     * @param channel the channel, may be empty or <b>null</b>
     * @param data the data to be recorded
     */
    protected void recordData(String channel, Object data) {
        if (null != recorder) {
            recorder.record(channel, data);
        }
    }

    @Override
    protected ServiceState stop() {
        ServiceState result = super.stop();
        if (null != recorder) {
            recorder.close();
        }
        converter.stop();
        Server.stop(server, true);
        return result;
    }
    
    /**
     * Returns whether the AAS was started/startup is done.
     * 
     * @return {@code true} for started, {@code false} else
     */
    public boolean isAasStarted() {
        return converter.isAasStarted();
    }
    
    /**
     * Returns the AAS transport channel.
     * 
     * @return the AAS transport channel (per default, a combination of {@link #TRANSPORT_CHANNEL_PREFIX} and 
     * the {@link ServiceBase#getApplicationId(String)}
     */
    protected String getAasTransportChannel() {
        String appId = ServiceBase.getApplicationId(getId());
        if (appId.length() > 0) {
            appId = "_" + appId;
        }
        return TRANSPORT_CHANNEL_PREFIX + appId + "/traces";
    }
    
    /**
     * Returns a pre-configured serialization provider for the output connector.
     * Please consider registering <b>data type implementations</b> rather than data type interfaces.
     * 
     * @return the provider
     */
    protected BasicSerializerProvider getConfiguredSerializationProvider() {
        BasicSerializerProvider result = new BasicSerializerProvider();
        result.registerSerializer(new TraceRecordSerializer());
        return result;
    }
    
    /**
     * Defines the transport parameter for {@link #createTransport(BasicSerializerProvider)}.
     * 
     * @param transportParameter the transport parameter
     */
    public void setTransportParameter(TransportParameter transportParameter) {
        this.outTransportParameter = transportParameter;
    }
    
    /**
     * Creates an optional transport connector to pump received data out. [factory]
     * 
     * @param serializationProvider pre-configured serialization provider
     * @return the transport connector, may be <b>null</b> for none
     */
    protected TransportConnector createTransport(BasicSerializerProvider serializationProvider) {
        return null;
    }
    
    /**
     * Returns the optional transport connector to pump received data out.
     * 
     * @return the transport connector, may be <b>null</b> for none
     */
    protected TransportConnector getTransport() {
        return outTransport;
    }

    /**
     * Returns the optional transport parameters.
     * 
     * @return the transport parameter, may be <b>null</b> for none
     */
    public TransportParameter getTransportParameter() {
        return outTransportParameter;
    }

    /**
     * Sends data asynchronously via the optional transport connector. Does nothing if there
     * is no transport connector, {@link #createTransport(BasicSerializerProvider)}.
     * 
     * @param channel the channel to send to
     * @param data the data object to send
     */
    protected void sendTransportAsync(String channel, Object data) {
        if (null != outTransport) {
            try {
                outTransport.asyncSend(channel, data);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).warn("Cannot send to outTransport {}", e.getMessage());
            }
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
     * May not be called if {@link Converter#setAasEnabledSupplier(Supplier) AAS enabled supplier} signals
     * that there shall not be an AAS.
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
     * Retrieves the App AAS.
     * 
     * @return the AAS
     * @throws IOException if the App AAS cannot be retrieved, in particular if 
     * {@link Converter#setAasEnabledSupplier(Supplier) AAS enabled supplier} signals that there shall not be an AAS.
     */
    protected Aas retrieveAas() throws IOException {
        return AasPartRegistry.retrieveAas(Starter.getSetup().getAas(), getAasUrn());
    }
    
    /**
     * Returns whether traced data is directly stored in the AAS or not.
     * 
     * @return {@code true} for traced data in AAS, {@code false} else
     */
    public boolean isTraceInAas() {
        return converter.isTraceInAas();
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
        protected boolean cleanUpAas(Aas aas) {
            aas.delete(aas.getSubmodel(PlatformAas.SUBMODEL_NAMEPLATE));
            aas.delete(aas.getSubmodel(SUBMODEL_COMMANDS));
            // unclear how to get rid of AAS itself
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
                .setValue(Type.INT64, data.getTimestamp())
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

        @Override
        protected void doWatch(SubmodelElementCollection coll, long lastRun) {
            System.out.println(coll); // preliminary
        }        
        
    }

}
