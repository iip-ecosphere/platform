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

package de.iip_ecosphere.platform.support.plugins;

/**
 * Represents a loaded plugin.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PluginDescriptor {

    /**
     * Returns the unique plugin id.
     * 
     * @return the id
     */
    public String getId();
    
    /**
     * Creates a plugin instance.
     * 
     * @return the plugin instance
     */
    public Plugin<?> createPlugin();
    
}
