/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.plugins;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.plugins.DefaultPluginDescriptor;

/**
 * A test plugin playing the role of a singleton. Class shall be neither referenced from production nor from test code! 
 * 
 * @author Holger Eichelberger, SSE
 */
public class MyTestPlugin implements Server {
    
    public static final String ID = "test-plugin";
    private static final MyTestPlugin INSTANCE = new MyTestPlugin();

    /**
     * The plugin descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyTestPluginDescriptor extends DefaultPluginDescriptor<Server> {

        /**
         * Creates an instance.
         */
        public MyTestPluginDescriptor() {
            super(ID, null, Server.class, () -> INSTANCE);
        }
        
    }
    
    /**
     * Prevents external creation.
     */
    private MyTestPlugin() {
    }

    @Override
    public Server start() {
        System.out.println("STARTING...");
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        System.out.println("STOPPING...");
    }
    
    // typically hooks itself into something, e.g., using JSL
    // here, we are only interested whether the plugin manager has then an instance of this class
    // descriptor file is in src/test/plugins/resources/META-INF/services as this shall be an "external" jar (to be
    // packaged in maven before tests)

}
