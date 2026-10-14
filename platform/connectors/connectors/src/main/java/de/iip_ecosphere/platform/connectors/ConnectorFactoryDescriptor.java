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

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

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
        
        /**
         * Creates the default connector protocol adapter.
         * 
         * @param <O> the output type from the underlying machine/platform
         * @param <I> the input type to the underlying machine/platform
         * @param <CO> the output type of the connector
         * @param <CI> the input type of the connector
         * @param oClass the actual type of {@code O}
         * @param iClass the actual type of {@code I}
         * @param coClass the actual type of {@code CO}
         * @param ciClass the actual type of {@code CI}
         * @return the connector protocol adapter
         */
        public <O, I, CO, CI> ProtocolAdapter<O, I, CO, CI>[] createProtocolAdapter(Class<O> oClass, Class<I> iClass, 
            Class<CO> coClass, Class<CI> ciClass);

    }

    /**
     * Returns the creator for the given {@code serviceId}.
     * 
     * @param serviceId the service id
     * @return the creator, may be <b>null</b> for unknown
     */
    public ConnectorFactoryCreator getCreator(String serviceId);

}
