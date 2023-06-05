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

package de.iip_ecosphere.platform.examples.hm22.mock;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.iip_ecosphere.platform.examples.hm22.Commands;
import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.IipStringStyle;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter.ConverterInstances;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import iip.datatypes.Command;
import iip.datatypes.DecisionResult;
import iip.datatypes.MdzhOutput;
import iip.datatypes.PlcOutput;

/**
 * AAS trace based sink.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AppAas extends de.iip_ecosphere.platform.examples.hm22.AppAas {

    private static final ToStringStyle TRACE_OUT_STYLE = IipStringStyle.SHORT_STRING_STYLE; // MULTI_LINE_STYLE
    private static final boolean DO_EVENTS = Boolean.valueOf(System.getProperty("iip.app.hm22.mock.doEvents", "true"));
    private static final boolean LOG_ALL = Boolean.valueOf(System.getProperty("iip.app.hm22.mock.logAll", "false"));
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
        System.out.println("APP: " + data);
    }

    @Override
    public void attachCommandIngestor(DataIngestor<Command> ingestor) {
        super.attachCommandIngestor(ingestor);
        
        if (null == timer && null != ingestor) {
            long period = 60000;
            try {
                period = Long.parseLong(System.getProperty("iip.app.hm22.mock.cmdPeriod", "0")); // 120000
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
                cmd = Commands.REQUEST_START;
                arg = null;
                //count++; // stay on 0, no feedback, no switch
            } else if (count == 1) {
                cmd = Commands.SEND_FEEDBACK_TO_AI;
                arg = "ok";
                //count++; // stay on 0, no feedback, no switch
            } else {
                cmd = Commands.SWITCH_AI;
                arg = null;
                count = 0;
            }
            System.out.println("AppAAS: Sending " + cmd);
            sendCommand(cmd, arg);
        }
        
    }

    /**
     * Refined converter.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ConfiguredMockingConverter extends ConfiguredConverter {

        @Override
        protected void handleNew(TraceRecord data) {
            // do not write to AAS, i.e., no super call
            boolean log = true;
            if (!LOG_ALL) {
                if ((data.getPayload() instanceof PlcOutput)) { // filter out commands
                    log = false;
                }
                if ((data.getPayload() instanceof MdzhOutput)) { 
                    log = false;
                }
            }
            if (log) {
                System.out.println("APP Trace RCV: " + ReflectionToStringBuilder.toString(data, TRACE_OUT_STYLE) 
                    + " doEvents: " + DO_EVENTS + " logAll: " + LOG_ALL);
            }
            if (DO_EVENTS) {
                super.handleNew(data);
            }
        }
    
        @Override
        public boolean cleanup(Aas aas) {
            boolean done = false;
            if (DO_EVENTS) {
                done = super.cleanup(aas);
            }
            return done;
        }
        
    }
    
    @Override
    protected ConverterInstances<TraceRecord> createConverter() {
        return new ConverterInstances<TraceRecord>(new ConfiguredMockingConverter());
    }

}
