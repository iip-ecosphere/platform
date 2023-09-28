/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.kiServices.functions.aggregation;

/**
 * Generic single-data-point time series aggregator for identified channels.
 * 
 * @param <I> the input data type
 * @param <O> the output data type
 * @param <D> the data point type within {@code I} and {@code O}
 * @param <T> the timestamp type within {@code I} and {@code O}
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelTimeSeriesAggregator<I, O, D, T> extends MultiChannelTimeSeriesAggregator<I, I, O, D, T> {

    /**
     * Creates a not-yet-aggregating instance.
     * 
     * @param function the aggreation function
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public ChannelTimeSeriesAggregator(AggregationFunction<I, I, O, D, T> function) {
        this(true, function);
    }
    
    /**
     * Creates a new instance for single data points.
     * 
     * @param isAggregating whether aggregation shall start immediately
     * @param function the aggregation function
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public ChannelTimeSeriesAggregator(boolean isAggregating, AggregationFunction<I, I, O, D, T> function) {
        super(isAggregating, function, i -> new SingleValueIterator<I>(i));
    }

    /**
     * Called to process the input.
     * 
     * @param data the data to process
     * @return the aggregated data, may be <b>null</b> for none at this point in time
     */
    public O process(I data) {
        O result = null;
        if (isAggregating()) { // avoid creating an iterator
            result = process(data, data);
        }
        return result;
    }

}
