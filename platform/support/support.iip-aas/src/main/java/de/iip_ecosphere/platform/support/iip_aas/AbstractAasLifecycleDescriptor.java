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
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasMode;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * Implements the generic lifecycle descriptor for the service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractAasLifecycleDescriptor implements LifecycleDescriptor {

    /**
     * Explicitly determine the AAS implementation server port. If not given, use an ephemeral one.
     */
    public static final String PARAM_IIP_PORT = "iip.port";
    
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
        int port = CmdLine.getIntArg(args, PARAM_IIP_PORT, -1);
        if (port > 0) {
            setupSupplier.get().getImplementation().setPort(port);
            LoggerFactory.getLogger(getClass()).info("Using port " + port + " for the AAS implementation server.");
        }
        if (AasFactory.isFullInstance()) {
            AasSetup setup = setupSupplier.get();
            AasPartRegistry.setAasSetup(setup);
            AasPartRegistry.AasBuildResult res = AasPartRegistry.build(true); // true due to incremental deployment
            implServer = res.getProtocolServer();
            
            if (AasMode.REGISTER == setup.getMode()) {
                try {
                    aasServer = AasPartRegistry.register(res.getAas(), setup.getRegistryEndpoint()); 
                    aasServer.start();
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Cannot register AAS " + name + " with " 
                        + setup.getRegistryEndpoint().toUri() + ":" + e.getMessage());
                }
            } else {
                try {
                    AasPartRegistry.remoteDeploy(res.getAas());
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Cannot deploy AAS " + name + ": " + e.getMessage());
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
