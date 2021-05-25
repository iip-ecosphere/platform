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

package de.iip_ecosphere.platform.support.aas.basyx.server;

import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;

import de.iip_ecosphere.platform.support.aas.AasServerRecipeDescriptor;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxServerRecipe;

/**
 * Full BaSyx server recipe, hooked in via JSL.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxFullServerRecipe extends BaSyxServerRecipe {

    /**
     * Descriptor to hook the recipe as specialized recipe into the BaSxy AAS factory.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxFullServerReceipeDescriptor implements AasServerRecipeDescriptor {

        @Override
        public ServerRecipe createInstance() {
            return new BaSyxFullServerRecipe();
        }
        
    }

    /**
     * Enables BaSyx persistence to Mongo, but only on Server (vs. Edge) side.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ServerPersistenceType implements PersistenceType {
        MONGO;
    }

    @Override
    protected RegistryBackend translateForRegistry(PersistenceType type) {
        RegistryBackend result;
        if (ServerPersistenceType.MONGO == type) {
            result = RegistryBackend.MONGODB;
        } else {
            result = super.translateForRegistry(type);
        }
        return result;
    }

    @Override
    protected AASServerBackend translateForServer(PersistenceType type) {
        AASServerBackend result;
        if (ServerPersistenceType.MONGO == type) {
            result = AASServerBackend.MONGODB;
        } else {
            result = super.translateForServer(type);
        }
        return result;
    }

}
