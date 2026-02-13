/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2.server;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.basyx2.server.BaSyxAbstractAasServer.ServerType;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Implements the server recipe for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxServerRecipe implements ServerRecipe {
    
    @Override
    public PersistenceType toPersistenceType(String type) {
        PersistenceType result = LocalPersistenceType.INMEMORY;
        try {
            result = LocalPersistenceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            LoggerFactory.getLogger(getClass()).warn("Persistence type '" + type + "' is unknown. Using " 
                + LocalPersistenceType.INMEMORY + " as fallback.");
        }
        return result;
    }
            
    @Override
    public AasServer createAasServer(SetupSpec spec, PersistenceType persistence, String... options) {
        return new BaSyxLocalServer(spec, ServerType.REPOSITORY, persistence, options);
    }

    @Override
    public Server createRegistryServer(SetupSpec spec, PersistenceType persistence, String... options) {
        return new BaSyxLocalServer(spec, ServerType.REGISTRY, persistence, options);
    }
    
    @Override
    public ServerRecipe setAccessControlAllowOrigin(String accessControlAllowOrigin) {
        return this;
    }

}
