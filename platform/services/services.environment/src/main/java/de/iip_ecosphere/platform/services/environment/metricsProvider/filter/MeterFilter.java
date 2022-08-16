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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.micrometer.core.instrument.Meter;

/**
 * Defines an interface to filter meters before exporting them, e.g., to JSON.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface MeterFilter extends Predicate<Meter> {

    /**
     * Defines filter types to be considered while processing the filters. Additional types will
     * require a modification of the filtering.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum Type {

        /**
         * A filter that includes a given metric at the actual position of the filter chain 
         * for further filtering.
         */
        INCLUSION,

        /**
         * A filter that excludes a given metric at the actual position of the filter chain 
         * for further filtering.
         */
        EXCLUSION
    }
    
    /**
     * Returns the type of the meter filter.
     * 
     * @return the type
     */
    public Type getType();

    /**
     * Filters the given meters according to the specified filters.
     * 
     * @param meters the meters to be filtered
     * @param copy copy the {@code meters} if filters are to be applied or if {@code false} modify meters directly
     * @param filter the filters to be applied. The first matching {@link Type#EXCLUSION} filter will remove a metric
     *    from the result list, an {@link Type#INCLUSION} filter allows for further filtering.
     * @return the filtered metrics list
     */
    public static List<Meter> filter(List<Meter> meters, boolean copy, MeterFilter... filter) {
        List<Meter> result = meters;
        if (filter.length > 0 && meters.size() > 0) {
            if (copy) {
                result = new ArrayList<Meter>(meters);
            }
            for (int m = meters.size() - 1; m >= 0; m--) {
                Meter meter = meters.get(m);
                boolean keep = true;
                for (int f = 0; keep && f < filter.length; f++) {
                    boolean applies = filter[f].test(meter);
                    if (Type.EXCLUSION == filter[f].getType()) {
                        keep = !applies; // if filter applies, throw out metric
                    } else {
                        keep = applies; // if filter applies, keep metric
                    }
                }
                if (!keep) {
                    result.remove(m);
                }
            }
        }
        return result;
    }
    
}
