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

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import de.iip_ecosphere.platform.support.CollectionUtils;

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
        List<D> tmp = CollectionUtils.toList(loader.iterator());
        Optional<D> first = tmp.stream()
            .filter(d -> !hasExcludeFirst(d))
            .findFirst();
        if (first.isEmpty()) {
            first = loader.findFirst();
        }
        return first;
    }
    
    /**
     * Returns whether an {@link instance} is tagged with the {@link ExcludeFirst} annotation.
     * 
     * @param instance the instance to check (may be <b>null</b>)
     * @return {@code true} if the annotation is present, {@code false} else
     */
    public static boolean hasExcludeFirst(Object instance) {
        return null != instance && instance.getClass().isAnnotationPresent(ExcludeFirst.class);
    }

}
