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

import de.iip_ecosphere.platform.ecsRuntime.EcsAasClient;
import de.iip_ecosphere.platform.ecsRuntime.EcsClient;

/**
 * A factory for ECS client instances. [testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public interface EcsClientFactory {
    
    /**
     * The default factory.
     */
    public static final EcsClientFactory DEFAULT = new EcsClientFactory() {
        
        @Override
        public EcsClient create(String resourceId) throws IOException {
            return new EcsAasClient(resourceId);
        }
    };
    
    /**
     * Creates an ECS AAS client.
     * 
     * @param resourceId the id of the resource to create the client for 
     * @return the client instance
     * @throws IOException if the client cannot be created
     */
    public EcsClient create(String resourceId) throws IOException;

}
