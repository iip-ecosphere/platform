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

package de.iip_ecosphere.platform.examples.hm23.mock;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.iip_ecosphere.platform.examples.hm23.Commands;
import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.IipStringStyle;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter.ConverterInstances;
import de.iip_ecosphere.platform.services.environment.testing.DataRecorder;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProvider;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import iip.datatypes.BeckhoffOutput;
import iip.datatypes.Command;
import iip.datatypes.DecisionResult;
import iip.datatypes.DriveBeckhoffOutput;
import iip.datatypes.MdzhOutput;
import iip.datatypes.PlcEnergyMeasurementJson;
import iip.datatypes.PlcOutput;

/**
 * AAS trace based sink.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AppAas extends de.iip_ecosphere.platform.examples.hm23.AppAas {

    private static final ToStringStyle TRACE_OUT_STYLE = IipStringStyle.SHORT_STRING_STYLE; // MULTI_LINE_STYLE
    private static final boolean DO_EVENTS = Boolean.valueOf(System.getProperty("iip.app.hm23.mock.doEvents", "true"));
    private static final boolean DO_TRANSPORT_OUT = Boolean.valueOf(
        System.getProperty("iip.app.hm23.mock.doTransportOut", "true"));
    private static final boolean LOG_ALL = Boolean.valueOf(System.getProperty("iip.app.hm23.mock.logAll", "false"));
    private Timer timer;
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public AppAas(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates a service instance. [for testing]
     *
     * @param app static information about the application
     * @param yaml the service description 
     */
    public AppAas(ApplicationSetup app, YamlService yaml) {
        super(app, yaml);
    }

    @Override
    public void processDecisionResult(DecisionResult data) {
        super.processDecisionResult(data);
        System.out.println("APP: " + JsonUtils.toJson(data));
        recordData("data", data);
    }

    @Override
    public void attachCommandIngestor(DataIngestor<Command> ingestor) {
        super.attachCommandIngestor(ingestor);
        
        if (null == timer && null != ingestor) {
            long period = 60000;
            try {
                period = Long.parseLong(System.getProperty("iip.app.hm23.mock.cmdPeriod", "0")); // 120000
            } catch (NumberFormatException e) {
                System.out.println("Cannot set period: " + e.getMessage());
            }
            if (period > 0) {
                timer = new Timer();
                System.out.println("ActionDecider: Starting timer for source picture commands " 
                    + period + " " + DO_EVENTS);
                // let the source send images in regular pace
                timer.schedule(new UserTimerTask(), 40 * 1000, period); // startup takes ~35s
            } else {
                System.out.println("ActionDecider: Skipping timer, waiting for start commands " + DO_EVENTS);
            }
        }
    }
    
    /**
     * A timer task emulating an user.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class UserTimerTask extends TimerTask {

        private int count = 0;
        
        @Override
        public void run() {
            Commands cmd;
            String arg;
            if (count == 0) {
                cmd = Commands.REQUEST_START_QUALITY_DETECTION;
                arg = null;
                //count++; // stay on 0, no feedback, no switch
            } else if (count == 1) {
                cmd = Commands.SEND_FEEDBACK_TO_AI;
                arg = "ok";
                //count++; // stay on 0, no feedback, no switch
            } else {
                cmd = Commands.REQUEST_DRIVE;
                arg = null;
                count = 0;
            }
            System.out.println("AppAAS: Sending " + cmd);
            sendCommand(cmd, arg);
        }
        
    }
    
    @Override
    protected Predicate<TraceRecord> getHandleNewFilter() {
        final Predicate<TraceRecord> parentFilter = super.getHandleNewFilter();
        return data -> {
            boolean result = false;
            boolean log = true;
            if (!LOG_ALL) {
                Object payload = data.getPayload();
                if (payload instanceof PlcOutput) { // filter out commands
                    log = false;
                } else if (payload instanceof MdzhOutput) { 
                    log = false;
                } else if (payload instanceof BeckhoffOutput) { 
                    log = false;
                } else if (payload instanceof DriveBeckhoffOutput) {
                    log = false;
                } else if (payload instanceof PlcEnergyMeasurementJson) {
                    log = false;
                }
            }
            if (log) {
                System.out.println("APP Trace RCV: " + ReflectionToStringBuilder.toString(data, TRACE_OUT_STYLE) 
                    + " doEvents: " + DO_EVENTS + " logAll: " + LOG_ALL);
            }
            if (DO_EVENTS) { // decision about passing to AAS happens in parent
                result = parentFilter.test(data);
            }
            return result;
        };
    }
    
    @Override
    protected TransportConnector createTransport(BasicSerializerProvider serializationProvider) {
        TransportConnector result;
        if (DO_TRANSPORT_OUT) {
            result = super.createTransport(serializationProvider);
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    protected ConverterInstances<TraceRecord> createConverter() {
        ConverterInstances<TraceRecord> result = super.createConverter();
        TransportConverter<TraceRecord> conv = result.getConverter();
        if (!DO_EVENTS) {
            conv.setCleanupTimeout(-1);
        }
        return result;
    }
    
    @Override
    protected DataRecorder createDataRecorder() {
        return createDataRecorderOrig();
    }

}
