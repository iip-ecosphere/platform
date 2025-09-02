/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.jsl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.PluginSetup;

/**
 * Helper functions for Java Service Loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceLoaderUtils {

    /**
     * Filters excluded descriptors, i.e., descriptors marked with {@link ExcludeFirst}. If selection by
     * filtering does not work, the first descriptor is returned.
     * 
     * @param <D> the descriptor type
     * @param descriptorClass the descriptor class
     * @return the found descriptor, may be an empty optional instance
     */
    public static <D> Optional<D> filterExcluded(Class<D> descriptorClass) {
        ServiceLoader<D> loader = ServiceLoader.load(descriptorClass);
        // in test settings, the fake descriptor may also be there - filter it out
        Optional<D> first = stream(loader)
            .filter(d -> !hasExcludeFirst(d))
            .findFirst();
        if (!first.isPresent()) { // JDK 1.8
            first = findFirst(loader);
        }
        return first;
    }
    
    /**
     * Convenience method for {@link #findFirst(ServiceLoader)} based on default loading of the service descriptors.
     * 
     * @param <D> the descriptor type
     * @param descriptorClass the descriptor class
     * @return the first service provider
     */
    public static <D> Optional<D> findFirst(Class<D> descriptorClass) {
        return findFirst(ServiceLoader.load(descriptorClass));
    }

    /**
     * Load the first available service provider of the given {@code loader}'s service. [JDK 1.8 compatibility]
     *
     * @param <D> the service descriptor type
     * @param loader the loader instance
     * @return the first service provider
     */
    public static <D> Optional<D> findFirst(ServiceLoader<D> loader) {
        Iterator<D> iterator = loader.iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Turns the service loader into a stream. [JDK 1.8 compatibility]
     * 
     * @param <D> the service descriptor type
     * @param loader the loader instance
     * @return the stream of descriptor
     */
    public static <D> Stream<D> stream(ServiceLoader<D> loader) {
        List<D> result = new ArrayList<D>();
        Iterator<D> iterator = loader.iterator();  // JDK 1.8
        while (iterator.hasNext()) {
            try {
                result.add(iterator.next());
            } catch (ServiceConfigurationError e) {
                LoggerFactory.getLogger(ServiceLoaderUtils.class).error(
                    "Service configuration error: {}", e.getMessage());
            }
        }
        return result.stream();
    }
    
    /**
     * Returns whether an {@code instance} is tagged with the {@link ExcludeFirst} annotation.
     * 
     * @param instance the instance to check (may be <b>null</b>)
     * @return {@code true} if the annotation is present, {@code false} else
     */
    public static boolean hasExcludeFirst(Object instance) {
        return null != instance && instance.getClass().isAnnotationPresent(ExcludeFirst.class);
    }
    
    /**
     * Creates a service loader for the given {@code cls} using {@link PluginSetup#getClassLoader()} as class loader.
     * 
     * @param <D> the descriptor type
     * @param cls the class of the descriptor type
     * @return the service loader
     */
    public static <D> ServiceLoader<D> load(Class<D> cls) { 
        return ServiceLoader.load(cls, PluginSetup.getClassLoader());
    }
    
}
