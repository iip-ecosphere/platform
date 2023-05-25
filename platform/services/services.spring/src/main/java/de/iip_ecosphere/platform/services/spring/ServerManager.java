/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring;

import static de.iip_ecosphere.platform.services.spring.SpringInstances.getConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.ServerWrapper;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.spring.Starter;
import de.iip_ecosphere.platform.services.spring.descriptor.Server;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.NetworkManager;

/**
 * Manages server instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerManager {

    private static final String PROP_DISABLE_SERVER = "iip.services.disableServer";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);
    private Map<SpringCloudServiceDescriptor, de.iip_ecosphere.platform.support.Server> 
        runningServers = new HashMap<>();
    private Supplier<NetworkManager> networkManagerSupplier;

    /**
     * Creates a server manager instance.
     * 
     * @param networkManagerSupplier the network manager supplier
     */
    public ServerManager(Supplier<NetworkManager> networkManagerSupplier) {
        this.networkManagerSupplier = networkManagerSupplier;
    }
    
    /**
     * Starting server instances.
     * 
     * @param options optional map of optional options to reconfigure the host identifications of the servers 
     *     ({@link ServiceManager#OPTION_SERVERS}), may be <b>null</b>
     * @param artifacts the artifacts to be considered for server definitions
     * @see #getThisDeviceHostIds()
     */
    public void startServers(Map<String, String> options, Collection<SpringCloudArtifactDescriptor> artifacts) {
        String myHost = NetUtils.getOwnHostname();
        Map<String, SpringCloudServiceDescriptor> servers = getServers(options, artifacts);
        
        if (servers.size() > 0) { // prevent warnings if there are no server specs to process
            NetworkManager netClient = networkManagerSupplier.get();
            if (null != netClient) {
                Map<SpringCloudArtifactDescriptor, ClassLoader> loaders = new HashMap<>();
                for (SpringCloudServiceDescriptor s: servers.values()) {
                    String id = s.getId();
                    if (null == netClient.getPort(id)) {
                        try {
                            // current assumption: process comes with valid path, temporary path -> cmdLine
                            Starter.extractProcessArtifacts(id, s.getServer(), s.getArtifact().getJar(), 
                                SpringInstances.getConfig().getDownloadDir());
                            Server ser = s.getServer();
                            ClassLoader loader = loaders.get(s.getArtifact());
                            if (null == loader) {
                                loader = DescriptorUtils.determineArtifactClassLoader(s);
                                loaders.put(s.getArtifact(), loader);
                            }
                            Class<?> cls = Class.forName(ser.getCls(), true, loader); 
                            Object o;
                            try {
                                List<String> cmdLine = s.collectCmdArguments(getConfig(), ser.getPort(), "");
                                o = cls.getConstructor(String[].class).newInstance(
                                    (Object) cmdLine.toArray(new String[0]));
                            } catch (NoSuchMethodException e) {
                                o = cls.getConstructor().newInstance();
                            }
                            if (o instanceof de.iip_ecosphere.platform.support.Server) {
                                LOGGER.info("Starting server {} ", id);
                                ServerWrapper sv = new ServerWrapper((de.iip_ecosphere.platform.support.Server) o);
                                sv.start();
                                DescriptorUtils.setStateSafe(s, ServiceState.STARTING);
                                ServerAddress adr = new ServerAddress(Schema.IGNORE, myHost, ser.getPort());
                                adr = netClient.reservePort(id, adr);
                                runningServers.put(s, sv);
                                DescriptorUtils.setStateSafe(s, ServiceState.RUNNING);
                                LOGGER.info("Started server {} ", id);
                            } else {
                                LOGGER.error("Starting server {}. Specified class does not implement support.Server. "
                                    + "Cannot start.", id);
                            }
                        } catch (ClassNotFoundException  e) {
                            LOGGER.error("Starting server {}: Cannot find class {}", id, e.getMessage());
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException 
                            | NoSuchMethodException e) {
                            LOGGER.error("Starting server {}, cannot invoke constructor: {}", id, e.getMessage());
                        } catch (IOException e) {
                            LOGGER.error("Starting server {}, unpacking artfiact: {}", id, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Stopping server instances.
     * 
     * @param artifacts the artifacts to be considered for server definitions
     */
    public void stopServers(Collection<SpringCloudArtifactDescriptor> artifacts) {
        List<SpringCloudServiceDescriptor> servers = new ArrayList<>();
        for (SpringCloudArtifactDescriptor desc : artifacts) {
            for (SpringCloudServiceDescriptor s: desc.getServers()) {
                if (runningServers.containsKey(s)) {
                    servers.add(s);
                }
            }
        }
        if (servers.size() > 0) { // prevent warnings if there are no server specs to process
            NetworkManager netClient = networkManagerSupplier.get();
            if (null != netClient) {
                for (SpringCloudServiceDescriptor s: servers) {
                    String id = s.getId();
                    if (netClient.getRegisteredInstances(id) == 0) {
                        DescriptorUtils.setStateSafe(s, ServiceState.STOPPING);
                        de.iip_ecosphere.platform.support.Server sv = runningServers.remove(s);
                        sv.stop(true);
                        netClient.releasePort(id);
                        DescriptorUtils.setStateSafe(s, ServiceState.STOPPED);
                        LOGGER.info("Stopped server {} ", id);
                    }
                }
            }
        }
    }

    /**
     * String returns the host names/ids to be considered equal for this (the executing) device.
     *  
     * @return the ids
     */
    private static Set<String> getThisDeviceHostIds() {
        Set<String> thisDevice = new HashSet<>();
        thisDevice.add(ServerAddress.LOCALHOST);
        thisDevice.add("127.0.0.1");
        thisDevice.add(NetUtils.getOwnHostname()); // may require netmask
        thisDevice.add(NetUtils.getOwnIP(NetUtils.NO_MASK)); // may require netmask
        thisDevice.add(Id.getDeviceId());
        thisDevice.add(Id.getDeviceIdAas());
        return thisDevice;
    }
    
    /**
     * Returns the servers to be started.
     * 
     * @param options optional map of optional options to reconfigure the host identifications of the servers 
     *     ({@link ServiceManager#OPTION_SERVERS}), may be <b>null</b>
     * @param artifacts the artifacts to be considered for server definitions
     * @return the id-descriptor mapping of servers to be started, may be empty
     */
    private Map<String, SpringCloudServiceDescriptor> getServers(Map<String, String> options, 
        Collection<SpringCloudArtifactDescriptor> artifacts) {
        Map<String, SpringCloudServiceDescriptor> servers = new HashMap<>();
        Map<String, String> hostMap = new HashMap<>();
        if (null != options) {
            String opt = options.get(ServiceManager.OPTION_SERVERS);
            if (null != opt) {
                Map<?, ?> optMap = JsonUtils.fromJson(opt, Map.class);
                for (Map.Entry<?, ?> ent : optMap.entrySet()) {
                    hostMap.put(ent.getKey().toString(), ent.getValue().toString());
                }
            }
        }
        Set<String> thisDevice = getThisDeviceHostIds();
        Set<String> knownServers = new HashSet<>();
        for (SpringCloudArtifactDescriptor desc : artifacts) {
            for (SpringCloudServiceDescriptor s: desc.getServers()) {
                String id = s.getId();
                knownServers.add(id);
                String host = hostMap.get(id);
                if (null == host) {
                    host = s.getServer().getHost();
                }
                if (thisDevice.contains(host)) {
                    servers.put(id, s);
                }
            }
        }
        String[] disable = System.getProperty(PROP_DISABLE_SERVER, "").split(",");
        for (String d : disable) {
            String id = d.trim();
            if (servers.containsKey(id)) {
                servers.remove(id);
                LOGGER.info("Ignoring disabled server {} from -D{}", id, PROP_DISABLE_SERVER);
            }
        }
        
        if (knownServers.size() > 0) {
            LOGGER.info("Preparing server start: Of known servers {} starting {} on this host ({})", knownServers, 
                servers, thisDevice);
        }
        return servers;
    }

}
