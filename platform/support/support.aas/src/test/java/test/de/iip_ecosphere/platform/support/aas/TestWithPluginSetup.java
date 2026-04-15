/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

/**
 * Testing setup for plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWithPluginSetup {

    private static String aasPluginId;
    private static boolean withBasyx2 = true;
    private static boolean withBasyx = true;
    private static boolean withBasyxServer = false; // optional
    
    /**
     * Returns the plugin id to use.
     * 
     * @param dflt the default value if unspecified
     * @return the plugin id to use, may be {@code dflt}
     */
    public static String getAasPluginId(String dflt) {
        return null == aasPluginId ? dflt : aasPluginId;
    }

    /**
     * Sets the plugin id for BaSyx2.
     */
    public static void setBasyx2AasPluginId() {
        aasPluginId = "aas.basyx-2.0";
    }

    /**
     * Sets the plugin id for BaSyx1.3.
     */
    public static void setBasyx13AasPluginId() {
        aasPluginId = "aas.basyx-1.3";
    }

    static {
        setBasyx2AasPluginId();  // for now
    }

    /**
     * Returns whether BaSyx2 is enabled in plugin testing.
     * 
     * @return whether BaSyx2 is enabled in plugin testing
     */
    public static boolean isWithBasyx2() {
        return withBasyx2;
    }
    
    /**
     * Specifies whether BaSyx2 is enabled in plugin testing.
     *
     * @param withBasyx2 enables/disables BaSyx2
     */
    public static void setWithBasyx2(boolean withBasyx2) {
        TestWithPluginSetup.withBasyx2 = withBasyx2;
    }

    /**
     * Returns whether BaSyx (without server) is enabled in plugin testing.
     * 
     * @return whether BaSyx (without server) is enabled in plugin testing
     */
    public static boolean isWithBasyx() {
        return withBasyx;
    }

    /**
     * Specifies whether BaSyx (without server) is enabled in plugin testing.
     *
     * @param withBasyx enables/disables BaSyx
     */
    public static void setWithBasyx(boolean withBasyx) {
        TestWithPluginSetup.withBasyx = withBasyx;
    }

    /**
     * Returns whether BaSyx server is enabled in plugin testing.
     * 
     * @return whether BaSyx server is enabled in plugin testing
     */
    public static boolean isWithBasyxServer() {
        return withBasyxServer;
    }

    /**
     * Specifies whether BaSyx server is enabled in plugin testing.
     *
     * @param withBasyxServer enables/disables BaSyx server
     */
    public static void setWithBasyxServer(boolean withBasyxServer) {
        TestWithPluginSetup.withBasyxServer = withBasyxServer;
    }

}
