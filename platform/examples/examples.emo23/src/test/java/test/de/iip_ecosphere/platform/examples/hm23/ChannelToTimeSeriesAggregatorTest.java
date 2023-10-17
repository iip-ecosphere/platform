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

package test.de.iip_ecosphere.platform.examples.hm23;

import java.util.Calendar;
//import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.examples.hm23.drive.mock.ChannelToTimeSeriesAggregator;
import iip.datatypes.AggregatedPlcEnergyMeasurement;
//import iip.datatypes.PlcEnergyMeasurement;
import iip.datatypes.PlcEnergyMeasurementChannel;
import iip.datatypes.PlcEnergyMeasurementDatapointsJson;
import iip.datatypes.PlcEnergyMeasurementDatapointsJsonImpl;
//import iip.datatypes.PlcEnergyMeasurementImpl;
import iip.datatypes.PlcEnergyMeasurementJson;
import iip.datatypes.PlcEnergyMeasurementJsonImpl;
import iip.datatypes.PlcEnergyMeasurementListOfDataPointJson;
import iip.datatypes.PlcEnergyMeasurementListOfDataPointJsonImpl;

/**
 * Tests the channel aggregator.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelToTimeSeriesAggregatorTest {

    private AggregatedPlcEnergyMeasurement res = null;

    /**
     * Tests the channel aggregator.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testAggregation() throws ExecutionException {
        /*Date now = Calendar.getInstance().getTime();
        PlcEnergyMeasurement[] data = new PlcEnergyMeasurement[3];
        data[0] = new PlcEnergyMeasurementImpl();
        data[0].setTimestamp(now);
        data[0].setChannel("ch1");
        data[0].setValue("1");
        data[1] = new PlcEnergyMeasurementImpl();
        data[1].setTimestamp(now);
        data[1].setChannel("ch2");
        data[1].setValue("2");
        data[2] = new PlcEnergyMeasurementImpl();
        data[2].setTimestamp(now);
        data[2].setChannel("ch3");
        data[2].setValue("3");*/
        
        PlcEnergyMeasurementJson data = new PlcEnergyMeasurementJsonImpl();
        PlcEnergyMeasurementDatapointsJson dPoints = new PlcEnergyMeasurementDatapointsJsonImpl();
        data.setDataPoints(dPoints);
        dPoints.setDevice("EM");
        dPoints.setTimestamp(Calendar.getInstance().getTime());
        PlcEnergyMeasurementListOfDataPointJson[] dPointsList = new PlcEnergyMeasurementListOfDataPointJson[3];
        dPoints.setListOfDataPoints(dPointsList);
        dPointsList[0] = new PlcEnergyMeasurementListOfDataPointJsonImpl();
        dPointsList[0].setDataType("REAL");
        dPointsList[0].setUnit("V");
        dPointsList[0].setDatapoint("ch1");
        dPointsList[0].setValue("1");
        dPointsList[1] = new PlcEnergyMeasurementListOfDataPointJsonImpl();
        dPointsList[0].setDataType("REAL");
        dPointsList[0].setUnit("V");
        dPointsList[1].setDatapoint("ch2");
        dPointsList[1].setValue("2");
        dPointsList[2] = new PlcEnergyMeasurementListOfDataPointJsonImpl();
        dPointsList[0].setDataType("REAL");
        dPointsList[0].setUnit("V");
        dPointsList[2].setDatapoint("ch3");
        dPointsList[2].setValue("3");

        ChannelToTimeSeriesAggregator agg = new ChannelToTimeSeriesAggregator();
        agg.setParameterSamples(3);
        agg.attachAggregatedPlcEnergyMeasurementIngestor(d -> res = d);
        for (int s = 1; s <= 5; s++) {
            /*for (int d = 0; d < data.length; d++) {
                //agg.processPlcEnergyMeasurement(data[d]);
            }*/
            agg.processPlcEnergyMeasurementJson(data);
        }

        Assert.assertNotNull(res);
        PlcEnergyMeasurementChannel[] channels = res.getChannels();
        Assert.assertNotNull(channels);
        Assert.assertEquals(3, channels.length);
        Map<String, double[]> collectedTestData = new HashMap<>();
        for (int c = 0; c < channels.length; c++) {
            PlcEnergyMeasurementChannel channel = channels[c];
            Assert.assertNotNull(channel.getChannel());
            Assert.assertNotNull(channel.getValues());
            Assert.assertEquals(3, channel.getValues().length);
            Assert.assertTrue(channel.getSamplerate() > 0);
            Assert.assertNotNull(channel.getTimestamp());
            collectedTestData.put(channel.getChannel(), channel.getValues());
        }
        Assert.assertEquals(3,  collectedTestData.size());
        for (Map.Entry<String, double[]> e : collectedTestData.entrySet()) {
            String channel = e.getKey();
            for (int i = 0; i < e.getValue().length; i++) {
                Assert.assertEquals(channel, "ch" + ((int) e.getValue()[i]));
            }
        }
    }

}
