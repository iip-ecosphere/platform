/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Before;

import de.iip_ecosphere.platform.support.aas.AasFactory;

/**
 * Plugin-based test wit default setup for AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWithPlugin extends test.de.iip_ecosphere.platform.support.TestWithPlugin {

    private static String aasPluginId = "aas.basyx-2.0"; // shall become the default id then
    
    /**
     * Changes the AAS plugin Id used for testing.
     * 
     * @param id the new plugin Id, ignored if <b>null</b> or empty
     * @return the plugin id before trying to change the value
     */
    public static String setAasPluginId(String id) {
        String old = aasPluginId;
        if (null != id && id.length() > 0) {
            aasPluginId = id;
        }
        return old;
    }
    
    static {
        setupAASPlugins();
    }

    /**
     * Sets up the AAS plugins for testing.
     */
    public static void setupAASPlugins() {
        addPluginLocation("support", "support.aas.basyx2", "basyx2", false, "support.log-slf4j-simple");
        addPluginLocation("support", "support.aas.basyx", "basyx", false, "support.log-slf4j-simple");
        addRunAfterLoading(() -> {
            // TODO default from AASFactory
            AasFactory.setPluginId(System.getProperty("okto.test.aas.pluginId", aasPluginId)); 
        });
    }
    
    /**
     * Sets up plugins. Non-static so that loading is inherited.
     */
    @Before
    public void setup() {
        super.setup();
    }

}
