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

package test.de.iip_ecosphere.platform.transport;

import java.io.File;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * A builder for delayed building of test server plugin server instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestServerBuilder implements Builder<AbstractTestServer> {

    private String id;
    private ServerAddress addr;
    private InstanceCreator instanceCreator;
    private File installDir;

    /**
     * Creates test server instances.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface InstanceCreator {

        /**
         * Creates test server instances.
         * 
         * @param id the plugin id
         * @param address the server address
         * @param installDir the plugin installation directory, may be <b>null</b>
         * @return the test server instance
         */
        public AbstractTestServer createInstance(String id, ServerAddress address, File installDir);
    }
    
    /**
     * Creates a builder instance.
     * 
     * @param id the plugin id
     * @param instanceCreator the server instance creator
     * @param installDir the plugin installation directory, may be <b>null</b>
     */
    public TestServerBuilder(String id, InstanceCreator instanceCreator, File installDir) {
        this.id = id;
        this.instanceCreator = instanceCreator;
        this.installDir = installDir;
    }
    
    /**
     * Creates the server instance.
     * 
     * @param addr the server address (schema ignored)
     */
    public TestServerBuilder setAddress(ServerAddress addr) {
        this.addr = addr;
        return this;
    }
    
    @Override
    public AbstractTestServer build() {
        return instanceCreator.createInstance(id, addr, installDir);
    }

    /**
     * Creates a builder from the given plugin id via the {@link PluginManager}.
     * 
     * @param pluginId the plugin id
     * @return the test server builder
     * @throws NullPointerException if no such plugin exists
     */
    public static TestServerBuilder fromPlugin(String pluginId) {
        return PluginManager.getPlugin(TestServerBuilder.class, pluginId).getInstance();
    }

    /**
     * Creates a test server instance from the given plugin id via the {@link PluginManager} via the 
     * {@link TestServerBuilder}.
     * 
     * @param pluginId the plugin id
     * @return the test server builder
     * @throws NullPointerException if no such plugin exists
     */
    public static AbstractTestServer fromPlugin(String pluginId, ServerAddress address) {
        return fromPlugin(pluginId)
            .setAddress(address)
            .build();
    }

}
