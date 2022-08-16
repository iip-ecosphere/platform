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
 * A filter based the metrics id/name.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MeterNameFilter extends AbstractMeterFilter {

    private String name;
    
    /**
     * Creates a filter that includes/excludes metrics based on a given prefix.
     * 
     * @param type the type of the filter influencing the filtering behavior
     * @param name the name that must be equal
     * @throws IllegalArgumentException if the name is null
     */
    public MeterNameFilter(Type type, String name) {
        super(type);
        if (name == null) {
            throw new IllegalArgumentException("name is null!");
        }
        this.name = name;
    }

    @Override
    public boolean test(Meter meter) {
        return name.equals(meter.getId().getName());
    }

}
