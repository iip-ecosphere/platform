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

package de.oktoflow.platform.support.json.jackson;

import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonProviderDescriptor;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The Jackson plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JacksonJsonPluginDescriptor extends SingletonPluginDescriptor<Json> implements JsonProviderDescriptor  {
    
    /**
     * Creates the descriptor.
     */
    public JacksonJsonPluginDescriptor() {
        super("json-jackson", null, Json.class, p -> new JacksonJson());
    }

    @Override
    public Json create() {
        return new JacksonJson();
    }
    
}
