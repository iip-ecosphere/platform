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

import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.submodelRegistry.SubmodelRegistrySpringApp;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.submodelRegistry.SubmodelRegistrySpringAppInitializer;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.submodelRepository.SubmodelRepositorySpringApp;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.aasRegistry.AasRegistrySpringApp;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.aasRegistry.AasRegistrySpringAppInitializer;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.aasRepository.AasRepositorySpringApp;

/**
 * A local AAS server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxLocalServer extends BaSyxAbstractAasServer {
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param spec the setup specification
     * @param serverType the server type
     * @param persistencyType the persistence type
     * @param options for server creation
     */
    public BaSyxLocalServer(SetupSpec spec, ServerType serverType, PersistenceType persistencyType, String... options) {
        super(spec, serverType, options); // TODO persistence type
    }
    
    @Override
    protected Class<?> getAasRepositoryAppClass() {
        return AasRepositorySpringApp.class;
    }    

    @Override
    protected Class<?> getAasRegistryAppClass() {
        return AasRegistrySpringApp.class;
    }
    
    @Override
    protected ApplicationContextInitializer<ConfigurableApplicationContext> getAasRegistryAppInitializer() {
        return new AasRegistrySpringAppInitializer();
    }

    @Override
    protected Class<?> getSmRepositoryAppClass() {
        return SubmodelRepositorySpringApp.class;
    }

    @Override
    protected Class<?> getSmRegistryAppClass() {
        return SubmodelRegistrySpringApp.class;
    }
    
    @Override
    protected ApplicationContextInitializer<ConfigurableApplicationContext> getSmRegistryAppInitializer() {
        return new SubmodelRegistrySpringAppInitializer();
    }       

}