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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Generic time series aggregator for identified channels.
 * 
 * @param <I> the input data type
 * @param <O> the output data type
 * @param <D> the data point type within {@code I} and {@code O}
 * @param <T> the timestamp type within {@code I} and {@code O}
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelTimeSeriesAggregator <I, O, D, T> {

    private boolean isAggregating;
    private T aggregationTimestamp; 
    private int numberAggregatedSamples;
    private Map<String, I> timePointAggregator = Collections.synchronizedMap(new HashMap<>());
    private Map<String, List<D>> timeSeriesAggregator = Collections.synchronizedMap(new HashMap<>());
    private AggregationFunction<I, O, D, T> function;

    /**
     * Builds a result from multiple category/data entries.
     * 
     * @param <O> the output data type
     * @param <D> the data point type within {@code I} and {@code O}
     * @param <T> the timestamp type within {@code I} and {@code O}
     * @author Holger Eichelberger, SSE
     */
    public interface ResultBuilder<O, D, T> {

        /**
         * Adds an aggregated data series.
         * 
         * @param category the category the aggregation was performed for
         * @param data the data collected over various point in times until 
         *     {@link AggregationFunction#chunkCompleted(int, Date)}
         * @param timestamp the aggrgation time stamp
         */
        public void addData(String category, List<D> data, T timestamp);

        /**
         * Builds the data after one or multiple calls to {@link #addData(String, List, Date)}.
         * @return the output instance
         */
        public O build();
        
    }

    /**
     * Represents and implements the data format independent access to aggregation data.
     * Individual information may be ignored depending on the actual aggregation.
     *
     * @param <I> the input data type
     * @param <O> the output data type
     * @param <D> the data point type within {@code I} and {@code O}
     * @param <T> the timestamp type within {@code I} and {@code O}
     * @author Holger Eichelberger, SSE
     */
    public interface AggregationFunction<I, O, D, T> {

        /**
         * Returns the aggregation timestamp from {@code input}.
         * 
         * @param input the input data
         * @return the timestamp from {@code input}
         */
        public T getTimestamp(I input);

        /**
         * Returns the aggregation category from {@code input}.
         * 
         * @param input the input data
         * @return the category from {@code input}
         */
        public String getCategory(I input);

        /**
         * Returns a data point from {@code input}.
         * 
         * @param input the input data
         * @return the data point from {@code input}
         */
        public D getData(I input);

        /**
         * Returns whether an aggregation chunk has been completed and a result
         * instance shall be created.
         * 
         * @param numberAggregatedSamples the number of samples aggregated so far
         * @param timestamp the timestamp the aggregation started
         * @return {@code true} for completed, {@code false} else
         */
        public boolean chunkCompleted(int numberAggregatedSamples, T timestamp);
        
        /**
         * Creates a result builder for composing a potentially multi-category result.
         * 
         * @param categoriesCount the actual number of categories
         * @return the result builder instance
         */
        public ResultBuilder<O, D, T> createResult(int categoriesCount);
        
    }

    /**
     * Creates a not-yet-aggregating instance.
     * 
     * @param function the aggreation function
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public ChannelTimeSeriesAggregator(AggregationFunction<I, O, D, T> function) {
        this(true, function);
    }

    /**
     * Creates a new instance.
     * 
     * @param isAggregating whether aggregation shall start immediately
     * @param function the aggreation function
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public ChannelTimeSeriesAggregator(boolean isAggregating, AggregationFunction<I, O, D, T> function) {
        if (null == function) {
            throw new IllegalArgumentException("function must be given");
        }
        this.isAggregating = isAggregating;
        this.function = function;
    }
    
    /**
     * Called to process the input.
     * 
     * @param data the data to process
     * @return the aggregated data, may be <b>null</b> for none at this point in time
     */
    public O process(I data) {
        O result = null;
        if (isAggregating) {
            if (null == aggregationTimestamp) {
                aggregationTimestamp = function.getTimestamp(data);
            }
            String category = function.getCategory(data);
            if (!timePointAggregator.containsKey(category)) {
                timePointAggregator.put(category, data);
            } else {
                Map<String, I> tmp = timePointAggregator;
                timePointAggregator = Collections.synchronizedMap(new HashMap<>());
                for (Map.Entry<String, I> e: tmp.entrySet()) {
                    List<D> ch = timeSeriesAggregator.get(e.getKey());
                    if (null == ch) {
                        ch = Collections.synchronizedList(new LinkedList<>());
                        timeSeriesAggregator.put(e.getKey(), ch);
                    }
                    ch.add(function.getData(e.getValue()));
                }
                numberAggregatedSamples++;
                if (function.chunkCompleted(numberAggregatedSamples, aggregationTimestamp)) {
                    Map<String, List<D>> tmp2 = timeSeriesAggregator;
                    timeSeriesAggregator = Collections.synchronizedMap(new HashMap<>());
                    numberAggregatedSamples = 0;
                    
                    ResultBuilder<O, D, T> builder = function.createResult(tmp2.size());
                    for (Map.Entry<String, List<D>> e : tmp2.entrySet()) {
                        builder.addData(e.getKey(), e.getValue(), aggregationTimestamp);
                    }
                    result = builder.build();
                } // go on aggregating
            }
        }
        return result;
    }
    
    /**
     * Starts the aggregation.
     */
    public void startAggregating() {
        isAggregating = true;
    }
    
    /**
     * Stops the aggregation and clears the data structures.
     */
    public void stopAggregating() {
        isAggregating = false;
        timePointAggregator.clear();
        timeSeriesAggregator.clear();
    }

}
