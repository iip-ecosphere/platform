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
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ChanneledConnectorOutputTypeAdapter.ChanneledSerializer;
import de.iip_ecosphere.platform.support.metrics.MetricsProvider;

/**
 * JSL descriptor for application-provided connector instance creation.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ConnectorFactoryDescriptor {
    
    /**
     * Creator instance for a specific service id. Creator functions must return non-null values.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ConnectorFactoryCreator {

        /**
         * Returns the associated plugin id.
         * 
         * @return the plugin id
         */
        public String getPluginId();

        /**
         * Creates the default connector parameter instance.
         * 
         * @return the connector parameter
         */
        public ConnectorParameter createConnectorParameter();
        
        // checkstyle: stop parameter number check

        /**
         * Creates the default connector protocol adapter.
         * 
         * @param <O> the output type from the underlying machine/platform
         * @param <I> the input type to the underlying machine/platform
         * @param <CO> the output type of the connector
         * @param <CI> the input type of the connector
         * @param metrics the metrics provider to use, <b>null</b> for no metric measurements
         * @param log the log file to use to record individual measurements in experiments, <b>null</b> for no logging.
         *     Only becomes active if {@code metrics} is not <b>null</b>
         * @param inPathSupplier function returning the actual input base path to use for data accesses, may be 
         *     <b>null</b>
         * @param outPathSupplier function returning the actual output base path to use for data accesses, may be 
         *     <b>null</b>
         * @param oClass the actual type of {@code O}
         * @param iClass the actual type of {@code I}
         * @param coClass the actual type of {@code CO}
         * @param ciClass the actual type of {@code CI}
         * @return the connector protocol adapter
         */
        public <O, I, CO, CI> ProtocolAdapter<O, I, CO, CI>[] createProtocolAdapter(MetricsProvider metrics, File log, 
            Supplier<String> inPathSupplier, Supplier<String> outPathSupplier, Class<O> oClass, Class<I> iClass, 
            Class<CO> coClass, Class<CI> ciClass);

        // checkstyle: resume parameter number check

        /**
         * Creates a channeled input serializer for the given {@code serviceId} and the given type {@code cls}.
         * 
         * @param <T> the data type
         * @param cls the data type
         * @return the serializer or <b>null</b> if unknown
         */
        public <T> ChanneledSerializer<T> createChanneledInputSerializer(Class<T> cls);

        /**
         * Creates a channeled output serializer for the given {@code serviceId} and the given type {@code cls}.
         * 
         * @param <T> the data type
         * @param cls the data type
         * @return the serializer or <b>null</b> if unknown
         */
        public <T> ChanneledSerializer<T> createChanneledOutputSerializer(Class<T> cls);

    }

    /**
     * Returns the creator for the given {@code serviceId}.
     * 
     * @param serviceId the service id
     * @return the creator, may be <b>null</b> for unknown
     */
    public ConnectorFactoryCreator getCreator(String serviceId);

}
