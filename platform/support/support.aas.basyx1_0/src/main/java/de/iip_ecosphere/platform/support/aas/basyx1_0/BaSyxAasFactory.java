/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx1_0;

import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractBaSyxAasFactory;
import de.iip_ecosphere.platform.support.aas.basyx.VersionAdjustment;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginDescriptor;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends AbstractBaSyxAasFactory {

    private static final String PLUGIN_ID = "aas.basyx-1.0";
    
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
        registerPersistenceRecipe(new AasxPersistenceRecipe());
        // use old, not yet deprecated method
        VersionAdjustment.registerSetPropertyKind(Property.class, (p, k) -> p.setModelingKind(k));
        // use old, not yet deprecated method
        VersionAdjustment.registerOperationInvoke(Operation.class, (o, a) -> o.invoke(a));
        VersionAdjustment.registerOperationInvoke(ConnectedOperation.class, (o, a) -> o.invoke(a));
        // no CORS
        VersionAdjustment.registerSetBearerTokenAuthenticationConfiguration(BaSyxContext.class, (c, i, j, r) -> { });
        // property lambdas always active
        VersionAdjustment.registerSetupBaSyxAASServerConfiguration(BaSyxAASServerConfiguration.class, c -> { });
    }

    @Override
    public String getName() {
        return "AAS/BaSyx v1.0.1 (2021/10/20)";
    }
    
    @Override
    public boolean supportsPropertyFunctions() {
        return true;
    }

}
