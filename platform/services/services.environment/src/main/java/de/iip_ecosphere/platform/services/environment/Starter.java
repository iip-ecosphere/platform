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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;

/**
 * Service environment starter reading command server information from the command line.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    public static final String PARAM_IIP_PROTOCOL = "iip.protocol";
    public static final String PARAM_IIP_PORT = "iip.port";
    public static final String PARAM_PREFIX = "--";
    public static final String PARAM_VALUE_SEP = "=";
    
    private static ProtocolServerBuilder builder;
    private static Server server;
    
    /**
     * Parses command line arguments.
     * 
     * @param args the arguments
     */
    public static void parse(String... args) {
        AasFactory factory = AasFactory.getInstance();
        int port = getIntArg(args, PARAM_IIP_PORT, -1);
        if (port < 0) {
            port = NetUtils.getEphemeralPort();
        }
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

        LoggerFactory.getLogger(Starter.class).info("Configuring service command server for protocol '" + protocol 
            + "' (empty means default) and port " + port);
        builder = factory.createProtocolServerBuilder(protocol, port);
    }
    
    /**
     * Starts the server instance(s).
     */
    public static void start() {
        LoggerFactory.getLogger(Starter.class).info("Starting service command server");
        server = builder.build();
        server.start();
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
     * Terminates running server instances.
     */
    public static void shutdown() {
        if (null != server) {
            server.stop(false); 
        }
    }
    
    /**
     * Emulates reading a Spring-like parameter if the configuration is not yet in place.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static String getArg(String[] args, String argName, String dflt) {
        String result = dflt;
        String prefix = PARAM_PREFIX + argName + PARAM_VALUE_SEP;
        for (int a = 0; a < args.length; a++) {
            String arg = args[a];
            if (arg.startsWith(prefix)) {
                result = arg.substring(prefix.length());
                break;
            }
        }
        return result;
    }
    
    /**
     * Returns an int command line argument.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static int getIntArg(String[] args, String argName, int dflt) {
        int result;
        try {
            result = Integer.parseInt(getArg(args, argName, String.valueOf(dflt)));
        } catch (NumberFormatException e) {
            result = dflt;
        }
        return result;
    }

}
