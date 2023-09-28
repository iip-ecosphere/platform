
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

package test.de.iip_ecosphere.platform.kiServices.functions.aggregation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.kiServices.functions.aggregation.ChannelTimeSeriesAggregator;
import de.iip_ecosphere.platform.kiServices.functions.aggregation.MultiChannelTimeSeriesAggregator;
import de.iip_ecosphere.platform.kiServices.functions.aggregation.MultiChannelTimeSeriesAggregator.*;
import de.iip_ecosphere.platform.services.environment.IipStringStyle;

/**
 * Tests {@link ChannelTimeSeriesAggregator} and {@link MultiChannelTimeSeriesAggregator}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelTimeSeriesAggregatorTest {

    /**
     * Represents data to be aggregated.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class PlcEnergyMeasurement {
        
        private String value;
        private java.util.Date timestamp;
        private String channel;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, IipStringStyle.SHORT_STRING_STYLE);
        }

    }

    /**
     * Represents aggregated data.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class AggregatedPlcEnergyMeasurement {
        
        private PlcEnergyMeasurementChannel[] channels;
        
        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, IipStringStyle.SHORT_STRING_STYLE);
        }

    }

    /**
     * Represents nested aggregated data.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class PlcEnergyMeasurementChannel {
        
        private double[] values;
        private java.util.Date timestamp;
        private String channel;
        private double samplerate;
        
        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, IipStringStyle.SHORT_STRING_STYLE);
        }

    }
    
    /**
     * Represents data to be aggregated (multi-aggregation).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MultiPlcEnergyMeasurement {
        
        private MultiPlcEnergyMeasurementDatapoints datapoints = new MultiPlcEnergyMeasurementDatapoints();

    }

    /**
     * Represents the multi-data to be aggregated.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MultiPlcEnergyMeasurementDatapoints {

        private java.util.Date timestamp;
        private MultiPlcEnergyMeasurementDatapoint[] datapoints;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, IipStringStyle.SHORT_STRING_STYLE);
        }

    }

    /**
     * Represents a data point to be aggregated (multi-aggregation).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MultiPlcEnergyMeasurementDatapoint {

        private String value;
        private String channel;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, IipStringStyle.SHORT_STRING_STYLE);
        }

    }

    /**
     * Converts the {@link PlcEnergyMeasurement#value} from {@code input}.
     * 
     * @param input the input instance
     * @return the converted value
     */
    private static Double getValue(PlcEnergyMeasurement input) {
        Double result;
        try {
            result = Double.valueOf(input.value);
        } catch (NumberFormatException ex) {
            result = -1.0;
            LoggerFactory.getLogger(ChannelTimeSeriesAggregatorTest.class).info("Cannot add received value: {}", 
                ex.getMessage());
        }
        return result;
    }

    /**
     * Converts the {@link MultiPlcEnergyMeasurementDatapoint#value} from {@code input}.
     * 
     * @param input the input instance
     * @return the converted value
     */
    private static Double getValue(MultiPlcEnergyMeasurementDatapoint input) {
        Double result;
        try {
            result = Double.valueOf(input.value);
        } catch (NumberFormatException ex) {
            result = -1.0;
            LoggerFactory.getLogger(ChannelTimeSeriesAggregatorTest.class).info("Cannot add received value: {}", 
                ex.getMessage());
        }
        return result;
    }
    
    /**
     * The aggregation function in a bit lazy form merged with the {@link ResultBuilder}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class AggFunction implements AggregationFunction<PlcEnergyMeasurement, 
        PlcEnergyMeasurement, AggregatedPlcEnergyMeasurement, Double, Date>, ResultBuilder<
            AggregatedPlcEnergyMeasurement, Double, Date> {

        private PlcEnergyMeasurementChannel[] channels;
        private int pos;
        
        @Override
        public Date getTimestamp(PlcEnergyMeasurement input) {
            return input.timestamp;
        }

        @Override
        public String getCategory(PlcEnergyMeasurement input) {
            return input.channel;
        }

        @Override
        public Double getData(PlcEnergyMeasurement input) {
            return getValue(input);
        }

        @Override
        public boolean chunkCompleted(int numberAggregatedSamples, Date timestamp) {
            return (numberAggregatedSamples >= 3);
        }

        @Override
        public ResultBuilder<AggregatedPlcEnergyMeasurement, Double, Date> createResult(int categoriesCount) {
            pos = 0;
            channels = new PlcEnergyMeasurementChannel[categoriesCount];
            return this; // lazy
        }

        @Override
        public void addData(String category, List<Double> data, Date timestamp) {
            PlcEnergyMeasurementChannel ch = new PlcEnergyMeasurementChannel();
            ch.channel = category;
            ch.samplerate = 0.250;
            ch.timestamp = timestamp;
            double[] valsArray = new double[data.size()];
            for (int v = 0, size = data.size(); v < size; v++) {
                valsArray[v] = data.get(v);
            }
            ch.values = valsArray;
            channels[pos++] = ch;
        }

        @Override
        public AggregatedPlcEnergyMeasurement build() {
            AggregatedPlcEnergyMeasurement result = new AggregatedPlcEnergyMeasurement();
            result.channels = channels;
            pos = 0;
            channels = null;
            return result;
        }
        
    }

    /**
     * Tests {@link ChannelTimeSeriesAggregator}.
     */
    @Test
    public void testAggregator() {
        testAggregator(new AggFunction());
    }

    /**
     * Tests {@link ChannelTimeSeriesAggregator}.
     */
    @Test
    public void testLambdaAggregator() {
        testAggregator(new LambdaBasedAggregationFunction<PlcEnergyMeasurement, PlcEnergyMeasurement, 
            AggregatedPlcEnergyMeasurement, Double, Date>(d -> d.timestamp, d -> d.channel, d -> getValue(d), 
                (n, t) -> n >= 3, s -> new AggFunction().createResult(s)));
    }
    
    /**
     * Tests {@link ChannelTimeSeriesAggregator}.
     * 
     * @param aggFunc the aggregator function to test
     */
    private void testAggregator(AggregationFunction<PlcEnergyMeasurement, PlcEnergyMeasurement, 
        AggregatedPlcEnergyMeasurement, Double, Date> aggFunc) {
        Date now = Calendar.getInstance().getTime();
        PlcEnergyMeasurement[] data = new PlcEnergyMeasurement[3];
        data[0] = new PlcEnergyMeasurement();
        data[0].timestamp = now;
        data[0].channel = "ch1";
        data[0].value = "1";
        data[1] = new PlcEnergyMeasurement();
        data[1].timestamp = now;
        data[1].channel = "ch2";
        data[1].value = "2";
        data[2] = new PlcEnergyMeasurement();
        data[2].timestamp = now;
        data[2].channel = "ch3";
        data[2].value = "3";

        ChannelTimeSeriesAggregator<PlcEnergyMeasurement, AggregatedPlcEnergyMeasurement, Double, Date> agg 
            = new ChannelTimeSeriesAggregator<PlcEnergyMeasurement, AggregatedPlcEnergyMeasurement, Double, Date>(
                aggFunc);
        agg.stopAggregating();
        agg.startAggregating();
        
        AggregatedPlcEnergyMeasurement res = null;
        for (int s = 1; s <= 5; s++) {
            for (int d = 0; d < data.length; d++) {
                AggregatedPlcEnergyMeasurement tmp = agg.process(data[d]);
                if (null != tmp && null == res) {
                    res = tmp;
                }
            }
        }
        agg.stopAggregating();

        Assert.assertNotNull(res);
        Assert.assertNotNull(res.channels);
        Assert.assertEquals(3, res.channels.length);
        Map<String, double[]> collectedTestData = new HashMap<>();
        for (int c = 0; c < res.channels.length; c++) {
            PlcEnergyMeasurementChannel channel = res.channels[c];
            Assert.assertNotNull(channel.channel);
            Assert.assertNotNull(channel.values);
            Assert.assertEquals(3, channel.values.length);
            Assert.assertTrue(channel.samplerate > 0);
            Assert.assertNotNull(channel.timestamp);
            collectedTestData.put(channel.channel, channel.values);
        }
        Assert.assertEquals(3, collectedTestData.size());
        for (Map.Entry<String, double[]> e : collectedTestData.entrySet()) {
            String channel = e.getKey();
            for (int i = 0; i < e.getValue().length; i++) {
                Assert.assertEquals(channel, "ch" + ((int) e.getValue()[i]));
            }
        }
    }
    
    /**
     * Tests {@link MultiChannelTimeSeriesAggregator}.
     */
    @Test
    public void testMultiAggregation() {
        MultiPlcEnergyMeasurement data = new MultiPlcEnergyMeasurement();
        data.datapoints.timestamp = Calendar.getInstance().getTime();
        data.datapoints.datapoints = new MultiPlcEnergyMeasurementDatapoint[3];
        data.datapoints.datapoints[0] = new MultiPlcEnergyMeasurementDatapoint();
        data.datapoints.datapoints[0].channel = "ch1";
        data.datapoints.datapoints[0].value = "1";
        data.datapoints.datapoints[1] = new MultiPlcEnergyMeasurementDatapoint();
        data.datapoints.datapoints[1].channel = "ch2";
        data.datapoints.datapoints[1].value = "2";
        data.datapoints.datapoints[2] = new MultiPlcEnergyMeasurementDatapoint();
        data.datapoints.datapoints[2].channel = "ch3";
        data.datapoints.datapoints[2].value = "3";

        LambdaBasedAggregationFunction<MultiPlcEnergyMeasurement, MultiPlcEnergyMeasurementDatapoint, 
            AggregatedPlcEnergyMeasurement, Double, Date> aggFunc = new LambdaBasedAggregationFunction<
            MultiPlcEnergyMeasurement, MultiPlcEnergyMeasurementDatapoint, AggregatedPlcEnergyMeasurement, Double, 
                Date>(d -> d.datapoints.timestamp, d -> d.channel, d -> getValue(d),
                    (n, t) -> n >= 3, s -> new AggFunction().createResult(s));
        
        MultiChannelTimeSeriesAggregator<MultiPlcEnergyMeasurement, MultiPlcEnergyMeasurementDatapoint, 
            AggregatedPlcEnergyMeasurement, Double, Date> agg = new MultiChannelTimeSeriesAggregator<
                MultiPlcEnergyMeasurement, MultiPlcEnergyMeasurementDatapoint, AggregatedPlcEnergyMeasurement, Double, 
                    Date>(
            true, aggFunc, d -> new ArrayIterator<>(d.datapoints.datapoints));

        agg.stopAggregating();
        agg.startAggregating();

        agg.process(data);
        agg.process(data);
        agg.process(data);
        AggregatedPlcEnergyMeasurement res = agg.process(data);
        agg.stopAggregating();

        Assert.assertNotNull(res);
        Assert.assertNotNull(res.channels);
        Assert.assertEquals(3, res.channels.length);
        Map<String, double[]> collectedTestData = new HashMap<>();
        for (int c = 0; c < res.channels.length; c++) {
            PlcEnergyMeasurementChannel channel = res.channels[c];
            Assert.assertNotNull(channel.channel);
            Assert.assertNotNull(channel.values);
            Assert.assertEquals(3, channel.values.length);
            Assert.assertTrue(channel.samplerate > 0);
            Assert.assertNotNull(channel.timestamp);
            collectedTestData.put(channel.channel, channel.values);
        }
        Assert.assertEquals(3, collectedTestData.size());
        for (Map.Entry<String, double[]> e : collectedTestData.entrySet()) {
            String channel = e.getKey();
            for (int i = 0; i < e.getValue().length; i++) {
                Assert.assertEquals(channel, "ch" + ((int) e.getValue()[i]));
            }
        }
    }

    /**
     * Tests {@link ChannelTimeSeriesAggregator} with failing constructor.
     */
    @Test
    public void testAggregator_initFail() {
        try {
            new ChannelTimeSeriesAggregator<Object, Object, Object, Object>(null);
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // this is fine
        }
    }
    
    /**
     * Tests {@link SingleValueIterator}.
     */
    @Test
    public void testSingleValueIterator() {
        Iterator<Integer> iter = new SingleValueIterator<Integer>(1);
        Assert.assertTrue(iter.hasNext());
        Assert.assertTrue(1 == iter.next());
        Assert.assertFalse(iter.hasNext());
        try {
            iter.next();
            Assert.fail("No exception");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * Tests {@link SingleValueIterator}.
     */
    @Test
    public void testArrayIterator() {
        Iterator<Integer> iter = new ArrayIterator<Integer>(new Integer[] {});
        Assert.assertFalse(iter.hasNext());

        Integer[] array = new Integer[] {1, 2};
        iter = new ArrayIterator<Integer>(array);
        Assert.assertTrue(iter.hasNext());
        Assert.assertTrue(1 == iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertTrue(2 == iter.next());
        Assert.assertFalse(iter.hasNext());
        try {
            iter.next();
            Assert.fail("No exception");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

}
