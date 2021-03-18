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

import java.util.Optional;
import java.util.ServiceLoader;

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
        Optional<D> first = loader.stream()
            .map(p -> p.get())
            .filter(d -> !d.getClass().isAnnotationPresent(ExcludeFirst.class))
            .findFirst();
        if (first.isEmpty()) {
            first = loader.findFirst();
        }
        return first;
    }

}
