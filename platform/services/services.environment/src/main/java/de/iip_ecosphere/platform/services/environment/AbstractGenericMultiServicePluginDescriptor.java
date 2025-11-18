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

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * A basic implementation of the {@link ServicePluginDescriptor} delegating to the {@link ServiceDescriptor} for MIMO 
 * generic services.
 * 
 * @param <S> the actual type of service being created
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractGenericMultiServicePluginDescriptor<S extends Service> 
    extends AbstractServicePluginDescriptor<S> {

    /**
     * Creates an instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     */
    public AbstractGenericMultiServicePluginDescriptor(String id, List<String> ids) {
        super(id, ids);
    }

    /**
     * Creates an instance with a single secondary id.
     * 
     * @param id the plugin id
     * @param secId the secondary id
     */
    public AbstractGenericMultiServicePluginDescriptor(String id, String secId) {
        super(id, List.of(secId));
    }

    /**
     * Creates an instance with a class determining the single secondary id.
     * 
     * @param id the plugin id
     * @param secCls the class determining the secondary id (by its qualified name)
     */
    public AbstractGenericMultiServicePluginDescriptor(String id, Class<?> secCls) {
        this(id, secCls.getName());
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
    
    @Override
    public <I, O> S createService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml, Object... args) {
        return null;
    }
    
}
