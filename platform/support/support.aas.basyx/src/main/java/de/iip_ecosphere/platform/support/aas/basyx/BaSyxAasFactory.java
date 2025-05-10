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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.List;

import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.JwtBearerTokenAuthenticationConfiguration;

import de.iip_ecosphere.platform.support.aas.AasFactory;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends AbstractBaSyxAasFactory {

    static final boolean ENABLE_PROPERTY_LAMBDA = true;
    private static final String PLUGIN_ID = "aas.basyx-1.3";

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
            return DEFAULT_PLUGIN_ID;
        }
        
        @Override
        public List<String> getFurtherIds() {
            return List.of(PLUGIN_ID);
        }

    }
    
    /**
     * Creates an instance.
     */
    public BaSyxAasFactory() {
        registerProtocolCreator(PROTOCOL_VAB_HTTPS, new VabHttpsProtocolCreator());
        registerPersistenceRecipe(new AasxPersistenceRecipe());
        // use new method, prevent deprecated
        VersionAdjustment.registerSetPropertyKind(Property.class, (p, k) -> p.setKind(k));
        // use new method, prevent deprecated
        VersionAdjustment.registerOperationInvoke(Operation.class, (o, a) -> o.invokeSimple(a));
        VersionAdjustment.registerOperationInvoke(ConnectedOperation.class, (o, a) -> o.invokeSimple(a));
        // CORS available
        VersionAdjustment.registerSetBearerTokenAuthenticationConfiguration(BaSyxContext.class, (c, i, j, r) -> 
            c.setJwtBearerTokenAuthenticationConfiguration(
                JwtBearerTokenAuthenticationConfiguration.of(i, j, r)));
        // switch off data mapper
        VersionAdjustment.registerSetupBaSyxAASServerConfiguration(BaSyxAASServerConfiguration.class, c -> setup(c));
    }
    
    /**
     * Sets up a server configuration for lambda properties.
     * 
     * @param cfg the configuration
     */
    private static void setup(BaSyxAASServerConfiguration cfg) {
        if (ENABLE_PROPERTY_LAMBDA) { // enables user lambdas, disables data mapper
            cfg.disablePropertyDelegation();
        } else { // enables data mapper, disables user lambdas
            cfg.enablePropertyDelegation();
        }        
    }
    
    @Override
    public boolean supportsPropertyFunctions() {
        return ENABLE_PROPERTY_LAMBDA;
    }    

    @Override
    public String getName() {
        return "AAS/BaSyx v1.3.0 (2022/12/15)";
    }

}
