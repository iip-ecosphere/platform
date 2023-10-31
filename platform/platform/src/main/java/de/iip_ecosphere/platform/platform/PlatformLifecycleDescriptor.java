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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.EcsLifecycleDescriptor;
import de.iip_ecosphere.platform.services.ServicesLifecycleDescriptor;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleExclude;
import de.iip_ecosphere.platform.support.PidFile;
import de.iip_ecosphere.platform.support.PidLifecycleDescriptor;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.Transport;

/**
 * Defines the parts to be started directly by the platform code.
 * 
 * @author Holger Eichelberger, SSE
 */
@LifecycleExclude({ServicesLifecycleDescriptor.class, EcsLifecycleDescriptor.class})
public class PlatformLifecycleDescriptor implements LifecycleDescriptor, PidLifecycleDescriptor {

    public static final String PLATFORM_FILE_NAME = "iip-platform";
    public static final String PROP_AAS_REGISTRY = "aas.registry.uri";
    public static final String PROP_ASS_SERVER = "aas.server.uri";
    
    private Server registryServer;
    private Server aasServer;
    private Server gatewayServer;
    
    @Override
    public void startup(String[] args) {
        Properties props = new Properties();
        PlatformSetup setup = PlatformSetup.getInstance();
        Transport.setTransportSetup(() -> setup.getTransport());
        AasSetup aasSetup = setup.getAas();
        ServerRecipe rcp = AasPartRegistry.applyCorsOrigin(AasFactory.getInstance().createServerRecipe(), aasSetup);
        Endpoint regEndpoint = aasSetup.adaptEndpoint(aasSetup.getRegistryEndpoint());
        LoggerFactory.getLogger(getClass()).info("ServerHost {} {}", aasSetup.getServerHost(), regEndpoint.toUri());
        PersistenceType pType = rcp.toPersistenceType(setup.getAas().getPersistence().name());
        String fullRegUri = AasFactory.getInstance().getFullRegistryUri(regEndpoint);
        LoggerFactory.getLogger(getClass()).info("Starting {} AAS registry on {}", pType, fullRegUri);
        props.put(PROP_AAS_REGISTRY, fullRegUri);
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.adaptEndpoint(aasSetup.getServerEndpoint());
        LoggerFactory.getLogger(getClass()).info("ServerHost {} {}", aasSetup.getServerHost(), serverEndpoint.toUri());
        props.put(PROP_ASS_SERVER, serverEndpoint.toUri());
        LoggerFactory.getLogger(getClass()).info("Starting {} AAS server on {}", pType, serverEndpoint.toUri());
        aasServer = rcp.createAasServer(aasSetup.getServerEndpoint(), pType, regEndpoint);
        aasServer.start();
        
        gatewayServer = TransportConverterFactory.getInstance().createServer(aasSetup, setup.getTransport());
        gatewayServer.start();
        
        File propFile = getPropertiesFile();
        try (FileWriter fw = new FileWriter(propFile)) {
            props.store(fw, "");
            LoggerFactory.getLogger(getClass()).info("Wrote platform properties to {}", propFile);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).warn("Failed writing platform properties to {}: {}", 
                propFile, e.getMessage());
        }
    }
    
    /**
     * Returns the dynamic platform properties file.
     * 
     * @return the properties file.
     */
    public static File getPropertiesFile() {
        return new File(PidFile.getPidDirectory(), PLATFORM_FILE_NAME + ".properties");
    }

    @Override
    public void shutdown() {
        Server.stop(gatewayServer, false);
        Server.stop(aasServer, false);
        Server.stop(registryServer, false);
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
        return PLATFORM_FILE_NAME + ".pid";
    }

}
