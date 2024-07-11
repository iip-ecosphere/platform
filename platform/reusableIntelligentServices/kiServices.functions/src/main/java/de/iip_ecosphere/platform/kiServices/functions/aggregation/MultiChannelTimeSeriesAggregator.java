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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Generic multi-data-point time series aggregator for identified channels.
 * 
 * @param <I> the input data type
 * @param <CI> the contained input data type in &lt;I&gt;, may be the same as &lt;I&gt; for 
 *     single-data-point aggregation 
 * @param <O> the output data type
 * @param <D> the data point type within {@code I} and {@code O}
 * @param <T> the timestamp type within {@code I} and {@code O}
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiChannelTimeSeriesAggregator <I, CI, O, D, T> {

    private boolean isAggregating;
    private T aggregationTimestamp; 
    private int numberAggregatedSamples;
    private Map<String, CI> timePointAggregator = Collections.synchronizedMap(new HashMap<>());
    private Map<String, List<D>> timeSeriesAggregator = Collections.synchronizedMap(new HashMap<>());
    private AggregationFunction<I, CI, O, D, T> function;
    private Function<I, Iterator<CI>> iteratorProvider;

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
         *     {@link AggregationFunction#chunkCompleted(int, Object)}
         * @param timestamp the aggrgation time stamp
         */
        public void addData(String category, List<D> data, T timestamp);

        /**
         * Builds the data after one or multiple calls to {@link #addData(String, List, Object)}.
         * @return the output instance
         */
        public O build();
        
    }

    /**
     * Determines whether an aggregation chunk is completed.
     * 
     * @param <T> the timestamp type within {@code I} and {@code O}
     * @author Holger Eichelberger, SSE
     */
    public interface CompletionFunction<T> {

        /**
         * Returns whether an aggregation chunk has been completed and a result
         * instance shall be created.
         * 
         * @param numberAggregatedSamples the number of samples aggregated so far
         * @param timestamp the timestamp the aggregation started
         * @return {@code true} for completed, {@code false} else
         */
        public boolean chunkCompleted(int numberAggregatedSamples, T timestamp);

    }

    /**
     * Represents the data format independent access to aggregation data for single data points.
     * Individual information may be ignored depending on the actual aggregation.
     *
     * @param <I> the input data type
     * @param <O> the output data type
     * @param <D> the data point type within {@code I} and {@code O}
     * @param <T> the timestamp type within {@code I} and {@code O}
     * @author Holger Eichelberger, SSE
     */
    public interface AggregationFunction<I, CI, O, D, T> extends CompletionFunction<T> {

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
        public String getCategory(CI input);

        /**
         * Returns a data point from {@code input}.
         * 
         * @param input the input data
         * @return the data point from {@code input}
         */
        public D getData(CI input);

        
        /**
         * Creates a result builder for composing a potentially multi-category result.
         * 
         * @param categoriesCount the actual number of categories
         * @return the result builder instance
         */
        public ResultBuilder<O, D, T> createResult(int categoriesCount);
        
    }
    
    /**
     * Basic aggregation function delegating the operations to (lambda) functions.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param <D> the data point type within {@code I} and {@code O}
     * @param <T> the timestamp type within {@code I} and {@code O}
     * @author Holger Eichelberger, SSE
     */
    public static class LambdaBasedAggregationFunction<I, CI, O, D, T> implements AggregationFunction<I, CI, O, D, T> {

        private Function<I, T> timestampProvider;
        private Function<CI, String> categoryProvider;
        private Function<CI, D> dataProvider;
        private CompletionFunction<T> completionFunction;
        private Function<Integer, ResultBuilder<O, D, T>> resultBuilderProvider;
        
        /**
         * Creates a lambda based aggregation function. All parameters must not be <b>null</b>.
         * 
         * @param timestampProvider the timestamp provider implementing {@link #getTimestamp(Object)}
         * @param categoryProvider the timestamp provider implementing {@link #getCategory(Object)}
         * @param dataProvider the data provider implementing {@link #getData(Object)}
         * @param completionFunction the completion function provider implementing {@link #chunkCompleted(int, Object)}
         * @param resultBuilderProvider the result builder function provider implementing {@link #createResult(int)}
         */
        public LambdaBasedAggregationFunction(Function<I, T> timestampProvider, Function<CI, String> categoryProvider, 
            Function<CI, D> dataProvider, CompletionFunction<T> completionFunction, Function<Integer, 
            ResultBuilder<O, D, T>> resultBuilderProvider) {
            this.timestampProvider = timestampProvider;
            this.categoryProvider = categoryProvider;
            this.dataProvider = dataProvider;
            this.resultBuilderProvider = resultBuilderProvider;
            this.completionFunction = completionFunction;
        }
        
        @Override
        public T getTimestamp(I input) {
            return timestampProvider.apply(input);
        }

        @Override
        public String getCategory(CI input) {
            return categoryProvider.apply(input);
        }

        @Override
        public D getData(CI input) {
            return dataProvider.apply(input);
        }

        @Override
        public boolean chunkCompleted(int numberAggregatedSamples, T timestamp) {
            return completionFunction.chunkCompleted(numberAggregatedSamples, timestamp);
        }

        @Override
        public ResultBuilder<O, D, T> createResult(int categoriesCount) {
            return resultBuilderProvider.apply(categoriesCount);
        }
        
    }
    
    /**
     * An iterator over a single value.
     * 
     * @param <I> the type to iterate over
     * @author Holger Eichelberger, SSE
     */
    public static class SingleValueIterator<I> implements Iterator<I> {

        private I value;
        private boolean hasNext = true;
        
        /**
         * Creates an iterator instance.
         * 
         * @param value the single value to iterate over, must not be <b>null</b>
         */
        public SingleValueIterator(I value) {
            this.value = value;
        }
        
        @Override
        public boolean hasNext() {
            boolean result = hasNext;
            hasNext = false;
            return result;
        }

        @Override
        public I next() {
            if (null == value) {
                throw new NoSuchElementException();
            }
            I v = value;
            value = null;
            return v;
        }
        
    }
    
    /**
     * Iterator for arrays.
     * 
     * @param <I> the type to iterate over
     * @author Holger Eichelberger, SSE
     */
    public static class ArrayIterator<I> implements Iterator<I> {

        private int pos = 0;
        private I[] array;
        
        /**
         * Creates an iterator instance.
         * 
         * @param array the single value to iterate over, must not be <b>null</b>
         */
        public ArrayIterator(I[] array) {
            this.array = array;
        }
        
        @Override
        public boolean hasNext() {
            return array.length > pos;
        }

        @Override
        public I next() {
            try {
                return array[pos++];
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }
        
    }

    /**
     * Creates a not-yet-aggregating instance.
     * 
     * @param function the aggreation function
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public MultiChannelTimeSeriesAggregator(AggregationFunction<I, CI, O, D, T> function) {
        this(true, function);
    }

    /**
     * Creates a new instance for single data points.
     * 
     * @param isAggregating whether aggregation shall start immediately
     * @param function the aggregation function
     * @param iteratorProvider function creating an iterator from &lt;I&gt; over &lt;CI&gt;, may be <b>null</b> for none
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public MultiChannelTimeSeriesAggregator(boolean isAggregating, AggregationFunction<I, CI, O, D, T> function, 
        Function<I, Iterator<CI>> iteratorProvider) {
        if (null == function) {
            throw new IllegalArgumentException("function must be given");
        }
        if (null == iteratorProvider) {
            iteratorProvider = i -> null;
        }
        this.isAggregating = isAggregating;
        this.function = function;
        this.iteratorProvider = iteratorProvider;
    }

    /**
     * Creates a new instance for single data points.
     * 
     * @param isAggregating whether aggregation shall start immediately
     * @param function the aggregation function
     * @throws IllegalArgumentException if {@code function} is not given
     */
    public MultiChannelTimeSeriesAggregator(boolean isAggregating, AggregationFunction<I, CI, O, D, T> function) {
        this(isAggregating, function, null);
    }
    
    /**
     * Returns whether aggregation is enabled.
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    public boolean isAggregating() {
        return isAggregating;
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
            Iterator<CI> iterator = iteratorProvider.apply(data);
            while (iterator.hasNext()) {
                O res = process(data, iterator.next());
                if (null == result) {
                    result = res;
                }
            }
        }
        return result;
    }
    
    /**
     * Processes a single data item.
     * 
     * @param parent the parent data item holding {@code data}, may be identical to {@code data}
     * @param data the data item
     * @return the aggregated data, may be <b>null</b> for none at this point in time
     */
    protected O process(I parent, CI data) {
        O result = null;
        if (null == aggregationTimestamp) {
            aggregationTimestamp = function.getTimestamp(parent);
        }
        String category = function.getCategory(data);
        if (!timePointAggregator.containsKey(category)) {
            timePointAggregator.put(category, data);
        } else {
            Map<String, CI> tmp = timePointAggregator;
            timePointAggregator = Collections.synchronizedMap(new HashMap<>());
            for (Map.Entry<String, CI> e: tmp.entrySet()) {
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
