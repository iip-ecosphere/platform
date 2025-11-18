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

package de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.support.plugins.PluginInstanceDescriptor;

/**
 * Declares the type of a service plugin descriptor.
 * 
 * @param <S> the actual type of service being created
 * @author Holger Eichelberger, SSE
 */
public interface ServicePluginDescriptor<S extends Service> extends PluginInstanceDescriptor<ServiceDescriptor<S>> {

    public static final String PLUGIN_ID_PREFIX = "service-";
    public static final String PLUGIN_TEST_ID_PREFIX = PLUGIN_ID_PREFIX + "test-";
    
}
