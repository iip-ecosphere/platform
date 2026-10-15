/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ChanneledConnectorOutputTypeAdapter.ChanneledSerializer;
import de.iip_ecosphere.platform.support.metrics.MetricsProvider;

/**
 * Implements a default (type-safe) connector factory descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultConnectorFactoryDescriptor implements ConnectorFactoryDescriptor {

    private Map<String, ConnectorFactoryCreator> creators = new HashMap<>();

    /**
     * Adds a creator. To be called in the constructor of the descriptor.
     * 
     * @param serviceId the service ID, ignored if <b>null</b>
     * @param creator the creator, ignored if <b>null</b>
     */
    protected void addCreator(String serviceId, ConnectorFactoryCreator creator) {
        if (null != serviceId && null != creator) {
            creators.put(serviceId, creator);
        }
    }
    
    /**
     * Supplies with input/output path.
     * 
     * @author Holger Eichelberger, SSE
     */
    @FunctionalInterface
    protected interface PathSupplier<R> {

        /**
         * Supplies the value.
         * 
         * @param metrics the metrics provider to use, <b>null</b> for no metric measurements
         * @param log the log file to use to record individual measurements in experiments, <b>null</b> for no logging.
         *     Only becomes active if {@code metrics} is not <b>null</b>
         * @param inPathSupplier function returning the actual input base path to use for data accesses, may be 
         *     <b>null</b>
         * @param outPathSupplier function returning the actual output base path to use for data accesses, may be 
         *     <b>null</b>
         * @return the value
         */
        public R get(MetricsProvider metrics, File log, Supplier<String> inPathSupplier, 
            Supplier<String> outPathSupplier);
        
    }
    
    /**
     * A protocol supplier.

     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @author Holger Eichelberger, SSE
     */
    protected abstract static class ProtocolAdapterSupplier<O, I, CO, CI> {

        protected Class<O> oClass;
        protected Class<I> iClass;
        protected Class<CO> coClass;
        protected Class<CI> ciClass;

        /**
         * Creates a supplier with type instances.
         * 
         * @param oClass the type for {@code O}
         * @param iClass the type for {@code I}
         * @param coClass the type for {@code CO}
         * @param ciClass the type for {@code CI}
         */
        protected ProtocolAdapterSupplier(Class<O> oClass, Class<I> iClass, Class<CO> coClass, Class<CI> ciClass) {
            this.oClass = oClass;
            this.iClass = iClass;
            this.coClass = coClass;
            this.ciClass = ciClass;
        }

        // checkstyle: stop parameter number check

        /**
         * Returns an instance if apparently type-compatible.
         * 
         * @param <OT> the desired output type from the underlying machine/platform
         * @param <IT> the desired input type to the underlying machine/platform
         * @param <COT> the desired output type of the connector
         * @param <CIT> the desired input type of the connector
         * @param metrics the metrics provider to use, <b>null</b> for no metric measurements
         * @param log the log file to use to record individual measurements in experiments, <b>null</b> for no logging.
         *     Only becomes active if {@code metrics} is not <b>null</b>
         * @param inPathSupplier function returning the actual input base path to use for data accesses, may be 
         *     <b>null</b>
         * @param outPathSupplier function returning the actual output base path to use for data accesses, may be 
         *     <b>null</b>
         * @param oClass the type for {@code OT}
         * @param iClass the type for {@code IT}
         * @param coClass the type for {@code COT}
         * @param ciClass the type for {@code CIT}
         * @return the instance or <b>null</b> if it is not possible to create/cast an instance
         */
        public abstract <OT, IT, COT, CIT> ProtocolAdapter<OT, IT, COT, CIT>[] get(MetricsProvider metrics, File log, 
            Supplier<String> inPathSupplier, Supplier<String> outPathSupplier, Class<OT> oClass, Class<IT> iClass, 
            Class<COT> coClass, Class<CIT> ciClass);

        // checkstyle: resume parameter number check

    }

    /**
     * Type-safe protocol adapter supplier for multiple adapters. Doesn't work without as Java does not allow casting 
     * from/to a supplier with inner wildcard type parameters.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @author Holger Eichelberger, SSE
     */
    protected static class SinglePathAdapterSupplier<O, I, CO, CI> extends ProtocolAdapterSupplier<O, I, CO, CI> {

        private PathSupplier<ProtocolAdapter<O, I, CO, CI>> protoSupplier;
       
        /**
         * Creates a supplier with type instances.
         * 
         * @param protoSupplier the supplier
         * @param oClass the type for {@code O}
         * @param iClass the type for {@code I}
         * @param coClass the type for {@code CO}
         * @param ciClass the type for {@code CI}
         */
        protected SinglePathAdapterSupplier(PathSupplier<ProtocolAdapter<O, I, CO, CI>> protoSupplier, 
            Class<O> oClass, Class<I> iClass, Class<CO> coClass, Class<CI> ciClass) {
            super(oClass, iClass, coClass, ciClass);
            this.protoSupplier = protoSupplier;
        }
        
        // checkstyle: stop parameter number check

        @Override
        @SuppressWarnings("unchecked")
        public <OT, IT, COT, CIT> ProtocolAdapter<OT, IT, COT, CIT>[] get(MetricsProvider metrics, File log, 
            Supplier<String> inPathSupplier, Supplier<String> outPathSupplier, Class<OT> oClass, Class<IT> iClass, 
            Class<COT> coClass, Class<CIT> ciClass) {
            ProtocolAdapter<OT, IT, COT, CIT>[] result = null;
            if (oClass.isAssignableFrom(this.oClass) && iClass.isAssignableFrom(this.iClass) 
                && coClass.isAssignableFrom(this.coClass) && ciClass.isAssignableFrom(this.ciClass)) {
                result = new ProtocolAdapter[1];
                result[0] = (ProtocolAdapter<OT, IT, COT, CIT>) protoSupplier.get(metrics, log, 
                    inPathSupplier, outPathSupplier);
            }
            return result;
        }

        // checkstyle: resume parameter number check

    }

    /**
     * Type-safe protocol adapter supplier for multiple adapters. Doesn't work without as Java does not allow casting 
     * from/to a supplier with inner wildcard type parameters.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @author Holger Eichelberger, SSE
     */
    protected static class ArrayPathAdapterSupplier<O, I, CO, CI> extends ProtocolAdapterSupplier<O, I, CO, CI> {

        private PathSupplier<ProtocolAdapter<O, I, CO, CI>[]> protoSupplier;
       
        /**
         * Creates a supplier with type instances.
         * 
         * @param protoSupplier the supplier
         * @param oClass the type for {@code O}
         * @param iClass the type for {@code I}
         * @param coClass the type for {@code CO}
         * @param ciClass the type for {@code CI}
         */
        protected ArrayPathAdapterSupplier(PathSupplier<ProtocolAdapter<O, I, CO, CI>[]> protoSupplier, 
            Class<O> oClass, Class<I> iClass, Class<CO> coClass, Class<CI> ciClass) {
            super(oClass, iClass, coClass, ciClass);
            this.protoSupplier = protoSupplier;
        }

        // checkstyle: stop parameter number check

        @Override
        @SuppressWarnings("unchecked")
        public <OT, IT, COT, CIT> ProtocolAdapter<OT, IT, COT, CIT>[] get(MetricsProvider metrics, File log, 
            Supplier<String> inPathSupplier, Supplier<String> outPathSupplier, Class<OT> oClass, Class<IT> iClass, 
            Class<COT> coClass, Class<CIT> ciClass) {
            ProtocolAdapter<OT, IT, COT, CIT>[] result = null;
            if (oClass.isAssignableFrom(this.oClass) && iClass.isAssignableFrom(this.iClass) 
                && coClass.isAssignableFrom(this.coClass) && ciClass.isAssignableFrom(this.ciClass)) {
                result = (ProtocolAdapter<OT, IT, COT, CIT>[]) protoSupplier.get(metrics, log, 
                    inPathSupplier, outPathSupplier);
            }
            return result;
        }

        // checkstyle: resume parameter number check

    }
    
    /**
     * Type-safe serializer supplier.
     * 
     * @param <T> the data type
     * @author Holger Eichelberger, SSE
     */
    private static class SerializerSupplier<T> {
        
        private Supplier<ChanneledSerializer<T>> supplier;
        private Class<T> cls;
        
        /**
         * Creates an instance.
         * 
         * @param supplier the supplier
         * @param cls the data type of the serializer
         */
        public SerializerSupplier(Supplier<ChanneledSerializer<T>> supplier, Class<T> cls) {
            this.supplier = supplier;
            this.cls = cls;
        }
        
        /**
         * Returns an instance if apparently type-compatible.
         * 
         * @param <TT> the desired data type
         * @param cls the desired data type class
         * @return the instance or <b>null</b> if it is not possible to create/cast an instance
         */
        @SuppressWarnings("unchecked")
        public <TT> ChanneledSerializer<TT> get(Class<TT> cls) {
            ChanneledSerializer<TT> result = null;
            if (cls.isAssignableFrom(this.cls)) {
                result = (ChanneledSerializer<TT>) supplier.get();
            }
            return result;
        }
        
    }

    /**
     * Default (type-safe) creator implementation.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DefaultConnectorFactoryCreator implements ConnectorFactoryCreator {

        private String pluginId;
        private Supplier<ConnectorParameter> connSupplier;
        private ProtocolAdapterSupplier<?, ?, ?, ?> protoSupplier;
        private Map<Class<?>, SerializerSupplier<?>> inputSerializerSupplier = new HashMap<>();
        private Map<Class<?>, SerializerSupplier<?>> outputSerializerSupplier = new HashMap<>();

        /**
         * Creates a factory creator instance for a given {@code pluginId}. Requires calls to the supplier-methods
         * after instance creation.
         * 
         * @param pluginId the plugin id
         */
        public DefaultConnectorFactoryCreator(String pluginId) {
            this.pluginId = pluginId;
        }
        
        /**
         * Adds a connector parameter supplier.
         * 
         * @param connSupplier the supplier to be called, may be <b>null</b> but then 
         *     {@link #createConnectorParameter()} will return <b>null</b>
         * @return <b>this</b> for chaining
         */
        public DefaultConnectorFactoryCreator supply(Supplier<ConnectorParameter> connSupplier) {
            this.connSupplier = connSupplier;
            return this;
        }
        
        /**
         * Adds a protocol adapter supplier.
         * 
         * @param <O> the output type from the underlying machine/platform
         * @param <I> the input type to the underlying machine/platform
         * @param <CO> the output type of the connector
         * @param <CI> the input type of the connector
         * @param protoSupplier the supplier to be called, may be <b>null</b>, will return <b>null</b>
         * @param oClass the type for {@code O}
         * @param iClass the type for {@code I}
         * @param coClass the type for {@code CO}
         * @param ciClass the type for {@code CI}
         * @return <b>this</b> for chaining
         */
        public <O, I, CO, CI> DefaultConnectorFactoryCreator supply(PathSupplier<
            ProtocolAdapter<O, I, CO, CI>> protoSupplier, Class<O> oClass, Class<I> iClass, Class<CO> coClass, 
            Class<CI> ciClass) {
            this.protoSupplier = new SinglePathAdapterSupplier<O, I, CO, CI>(protoSupplier, 
                oClass, iClass, coClass, ciClass);
            return this;
        }

        /**
         * Adds a protocol adapter supplier.
         * 
         * @param <O> the output type from the underlying machine/platform
         * @param <I> the input type to the underlying machine/platform
         * @param <CO> the output type of the connector
         * @param <CI> the input type of the connector
         * @param protoSupplier the supplier to be called, may be <b>null</b>, will return <b>null</b>
         * @param oClass the type for {@code O}
         * @param iClass the type for {@code I}
         * @param coClass the type for {@code CO}
         * @param ciClass the type for {@code CI}
         * @return <b>this</b> for chaining
         */
        public <O, I, CO, CI> DefaultConnectorFactoryCreator supplyMulti(
            PathSupplier<ProtocolAdapter<O, I, CO, CI>[]> protoSupplier, Class<O> oClass, Class<I> iClass, 
            Class<CO> coClass, Class<CI> ciClass) {
            this.protoSupplier = new ArrayPathAdapterSupplier<O, I, CO, CI>(protoSupplier, 
                oClass, iClass, coClass, ciClass);
            return this;
        }                

        /**
         * Adds an input serializer supplier.
         * 
         * @param <T> the data type
         * @param supplier the supplier
         * @param cls the data type class
         * @return <b>this</b> for chaining
         */
        public <T> DefaultConnectorFactoryCreator supplyForInput(Supplier<ChanneledSerializer<T>> supplier, 
            Class<T> cls) {
            if (null != cls) {
                this.inputSerializerSupplier.put(cls, new SerializerSupplier<T>(supplier, cls));
            }
            return this;
        }

        /**
         * Adds an output serializer supplier.
         * 
         * @param <T> the data type
         * @param supplier the supplier
         * @param cls the data type class
         * @return <b>this</b> for chaining
         */
        public <T> DefaultConnectorFactoryCreator supplyForOutput(Supplier<ChanneledSerializer<T>> supplier, 
            Class<T> cls) {
            if (null != cls) {
                this.outputSerializerSupplier.put(cls, new SerializerSupplier<T>(supplier, cls));
            }
            return this;
        }

        @Override
        public String getPluginId() {
            return pluginId;
        }
        
        @Override
        public ConnectorParameter createConnectorParameter() {
            return null != connSupplier ? connSupplier.get() : null;
        }

        // checkstyle: stop parameter number check

        @Override
        public <O, I, CO, CI> ProtocolAdapter<O, I, CO, CI>[] createProtocolAdapter(MetricsProvider metrics, File log, 
            Supplier<String> inPathSupplier, Supplier<String> outPathSupplier, Class<O> oClass, Class<I> iClass, 
            Class<CO> coClass, Class<CI> ciClass) {
            return null != protoSupplier ? protoSupplier.get(metrics, log, inPathSupplier, outPathSupplier, oClass, 
                iClass, coClass, ciClass) : null;
        }

        // checkstyle: resume parameter number check

        @Override
        public <T> ChanneledSerializer<T> createChanneledInputSerializer(Class<T> cls) {
            ChanneledSerializer<T> result = null;
            if (null != cls) {
                SerializerSupplier<?> s = this.inputSerializerSupplier.get(cls);
                return null != s ? s.get(cls) : null;
            }
            return result;
        }

        @Override
        public <T> ChanneledSerializer<T> createChanneledOutputSerializer(Class<T> cls) {
            ChanneledSerializer<T> result = null;
            if (null != cls) {
                SerializerSupplier<?> s = this.outputSerializerSupplier.get(cls);
                return null != s ? s.get(cls) : null;
            }
            return result;
        }

    }

    @Override
    public ConnectorFactoryCreator getCreator(String serviceId) {
        return creators.get(serviceId);
    }

}
