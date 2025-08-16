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
 * Represents a counter.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Counter extends Meter {

    /**
     * Builds a counter.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface CounterBuilder extends MetricsBuilder<Counter, CounterBuilder> {
        
        /**
         * Adds a base unit to the counter being built.
         * 
         * @param unit base unit of the eventual counter, may be <b>null</b>
         * @return <b>this</b> for chaining
         */
        public CounterBuilder baseUnit(String unit);
        
    }
    
    /**
     * Update the counter by one.
     */
    default void increment() {
        increment(1.0);
    }

    /**
     * Update the counter by {@code amount}.
     *
     * @param amount amount to add to the counter
     */
    public void increment(double amount);
    
    /**
     * Returns the cumulative count.
     * 
     * @return The cumulative count since this counter was created.
     */
    public double count();
    
}
