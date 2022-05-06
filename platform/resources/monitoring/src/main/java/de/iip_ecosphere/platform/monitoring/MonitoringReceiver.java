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

package de.iip_ecosphere.platform.monitoring;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.MeterRepresentation;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.status.ActionType;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.streams.StreamNames;
import io.micrometer.core.instrument.Meter;

/**
 * Observes IIP-Ecosphere standard transport channels and prepares the information for feeding it into
 * the monitoring system.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class MonitoringReceiver {
    
    private TransportConnector conn;
    private Map<String, Exporter> registry = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * A meter reception callback for receiving meter information from services or ECS runtime.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MeterReceptionCallback implements ReceptionCallback<String> {

        private String stream;
        
        /**
         * Creates a callback.
         * 
         * @param stream the stream name (for logging)
         */
        private MeterReceptionCallback(String stream) {
            this.stream = stream;
        }
        
        @Override
        public void received(String data) {
            try {
                try {
                    JsonObject obj = Json.createReader(new StringReader(data)).readObject();
                    String id = obj.getString("id");
                    notifyMeterReception(stream, id, obj);
                    // TODO preprocess id?
                    if (null != id) {
                        obtainExporter(id).addMeters(obj.getJsonObject("meters"));
                    }
                } catch (JsonParsingException e) {
                    LoggerFactory.getLogger(MonitoringReceiver.class).error("Cannot parse JSON: " 
                        + e.getMessage() + " " + data);
                }
            } catch (IllegalArgumentException e) {
                LoggerFactory.getLogger(MonitoringReceiver.class).warn(
                    "Cannot parse received meter data '{}' on {}: {}", data, stream, e.getMessage());
            }
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
        
    }
    
    /**
     * Receives status messages such as starting/stopping devices, containers, services.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class StatusReceptionCallback implements ReceptionCallback<StatusMessage> {

        @Override
        public void received(StatusMessage msg) {
            notifyStatusReceived(msg);
            String id = msg.getDeviceId();
            ActionType action = msg.getAction();
            if (ActionTypes.REMOVED == action) {
                Exporter exporter = registry.remove(id);
                notifyExporterRemoved(exporter);
                if (null != exporter) {
                    exporter.dispose();
                }
            } else {
                obtainExporter(id).validate();
            }
        }

        @Override
        public Class<StatusMessage> getType() {
            return StatusMessage.class;
        }
        
    }

    /**
     * Obtains an exporter for the given source id. Creates and registers a new one if required.
     * 
     * @param id the object id
     * @return the exporter
     */
    protected Exporter obtainExporter(String id) {
        Exporter exporter = registry.get(id);
        if (null == exporter) {
            exporter = createExporter(id);
            registry.put(id, exporter);
            notifyExporterAdded(exporter);
        }
        return exporter;
    }

    /**
     * Creates an exporter instance.
     * 
     * @param id the id of origin
     * @return the exporter instance
     */
    protected abstract Exporter createExporter(String id);

    /**
     * Is called when a meter information was received via transport. [testing, debugging]
     * 
     * @param stream the transport stream/channel
     * @param id the id
     * @param obj the received object
     */
    protected void notifyMeterReception(String stream, String id, JsonObject obj) {
    }

    /**
     * Is called when a status information was received via transport. [testing, debugging]
     * 
     * @param msg the status message
     */
    protected void notifyStatusReceived(StatusMessage msg) {
    }
    
    /**
     * Notifies about a added exporter.
     * 
     * @param exporter the exporter
     */
    protected void notifyExporterAdded(Exporter exporter) {
    }

    /**
     * Notifies about a removed exporter.
     * 
     * @param exporter the exporter
     */
    protected void notifyExporterRemoved(Exporter exporter) {
    }
    
    /**
     * An instance holding meters for update performing the export when needed, e.g., upon request or by timer.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected abstract class Exporter {
        
        private String id;
        private boolean valid;

        /**
         * Creates an exporter.
         * 
         * @param id the source id
         */
        protected Exporter(String id) {
            this.id = id;
        }
        
        /**
         * Adds a set of received meters.
         * 
         * @param mtrs the meters
         */
        protected void addMeters(JsonObject mtrs) {
            for (Map.Entry<String, JsonValue> e : mtrs.entrySet()) {
                addMeter(MeterRepresentation.parseMeter(e.getValue().toString()));
            }
        }

        /**
         * Adds a meter.
         * 
         * @param meter the meter
         */
        protected abstract void addMeter(Meter meter);
        
        /**
         * Returns the source id.
         * 
         * @return the source id
         */
        protected String getId() {
            return id;
        }

        /**
         * Returns whether the underlying object is considered valid.
         * 
         * @return {@code true} if validated through an event, {@code false} else
         */
        protected boolean isValid() {
            return valid;
        }

        /**
         * Disposes this exporter instance. It will be removed from the internal repository.
         */
        protected void dispose() {
            valid = false;
        }
        
        /**
         * Validates this exporter, i.e., marks it indicating that the underlying resource actually exists 
         * in the system.
         */
        protected void validate() {
            valid = true;
        }
        
    }
    
    /**
     * Starts the exporter.
     */
    public void start() {
        LoggerFactory.getLogger(MonitoringReceiver.class).info(
            "Connecting to IIP-Ecosphere transport");
        MonitoringSetup setup = MonitoringSetup.getInstance();
        TransportParameter tParams = setup.getTransport().createParameter();
        conn = TransportFactory.createConnector();
        try {
            conn.connect(tParams);
            conn.setReceptionCallback(StreamNames.STATUS_STREAM, 
                new StatusReceptionCallback());
            conn.setReceptionCallback(StreamNames.RESOURCE_METRICS, 
                new MeterReceptionCallback(StreamNames.RESOURCE_METRICS));
            conn.setReceptionCallback(StreamNames.SERVICE_METRICS, 
                new MeterReceptionCallback(StreamNames.SERVICE_METRICS));
        } catch (IOException e) {
            conn = null;
            LoggerFactory.getLogger(MonitoringReceiver.class).warn(
                "Cannot connect to IIP-Ecosphere transport: {} Central monitoring disabled", e.getMessage());
        }
    }
    
    // checkstyle: stop exception type check

    /**
     * Stops the exporter.
     */
    public void stop() {
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception e) {
                LoggerFactory.getLogger(MonitoringReceiver.class).warn(
                    "Disconnecting from IIP-Ecosphere transport: {}", e.getMessage());
            }
            conn = null;
        }
    }

    // checkstyle: resume exception type check

    /**
     * Returns the exporters.
     * 
     * @return the exporters
     */
    public Iterable<Exporter> exporters() {
        return registry.values();
    }

}
