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

import java.io.InputStream;
import java.util.List;

/**
 * A basic implementation of the {@link ServicePluginDescriptor} delegating to the {@link ServiceDescriptor}.
 * 
 * The {@link #create()} method contains a convenience/default implementation by reflection through an assumed
 * no-arg constructor that must anyway exist for JSL. May be overridden if needed. 
 * 
 * All creation methods are implemented with <b>null</b> return value so that implementing descriptor classes need to
 * declare less methods. However, if you describe a generic service, implement 
 * {@link #createService(YamlService, Object...)} and if you describe a specific service implement 
 * {@link #createService(String, InputStream)} as well as the fallbacks 
 * {@link #createService(String)} and {@link #createService()}.
 * 
 * @param <S> the actual type of service being created
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractGenericServicePluginDescriptor<S extends Service> 
    extends AbstractServicePluginDescriptor<S> {

    /**
     * Creates an instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     */
    public AbstractGenericServicePluginDescriptor(String id, List<String> ids) {
        super(id, ids);
    }

    @Override
    public S createService(String serviceId, InputStream ymlFile) {
        return null;
    }

    @Override
    public S createService(String serviceId) {
        return null;
    }

    @Override
    public S createService() {
        return null;
    }
    
}
