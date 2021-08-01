/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.platform.cli;

import java.io.IOException;

import de.iip_ecosphere.platform.ecsRuntime.ResourcesClient;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * A factory for the resources client. [testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ResourcesClientFactory {

    /**
     * The default factory.
     */
    public static final ResourcesClientFactory DEFAULT = new ResourcesClientFactory() {
        
        @Override
        public ResourcesClient create() throws IOException {
            return new ResourcesClient() {

                @Override
                public Submodel getResources() throws IOException {
                    Aas aas = AasPartRegistry.retrieveIipAas();
                    return aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
                }
            };
        }
        
    };
    
    /**
     * Creates a resources client.
     * 
     * @return the client instance
     * @throws IOException if the client cannot be created
     */
    public ResourcesClient create() throws IOException;
    
}
