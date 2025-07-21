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

package de.oktoflow.platform.support.yaml.snakeyaml;

import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import de.iip_ecosphere.platform.support.yaml.YamlProviderDescriptor;

/**
 * The Snakeyaml plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SnakeYamlPluginDescriptor extends SingletonPluginDescriptor<Yaml> implements YamlProviderDescriptor  {
    
    /**
     * Creates the descriptor.
     */
    public SnakeYamlPluginDescriptor() {
        super("yaml-snakeyaml", null, Yaml.class, p -> new SnakeYaml());
    }

    @Override
    public Yaml create() {
        return new SnakeYaml();
    }
    
}
