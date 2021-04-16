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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;

/**
 * Implements the generic lifecycle descriptor for the service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractAasLifecycleDescriptor implements LifecycleDescriptor {

    private String name;
    private Supplier<AasSetup> setupSupplier;
    private Server implServer;
    private Server aasServer;
    
    /**
     * Creates a descriptor instance.
     * 
     * @param name the name of the AAS to build for logging
     * @param setupSupplier the {@link AasSetup} supplier
     */
    protected AbstractAasLifecycleDescriptor(String name, Supplier<AasSetup> setupSupplier) {
        this.name = name;
        this.setupSupplier = setupSupplier;
    }
    
    @Override
    public void startup(String[] args) {
        if (AasFactory.isFullInstance()) {
            AasSetup setup = setupSupplier.get();
            AasPartRegistry.setAasSetup(setup);
            AasPartRegistry.AasBuildResult res = AasPartRegistry.build();
            
            // active AAS require two server instances and a deployment
            implServer = res.getProtocolServerBuilder().build();
            implServer.start();
            // TODO remote deployment, destination to be defined via JAML/AasPartRegistry
            if (ServerAddress.LOCALHOST.equals(setup.getServer().getHost())) {
                aasServer = AasPartRegistry.deploy(res.getAas()); 
                aasServer.start();
            } else {
                try {
                    AasPartRegistry.remoteDeploy(res.getAas());
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Cannot deploy " + name + "AAS: " + e.getMessage());
                }
            }
        } else {
            LoggerFactory.getLogger(getClass()).warn("No full AAS implementation registered. Cannot build up " 
                + name + " AAS. Please add an appropriate dependency.");
        }
    }

    @Override
    public void shutdown() {
        if (null != implServer) {
            implServer.stop(true);
        }
        if (null != aasServer) {
            implServer.stop(true);
        }
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }
    
    @Override
    public int priority() {
        return AAS_PRIORITY;
    }

}
