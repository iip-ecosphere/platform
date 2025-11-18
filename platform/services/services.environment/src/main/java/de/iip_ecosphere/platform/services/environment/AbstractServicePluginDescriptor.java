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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * A basic implementation of the {@link ServicePluginDescriptor} delegating to the {@link ServiceDescriptor}.
 * 
 * The {@link #create()} method contains a convenience/default implementation by reflection through an assumed
 * no-arg constructor that must anyway exist for JSL. May be overridden if needed. 
 * 
 * @param <S> the actual type of service being created
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServicePluginDescriptor<S extends Service> 
    extends SingletonPluginDescriptor<ServiceDescriptor<S>> 
    implements ServicePluginDescriptor<S>, ServiceDescriptor<S> {

    /**
     * Creates an instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     */
    public AbstractServicePluginDescriptor(String id, List<String> ids) {
        super(id, ids, null, null); // requires initPluginClass and initiPluginSupplier
    }
    
    @Override
    protected PluginSupplier<ServiceDescriptor<S>> initPluginSupplier(
        PluginSupplier<ServiceDescriptor<S>> pluginSupplier) {
        return p -> this;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Class<ServiceDescriptor<S>> initPluginClass(Class<ServiceDescriptor<S>> pluginClass) {
        Class<?> result = ServiceDescriptor.class; // the "static" default
        // if there is embedded generic type information...
        Class<?> aspdCls = getClass().getSuperclass(); // the calling class shall be a super class of this
        while (aspdCls != null && !aspdCls.getName().equals(AbstractServicePluginDescriptor.class.getName())) {
            aspdCls = aspdCls.getSuperclass();
        }
        if (null != aspdCls) {
            for (Class<?> i : aspdCls.getInterfaces()) {
                if (i.isAssignableFrom(ServiceDescriptor.class)) {
                    result = i; // it it keeps the generic info
                }
            }
        }
        return (Class<ServiceDescriptor<S>>) result;
    }

    @Override
    public ServiceDescriptor<S> create() {
        // a convenience default implementation assuming a no-arg constructor
        ServiceDescriptor<S> result = null;
        try {
            @SuppressWarnings("unchecked")
            Constructor<? extends AbstractServicePluginDescriptor<S>> c 
                = (Constructor<? extends AbstractServicePluginDescriptor<S>>) getClass().getDeclaredConstructor();
            c.setAccessible(true); // to be on the safe side, also for tests
            result = c.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException 
            | InstantiationException e) {
            LoggerFactory.getLogger(this).error("Convenience creation of instance failed. Please override create(). "
                + "Reason: {}", e.getMessage());
        }
        return result;
    }
    
}
