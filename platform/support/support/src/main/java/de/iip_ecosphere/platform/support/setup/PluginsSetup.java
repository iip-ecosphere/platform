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

package de.iip_ecosphere.platform.support.setup;

/**
 * Provides setup information for plugin loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PluginsSetup {

    /**
     * Returns the (parent) folder where the oktoflow plugins are located (the folder itself or by default 
     * its sub-folders "plugins" or "oktoPlugins").
     * 
     * @return the folder, by default taken from {@link #PARAM_PLUGINS} (env or sys property), fallback "plugins"
     */
    public String getPluginsFolder();

}
