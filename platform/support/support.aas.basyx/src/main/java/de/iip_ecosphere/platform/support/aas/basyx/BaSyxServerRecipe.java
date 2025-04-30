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
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Implements the server recipe for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxServerRecipe implements ServerRecipe {

    // currently this does not allow to run registry and server on the same port...
    
    private String accessControlAllowOrigin;
    
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
        DeploymentSpec dspec = applyAccessControlAllowOrigin(
            new DeploymentSpec(spec.getAasRepositoryEndpoint(), spec.getAasRepositoryKeyStore()));
        return new BaSyxRegistryDeploymentAasServer(dspec, spec.getAasRegistryEndpoint().toUri(), 
            translateForServer(persistence), options);
    }
    
    /**
     * Applies {@link #accessControlAllowOrigin} to {@code spec}.
     * 
     * @param spec the specification to modify
     * @return {@code spec}
     */
    private DeploymentSpec applyAccessControlAllowOrigin(DeploymentSpec spec) {
        if (null != accessControlAllowOrigin) {
            spec.setAccessControlAllowOrigin(accessControlAllowOrigin);
        }
        return spec;
    }

    @Override
    public Server createRegistryServer(SetupSpec spec, PersistenceType persistence, String... options) {
        // registries are not encrypted in BaSyx
        DeploymentSpec dspec = applyAccessControlAllowOrigin(new DeploymentSpec(spec.getAasRegistryEndpoint(), 
            (KeyStoreDescriptor) null));
        RegistryBackend backend = Tools.getOption(options, translateForRegistry(persistence), RegistryBackend.class);
        BaSyxRegistryConfiguration registryConfig = new BaSyxRegistryConfiguration(backend);
        final IComponent component = new RegistryComponent(dspec.getContextConfiguration(), registryConfig);
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
                    Tools.disposeTomcatWorkingDir(null, spec.getAasRegistryEndpoint().getPort());
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
    
    @Override
    public ServerRecipe setAccessControlAllowOrigin(String accessControlAllowOrigin) {
        this.accessControlAllowOrigin = accessControlAllowOrigin;
        return this;
    }

}
