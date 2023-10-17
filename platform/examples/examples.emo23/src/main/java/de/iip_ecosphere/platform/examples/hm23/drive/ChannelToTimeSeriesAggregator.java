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

package de.iip_ecosphere.platform.examples.hm23.drive;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.kiServices.functions.aggregation.MultiChannelTimeSeriesAggregator;
import de.iip_ecosphere.platform.kiServices.functions.aggregation.MultiChannelTimeSeriesAggregator.*;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.AggregatedPlcEnergyMeasurement;
import iip.datatypes.AggregatedPlcEnergyMeasurementImpl;
import iip.datatypes.DriveCommand;
import iip.datatypes.PlcEnergyMeasurement;
import iip.datatypes.PlcEnergyMeasurementChannel;
import iip.datatypes.PlcEnergyMeasurementChannelImpl;
import iip.datatypes.PlcEnergyMeasurementJson;
import iip.datatypes.PlcEnergyMeasurementListOfDataPointJson;
import iip.impl.ChannelToTimeSeriesAggregatorImpl;

/**
 * Application specific aggregator for energy data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelToTimeSeriesAggregator extends ChannelToTimeSeriesAggregatorImpl {

    private MultiChannelTimeSeriesAggregator<PlcEnergyMeasurementJson, PlcEnergyMeasurementListOfDataPointJson, 
        AggregatedPlcEnergyMeasurement, Double, Date> aggregator;

    /**
     * Creates a result instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class AggResultBuilder implements ResultBuilder<AggregatedPlcEnergyMeasurement, Double, Date> {

        private PlcEnergyMeasurementChannel[] channels;
        private int pos;
        
        /**
         * Creates a result builder.
         * 
         * @param channelCount the number of channels
         */
        private AggResultBuilder(int channelCount) {
            channels = new PlcEnergyMeasurementChannel[channelCount];
        }

        @Override
        public void addData(String category, List<Double> data, Date timestamp) {
            PlcEnergyMeasurementChannel ch = new PlcEnergyMeasurementChannelImpl();
            ch.setChannel(category);
            ch.setSamplerate(getParameterSampleRate() / 1000.0);
            ch.setTimestamp(timestamp);
            double[] valsArray = new double[data.size()];
            for (int v = 0, size = data.size(); v < size; v++) {
                valsArray[v] = data.get(v);
            }
            ch.setValues(valsArray);
            channels[pos++] = ch;
        }

        @Override
        public AggregatedPlcEnergyMeasurement build() {
            AggregatedPlcEnergyMeasurement result = new AggregatedPlcEnergyMeasurementImpl();
            result.setChannels(channels);
            return result;
        }
        
    }
    
    /**
     * Fallback constructor, also used for testing main program.
     */
    public ChannelToTimeSeriesAggregator() {
        super(ServiceKind.TRANSFORMATION_SERVICE);
        initializeAggregator();
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public ChannelToTimeSeriesAggregator(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
        initializeAggregator();
    }
    
    /**
     * Initializes the aggregator.
     */
    private void initializeAggregator() {
        aggregator = new MultiChannelTimeSeriesAggregator<PlcEnergyMeasurementJson, 
            PlcEnergyMeasurementListOfDataPointJson, AggregatedPlcEnergyMeasurement, Double, Date>(initIsAggregating(), 
                new LambdaBasedAggregationFunction<PlcEnergyMeasurementJson, PlcEnergyMeasurementListOfDataPointJson, 
                AggregatedPlcEnergyMeasurement, Double, Date>(d -> d.getDataPoints().getTimestamp(), 
                    d -> d.getDatapoint(), d -> getValue(d), (n, t) -> n >= getParameterSamples(), 
                    c -> new AggResultBuilder(c)),
                    d -> new ArrayIterator<>(d.getDataPoints().getListOfDataPoints()));
    }
    
    /**
     * Converts the {@link PlcEnergyMeasurement#getValue()} from {@code input}.
     * 
     * @param input the input instance
     * @return the converted value
     */
    private static Double getValue(PlcEnergyMeasurementListOfDataPointJson input) {
        Double result;
        try {
            result = Double.valueOf(input.getValue());
        } catch (NumberFormatException ex) {
            result = -1.0;
            LoggerFactory.getLogger(ChannelToTimeSeriesAggregator.class).info(
                "Cannot add received value: {}", ex.getMessage());
        }
        return result;

    }
    
    @Override
    public void processPlcEnergyMeasurementJson(PlcEnergyMeasurementJson data) {
        AggregatedPlcEnergyMeasurement result = aggregator.process(data);
        if (null != result) {
            ingestAggregatedPlcEnergyMeasurement(result);
        }
    }
    
    /**
     * Returns the initial value whether the aggregator is currently aggregating data.
     * 
     * @return {@code true} for aggregating, {@code false} else
     */
    protected boolean initIsAggregating() {
        return false;
    }

    @Override
    public void processDriveCommand(DriveCommand data) {
        if (data.getBExecute()) {
            aggregator.startAggregating();
        } else {
            aggregator.stopAggregating();
        }
    }

}
