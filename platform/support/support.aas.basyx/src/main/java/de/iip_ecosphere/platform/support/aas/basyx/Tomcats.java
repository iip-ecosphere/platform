/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.startup.Tomcat;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.slf4j.LoggerFactory;

/**
 * As long as BaSyx is using the same service name for all started tomcat servers, we try to track them 
 * here and prevent multiple shutdowns leading to MBean state warnings.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Tomcats {

    private static final Map<Class<?>, String[]> ATTRIBUTES = new HashMap<>();
    private static final Map<String, State> TOMCATS = new HashMap<>();

    /**
     * Defines the tomcat states.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum State {
        RUNNING,
        STOPPING
    }
    
    /**
     * Sets up the attribute access paths for the respective tomcat instances.
     */
    static {
        ATTRIBUTES.put(AASHTTPServer.class, new String[] {"tomcat"});
        ATTRIBUTES.put(RegistryComponent.class, new String[] {"server", "tomcat"});
        ATTRIBUTES.put(AASServerComponent.class, new String[] {"server", "tomcat"});
    }

    /**
     * Registers a {@code server} instance during startup. The tomcat instance must already be set up here.
     * The attribute access path must be known to this class.
     * 
     * @param server the server to register
     */
    public static void registerStart(Object server) {
        String[] attributes = ATTRIBUTES.get(server.getClass());
        if (null == attributes) {
            LoggerFactory.getLogger(Tomcats.class).warn("Access path to tomcat attributes undefined");
        } else {
            registerStart(server, attributes);
        }
    }

    /**
     * Guards a shutdown of {@code server}. The attribute access path must be known to this class.
     * 
     * @param server the server to register
     * @return {@code true} if shutdown seems to be safe, {@code false} if tomcat stop shall not be triggered
     */
    public static boolean guardStop(Object server) {
        boolean result = true;
        String[] attributes = ATTRIBUTES.get(server.getClass());
        if (null == attributes) {
            LoggerFactory.getLogger(Tomcats.class).warn("Access path to tomcat attributes undefined");
        } else {
            result = guardStop(server, attributes);
        }
        return result;
    }

    /**
     * Registers a {@code server} instance during startup. The tomcat instance must already be set up here.
     * The attribute access path must be known to this class.
     * 
     * @param attributes access path in terms of attribute names to the tomcat attribute
     * @param server the server to register
     */
    public static void registerStart(Object server, String...attributes) {
        String name = getTomcatServiceName(server, attributes);
        if (name.length() > 0) {
            TOMCATS.put(name, State.RUNNING);
        }
    }

    /**
     * Guards a shutdown of {@code server}.
     * 
     * @param server the server to register
     * @param attributes access path in terms of attribute names to the tomcat attribute
     * @return {@code true} if shutdown seems to be safe, {@code false} if tomcat stop shall not be triggered
     */
    public static boolean guardStop(Object server, String... attributes) {
        boolean enable = true;
        String name = getTomcatServiceName(server, attributes);
        if (name.length() > 0) {
            enable = State.RUNNING == TOMCATS.get(name);
            TOMCATS.put(name, State.STOPPING);
        }
        return enable;
    }
    
    /**
     * Clears all states.
     */
    public static void clear() {
        TOMCATS.clear();
    }
    
    /**
     * Tries to obtain the tomcat service name.
     * 
     * @param server the instance holding the tomcat instance, may be the tomcat instance itself
     * @param attributes a chain of nested attribute access to the tomcat instance starting in {@code server}, 
     *     may be empty
     * @return the name of the tomcat service (may be empty)
     */
    public static String getTomcatServiceName(Object server, String... attributes) {
        String result = "";
        try {
            for (String a : attributes) {
                Field field = server.getClass().getDeclaredField(a);
                field.setAccessible(true);
                server = field.get(server);
            }
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
        }
        if (server instanceof Tomcat) {
            result = ((Tomcat) server).getService().getName();
        }
        return result;
    }


}
