/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx1_5;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.Tools;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginDescriptor;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends de.iip_ecosphere.platform.support.aas.basyx.BaSyxAasFactory {

    private static final String PLUGIN_ID = "aas.basyx-1.5";
    
    /**
     * Factory descriptor for Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements AasFactoryDescriptor, PluginDescriptor {

        @Override
        public AasFactory createInstance() {
            return new BaSyxAasFactory();
        }

        @Override
        public String getId() {
            return PLUGIN_ID;
        }

        @Override
        public Plugin<?> createPlugin() {
            return new Plugin<AasFactory>(PLUGIN_ID, AasFactory.class, () -> createInstance());
        }
        
    }
    
    /**
     * Creates an instance.
     */
    public BaSyxAasFactory() {
        super();
        Tools.mapBaSyxType(Type.DOUBLE, ValueType.Decimal); // preliminary
        Tools.mapBaSyxType(Type.DATE_TIME, ValueType.Date); // preliminary
    }

    @Override
    public String getName() {
        return "AAS/BaSyx v1.5.1 (2024/05/02)";
    }
    
    @Override
    public boolean supportsPropertyFunctions() {
        return true;
    }

}
