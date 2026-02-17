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

package de.iip_ecosphere.platform.support.aas.basyx2;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.basyx2.common.PluginId;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends AbstractBaSyxAasFactory {

    public static final String PLUGIN_ID = PluginId.PLUGIN_ID;
    
    /**
     * Factory descriptor for Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor extends AbstractDescriptor {

        @Override
        public AasFactory createInstance() {
            return new BaSyxAasFactory();
        }

        @Override
        public String getId() {
            return PLUGIN_ID;
        }
        
    }
    
    /**
     * Creates an instance.
     */
    public BaSyxAasFactory() {
        registerPersistenceRecipe(new AasxPersistenceRecipe());
    }
    
    @Override
    public String getBasePluginId() {
        return PLUGIN_ID;
    }
    
    @Override
    public boolean supportsPropertyFunctions() {
        return false;
    }    

    @Override
    public String getName() {
        return "AAS/BaSyx v2 M5";
    }

    @Override
    public String getMetaModelVersion() {
        return "v3";
    }
    
}
