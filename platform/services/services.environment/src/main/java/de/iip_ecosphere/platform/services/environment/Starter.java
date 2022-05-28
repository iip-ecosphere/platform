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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.transport.Transport;

import static de.iip_ecosphere.platform.support.iip_aas.config.CmdLine.*;

/**
 * Service environment starter reading command server information from the command line.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    public static final String PARAM_IIP_PROTOCOL = "iip.protocol";
    public static final String PARAM_IIP_PORT = "iip.port";
    public static final String PARAM_IIP_TEST_SERVICE_AUTOSTART = "iip.test.service.autostart";
    
    private static ProtocolServerBuilder builder;
    private static Server server;
    private static Map<String, Integer> servicePorts = new HashMap<>();
    private static boolean serviceAutostart = false; // shall be off, done by platform, only for testing
    private static EnvironmentSetup setup;

    /**
     * Returns the network manager key used by this descriptor to allocate dynamic network ports for service commands.
     * 
     * @param serviceId the service id
     * @return the key
     */
    public static String getServiceCommandNetworkMgrKey(String serviceId) {
        return "admin_" + serviceId;
    }

    /**
     * Returns the network manager key used by this descriptor to allocate dynamic network ports for a non-Java 
     * realization process.
     * 
     * @param serviceId the service id
     * @return the key
     */
    public static String getServiceProcessNetworkMgrKey(String serviceId) {
        return getServiceCommandNetworkMgrKey(serviceId) + "_process";
    }
    
    /**
     * Composes a command line argument for the starter.
     * 
     * @param argName the argument name
     * @param value the value
     * @return the composed command line argument
     */
    public static String composeArgument(String argName, Object value) {
        return PARAM_PREFIX + argName + PARAM_VALUE_SEP + value.toString();
    }
    
    /**
     * Returns the argument name carrying the delegation port of {@code serviceId}. Arguments of this kind will
     * be collected for {@link #getServicePort(String)}
     * 
     * @param serviceId the service id (will be normalized to command line requirements)
     * @return the argument name
     */
    public static String getServicePortName(String serviceId) {
        return PARAM_IIP_PORT + PARAM_ARG_NAME_SEP + normalizeServiceId(serviceId);
    }

    /**
     * Returns a service port obtained in {@link #parse(String...)}.
     * 
     * @param serviceId the service id (will be normalized to command line requirements)
     * @return the port number, negative if invalid or unknown
     */
    public static int getServicePort(String serviceId) {
        Integer port = servicePorts.get(normalizeServiceId(serviceId));
        return null == port ? -1 : port;
    }
    
    /**
     * Returns the normalized service id.
     * 
     * @param serviceId the service id
     * @return the /normalized) service id
     */
    public static String normalizeServiceId(String serviceId) {
        return serviceId.replaceAll(" ", "");
    }
    
    /**
     * Parses command line arguments. Collects information for {@link #getServicePort(String)}.
     * 
     * @param args the arguments
     */
    public static void parse(String... args) {
        AasFactory factory = AasFactory.getInstance();
        int port = getIntArg(args, PARAM_IIP_PORT, -1);
        if (port < 0) {
            port = NetUtils.getEphemeralPort();
        }
        serviceAutostart = getBooleanArg(args, PARAM_IIP_TEST_SERVICE_AUTOSTART, false);
        String protocol = getArg(args, PARAM_IIP_PROTOCOL, AasFactory.DEFAULT_PROTOCOL);
        boolean found = false;
        for (String p : factory.getProtocols()) {
            if (p.equals(protocol)) {
                found = false;
            }
        }
        if (!found) {
            protocol = AasFactory.DEFAULT_PROTOCOL;
        }
        for (String a: args) {
            if (a.startsWith(PARAM_PREFIX + PARAM_IIP_PORT + PARAM_ARG_NAME_SEP)) {
                int valPos = a.indexOf(PARAM_VALUE_SEP);
                if (valPos > 0) {
                    String prefix = a.substring(0, valPos);
                    int idPos = prefix.lastIndexOf(PARAM_ARG_NAME_SEP);
                    try {
                        String serviceId = prefix.substring(idPos + 1);
                        int p = Integer.parseInt(a.substring(valPos + 1));
                        servicePorts.put(serviceId, p);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        getLogger().info("Configuring service command server for protocol '" + protocol 
            + "' (empty means default) and port " + port);
        builder = factory.createProtocolServerBuilder(protocol, port);
    }
    
    /**
     * Starts the server instance(s).
     */
    public static void start() {
        if (null != builder) {
            getLogger().info("Starting service command server");
            server = builder.build();
            server.start();
        } else {
            getLogger().error("Cannot start service command server as no builder is set.");
        }
    }
    
    /**
     * Returns the logger instance.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(Starter.class);
    }

    /**
     * Returns the protocol builder for mapping services.
     * 
     * @return the protocol builder, <b>null</b> if {@link #parse(String[])} was not called before
     */
    public static ProtocolServerBuilder getProtocolBuilder() {
        return builder;
    }

    /**
     * Returns the service mapper linked to {@link #getProtocolBuilder()}.
     * 
     * @return the service mapper, <b>null</b> if {@link #parse(String[])} was not called before
     */
    public static ServiceMapper getServiceMapper() {
        return new ServiceMapper(builder);
    }
    
    /**
     * Maps a service through a given mapper and metrics client. No mapping will take place if either {@code service},
     * {@code mapper} or {@link #getProtocolBuilder()} is <b>null</b>. The specific mapping for the metrics will only
     * take place if {@code metricsClient} is not <b>null</b>.
     * 
     * @param mapper the service mapper instance (may be <b>null</b>, no mapping will happen then)
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * @param enableAutostart whether service autostart shall be performed if {@code}, e.g., not for family members
     */
    public static void mapService(ServiceMapper mapper, Service service, boolean enableAutostart) {
        if (null != service) {
            if (null != mapper && null != Starter.getProtocolBuilder()) {
                mapper.mapService(service);
            }
            if (serviceAutostart && enableAutostart) {
                try {
                    getLogger().info("Service autostart: '{}' '{}'", service.getId(), service.getClass().getName());
                    service.setState(ServiceState.STARTING);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            System.out.println("Service autostop: " + service.getId());
                            service.setState(ServiceState.STOPPING);
                        } catch (ExecutionException e) {
                            getLogger().error("Service autostop '{}': {}", service.getId(), e.getMessage());
                        }
                    }));
                } catch (ExecutionException e) {
                    getLogger().error("Service autostart '{}': {}", service.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * @param enableAutostart whether service autostart shall be performed if {@code}, e.g., not for family members
     * 
     * @see #getServiceMapper()
     * @see #mapService(ServiceMapper, Service)
     */
    public static void mapService(Service service, boolean enableAutostart) {
        mapService(getServiceMapper(), service, enableAutostart);
    }

    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * By default, do autostart.
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * 
     * @see #getServiceMapper()
     * @see #mapService(ServiceMapper, Service)
     */
    public static void mapService(Service service) {
        mapService(service, true);
    }
    
    /**
     * Terminates running server instances.
     */
    public static void shutdown() {
        if (null != server) {
            server.stop(false); 
        }
    }
    
    /**
     * Returns the environment setup.
     * 
     * @return the setup
     */
    public static EnvironmentSetup getSetup() {
        if (null == setup) {
            try {
                setup = EnvironmentSetup.readFromYaml(EnvironmentSetup.class, 
                    ResourceLoader.getResourceAsStream(Starter.class, "application.yml"));
                Transport.setTransportSetup(() -> setup.getTransport());
            } catch (IOException e) {
                setup = new EnvironmentSetup();
                LoggerFactory.getLogger(Starter.class).warn("Cannot read application.yml. Aas/Transport setup invalid");
            }
        }
        return setup;
    }

    /**
     * Simple default start main program without mapping any services before startup. This can be done on-demand
     * through {@link #getProtocolBuilder()} and {@link #getServiceMapper()}.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Starter.parse(args);
        getSetup(); // ensure instance
        Starter.start();
    }

}
