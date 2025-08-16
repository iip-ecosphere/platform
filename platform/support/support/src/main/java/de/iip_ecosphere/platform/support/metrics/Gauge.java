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

package de.iip_ecosphere.platform.support.metrics;

/**
 * Represents a gauge.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Gauge extends Meter {

    /**
     * Builds a gauge.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface GaugeBuilder<T> extends MetricsBuilder<Gauge, GaugeBuilder<T>> {
        
        /**
         * Adds a base unit to the counter being built.
         * 
         * @param unit base unit of the eventual counter, may be <b>null</b>
         * @return <b>this</b> for chaining
         */
        public GaugeBuilder<T> baseUnit(String unit);

    }    
    
    /**
     * The act of observing the value by calling this method triggers sampling
     * of the underlying number or user-defined function that defines the value for the gauge.
     *
     * @return The current value.
     */
    public double value();

}
