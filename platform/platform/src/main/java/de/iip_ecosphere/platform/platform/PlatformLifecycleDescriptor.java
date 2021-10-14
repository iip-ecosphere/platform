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

package de.iip_ecosphere.platform.platform;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.EcsLifecycleDescriptor;
import de.iip_ecosphere.platform.services.ServicesLifecycleDescriptor;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleExclude;
import de.iip_ecosphere.platform.support.PidLifecycleDescriptor;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;

/**
 * Defines the parts to be started directly by the platform code.
 * 
 * @author Holger Eichelberger, SSE
 */
@LifecycleExclude({ServicesLifecycleDescriptor.class, EcsLifecycleDescriptor.class})
public class PlatformLifecycleDescriptor implements LifecycleDescriptor, PidLifecycleDescriptor {

    private Server registryServer;
    private Server aasServer;
    
    @Override
    public void startup(String[] args) {
        PlatformConfiguration cfg = PlatformConfiguration.getInstance();
        AasSetup aasSetup = cfg.getAas();
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        PersistenceType pType = rcp.toPersistenceType(cfg.getAas().getPersistence().name());
        LoggerFactory.getLogger(getClass()).info("Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.getServerEndpoint();
        LoggerFactory.getLogger(getClass()).info("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(aasSetup.getServerEndpoint(), pType, regEndpoint);
        aasServer.start();
    }

    @Override
    public void shutdown() {
        if (null != aasServer) {
            aasServer.stop(false);
        }
        if (null != registryServer) {
            registryServer.stop(false);
        }
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }

    @Override
    public int priority() {
        return INIT_PRIORITY;
    }

    @Override
    public String getPidFileName() {
        return "iip-platform.pid";
    }

}
