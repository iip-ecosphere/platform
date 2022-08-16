/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.metricsProvider.filter;

import io.micrometer.core.instrument.Meter;

/**
 * A filter based on a prefix of the metrics id/name.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MeterNamePrefixFilter extends AbstractMeterFilter {

    private String prefix;
    
    /**
     * Creates a filter that includes/excludes metrics based on a given prefix.
     * 
     * @param type the type of the filter influencing the filtering behavior
     * @param prefix the prefix to filter
     * @throws IllegalArgumentException if the prefix is null
     */
    public MeterNamePrefixFilter(Type type, String prefix) {
        super(type);
        if (prefix == null) {
            throw new IllegalArgumentException("prefix is null!");
        }
        this.prefix = prefix;
    }

    @Override
    public boolean test(Meter meter) {
        return meter.getId().getName().startsWith(prefix);
    }

}
