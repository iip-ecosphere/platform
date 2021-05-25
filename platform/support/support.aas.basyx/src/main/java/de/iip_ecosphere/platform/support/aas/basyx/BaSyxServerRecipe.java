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

package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.components.IComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;

/**
 * Implements the server recipe for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxServerRecipe implements ServerRecipe {

    // currently this does not allow to run registry and server on the same port...
    
    @Override
    public AasServer createAasServer(Endpoint serverEndpoint, PersistenceType persistence, Endpoint registryEndpoint, 
        String...options) {
        return new BaSyxRegistryDeploymentAasServer(new DeploymentSpec(serverEndpoint), registryEndpoint.toUri(), 
            translateForServer(persistence), options);
    }

    @Override
    public Server createRegistryServer(Endpoint endpoint, PersistenceType persistence, String... options) {
        BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(endpoint.getPort(), 
            endpoint.getEndpoint());
        RegistryBackend backend = Tools.getOption(options, translateForRegistry(persistence), RegistryBackend.class);
        BaSyxRegistryConfiguration registryConfig = new BaSyxRegistryConfiguration(backend);
        final IComponent component = new RegistryComponent(contextConfig, registryConfig);
        return new Server() {

            @Override
            public Server start() {
                component.startComponent();
                return this;
            }

            @Override
            public void stop(boolean dispose) {
                component.stopComponent();
                if (dispose) { // if not disposable, schedule for deletion at JVM end
                    Tools.disposeTomcatWorkingDir(null, endpoint.getPort());
                }
            }

        };
    }

    /**
     * Translates the given persistence type to a BaSyx registry backend. May be overridden by refining recipes.
     * 
     * @param type the persistence type
     * @return the BaSyx registry backend
     * @throws UnsupportedOperationException if {@code type} is not known/supported
     */
    protected RegistryBackend translateForRegistry(PersistenceType type) {
        RegistryBackend result;
        if (LocalPersistenceType.INMEMORY == type) {
            result = RegistryBackend.INMEMORY;
        } else {
            throw new UnsupportedOperationException("Persistence type '" + type 
                + "' is not supported as registry backend" );
        }
        return result;
    }

    /**
     * Translates the given persistence type to a BaSyx server backend. May be overridden by refining recipes.
     * 
     * @param type the persistence type
     * @return the BaSyx server backend
     * @throws UnsupportedOperationException if {@code type} is not known/supported
     */
    protected AASServerBackend translateForServer(PersistenceType type) {
        AASServerBackend result;
        if (LocalPersistenceType.INMEMORY == type) {
            result = AASServerBackend.INMEMORY;
        } else {
            throw new UnsupportedOperationException("Persistence type '" + type 
                + "' is not supported as server backend" );
        }
        return result;
    }

}
