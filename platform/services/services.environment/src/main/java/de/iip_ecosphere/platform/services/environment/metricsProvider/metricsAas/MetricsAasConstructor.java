/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.Irdi;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

import static de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants.*;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;

import org.slf4j.LoggerFactory;

/**
 * Class that provides an AAS the infrastructure to access the metrics
 * provider.<br>
 * This class includes the functionality that adds the metrics exposed by the
 * Metrics Provider as properties of an AAS submodel, as well as providing the
 * implementation required for those properties to correctly retrieve the
 * values<br>
 * If we wish to add any custom meters to our AAS, we can also use this class as
 * a class to do so, ensuring that all metrics (custom or not) are accessed in
 * the same way.
 * 
 * The original approach aimed at attaching metric results through 
 * {@link InvocablesCreator functor objects} to AAS properties. While this basically works,
 * it fails when AAS are deployed remotely as the values of all properties are read out 
 * for AAS serialization. As the BaSyx VAB connector is meant to be stateless,
 * it re-creates network connections per each request. Also caching the connectors did not 
 * solve the problem. Ultimately, in parallel access cases the AAS even blocked the 
 * entire operations of the program. Thus, we turned the approach around and rely now on
 * attached local functors that access a {@link JsonObjectHolder shared object}. The shared object is 
 * attached through a {@link MetricsReceptionCallback transport layer callback} to a transport layer 
 * connector, which is cached/created on demand upon execution of the functors. The functors are serializable, 
 * and carry all information required to create a transport connector. Updates to the
 * metric values happen in background to the {@link JsonObjectHolder shared object}, while the AAS just
 * accesses the values in its own pace (returning nothing if no metrics data was received so far). Shared 
 * objects shall be {@link #clear() released} when the program shuts down.
 * 
 * @author Miguel Gomez
 * @author Holger Eichelberger, SSE
 */
public class MetricsAasConstructor {

    private static Map<String, TransportConnector> conns = new HashMap<>();
    private static Map<String, JsonObjectHolder> holders = new HashMap<>();
    private static final boolean METRICS_AS_VALUES = true;
    
    /**
     * Clears temporary data structures.
     */
    public static void clear() {
        for (Map.Entry<String, TransportConnector> ent : conns.entrySet()) {
            try {
                ent.getValue().disconnect();
            } catch (IOException e) {
                LoggerFactory.getLogger(MetricsAasConstructor.class).error(
                    "Cannot disconnect transport connector for id " + ent.getKey() + ": " + e.getMessage());
            }
        }
        conns.clear();
        holders.clear();
    }

    /**
     * Returns a transport connector for the given {@code channel} and {@code setup}.
     * 
     * @param channel the transport channel
     * @param setup the transport setup
     * @return the (cached) transport connector
     */
    private static TransportConnector getTransportConnector(String channel, TransportSetup setup) {
        TransportConnector conn = conns.get(channel);
        if (null == conn && !conns.containsKey(channel)) {
            conn = TransportFactory.createConnector();
            try {
                conn.connect(setup.createParameter());
                conn.setReceptionCallback(channel, new MetricsReceptionCallback());
            } catch (IOException e) {
                LoggerFactory.getLogger(MetricsAasConstructor.class).error(
                    "Cannot create connector: " + e.getMessage());
            }
            conns.put(channel, conn);
        }
        return conn;
    }

    /**
     * Holds a received JSON object.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class JsonObjectHolder {
        
        private JsonObject obj;

        /**
         * Returns a meter value.
         * 
         * @param name the name of the registered meter, see, e.g., {@link MetricsProvider}
         * @return the meter value or <b>null</b>
         */
        public String getMeter(String name) {
            String result = "";
            if (null != obj) {
                JsonObject meters = obj.getJsonObject("meters");
                if (null != meters) {
                    JsonObject meter = meters.getJsonObject(name);
                    if (null != meter) {
                        result = meter.toString();
                    }
                }
            }
            return result;
        }
    }
    
    // put information into map (metrics provider would be better)
    /**
     * Receives monitoring information via the transport layer.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MetricsReceptionCallback implements ReceptionCallback<String> {

        /**
         * Creates a callback.
         */
        public MetricsReceptionCallback() {
        }
        
        @Override
        public void received(String data) {
            try {
                JsonObject obj = Json.createReader(new StringReader(data)).readObject();
                String id = obj.getString("id");
                if (null != id) {
                    JsonObjectHolder holder = holders.get(id);
                    if (null != holder) {
                        holder.obj = obj;
                    }
                }
            } catch (JsonParsingException e) {
                LoggerFactory.getLogger(MetricsAasConstructor.class).error("Cannot parse JSON: " 
                    + e.getMessage() + " " + data);
            }
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

    }

    /**
     * Implements a meter getter based on {@link JsonObjectHolder}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MeterGetter implements Supplier<Object>, Serializable {

        private static final long serialVersionUID = 2294254606334816252L;
        private String name;
        private String id;
        private String channel;
        private TransportSetup setup;

        /**
         * Creates a meter getter instance.
         * 
         * @param channel the transport channel
         * @param id the id to react on
         * @param setup the transport setup
         * @param name the list name {@link MetricsProvider}
         */
        private MeterGetter(String channel, String id, TransportSetup setup, String name) {
            this.id = id;
            this.name = name;
            this.channel = channel;
            this.setup = setup;
        }
        
        @Override
        public Object get() {
            String json = getHolder(id, channel, setup).getMeter(name);
            return METRICS_AS_VALUES ? getMeasurement(json) : json;
        }

        /**
         * Extracts the measurement out of the metrics JSON.
         * 
         * @param json the JSON, may be empty or <b>null</b>
         * @return the measurement
         */
        private Object getMeasurement(String json) {
            Object result = null;
            if (null != json && json.length() > 0) {
                JsonObject obj = Json.createReader(new StringReader(json)).readObject();
                JsonArray meas = obj.getJsonArray("measurements");
                if (null != meas && meas.size() > 0) {
                    JsonObject measurement = meas.getJsonObject(0);
                    if (null != measurement) {
                        JsonNumber num = measurement.getJsonNumber("value");
                        if (null != num) {
                            result = num.doubleValue();
                        }
                    }
                }
            }
            return result;
        }
        
    }
    
    /**
     * Returns a JSON object holder associated to a transport connector through {@link MetricsReceptionCallback}.
     * 
     * @param channel the transport channel
     * @param id the id to react on
     * @param setup the transport setup
     * @return the (shared) object holder instance
     */
    private static JsonObjectHolder getHolder(String id, String channel, TransportSetup setup) {
        getTransportConnector(channel, setup);
        JsonObjectHolder result = holders.get(id);
        if (null == result) {
            result = new JsonObjectHolder();
            holders.put(id, result);
        }
        return result;
    }
    
    /**
     * Tests whether metrics properties do exist on {@code sub}.
     * 
     * @param sub the submodel elements collection to test
     * @return {@code true} for metrics, {@code false} else
     */
    public static boolean containsMetrics(SubmodelElementCollection sub) {
        return sub.getElement(SYSTEM_DISK_FREE) != null;
    }
    
    /**
     * Returns the semantic id to use depending on {@link #METRICS_AS_VALUES}.
     * 
     * @param semId the id for {@link #METRICS_AS_VALUES}
     * @return the {@code semId} or <b>null</b>
     */
    private static String semId(String semId) {
        return METRICS_AS_VALUES ? semId : null;
    }

    /**
     * Returns the property type to use depending on {@link #METRICS_AS_VALUES}.
     * 
     * @param mAsVType the type for {@link #METRICS_AS_VALUES}
     * @param other the type if {@link #METRICS_AS_VALUES} is disabled
     * @return {@code mAsVType} or {@code other}
     */
    private static Type propType(Type mAsVType, Type other) {
        return METRICS_AS_VALUES ? mAsVType : other;
    }
    
    /**
     * Adds metrics to the submodel/elements. Metric values are bound against a transport connector receiver.
     * 
     * @param smBuilder submodel/elements builder of the AAS
     * @param filter    metrics filter, may be <b>null</b> for all (currently ignored)
     * @param channel   the transport channel to listen to
     * @param id        the metrics provider id to listen to
     * @param setup     the transport setup
     */
    public static void addProviderMetricsToAasSubmodel(SubmodelElementContainerBuilder smBuilder, 
        Predicate<String> filter, String channel, String id, TransportSetup setup) {

        /* System Disk Capacity metrics, string as JSON meter is transferred  */
        smBuilder.createPropertyBuilder(SYSTEM_DISK_FREE).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_FREE), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_TOTAL).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_TOTAL), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USABLE).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_USABLE), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USED).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_USED), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();

        /* System Physical Memory metrics, string as JSON meter is transferred  */
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_FREE).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_FREE), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_TOTAL).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_TOTAL), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USAGE).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_USAGE), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_PERCENT))
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USED).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_USED), InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_BYTE))
            .build();
        
        smBuilder.createPropertyBuilder(DEVICE_CPU_TEMPERATURE).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.DEVICE_CPU_TEMPERATURE), 
                InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_DEGREE_CELSIUS))
            .build();
        smBuilder.createPropertyBuilder(DEVICE_CASE_TEMPERATURE).setType(propType(Type.DOUBLE, Type.STRING))
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.DEVICE_CPU_TEMPERATURE), 
                InvocablesCreator.READ_ONLY)
            .setSemanticId(semId(Irdi.AAS_IRDI_UNIT_DEGREE_CELSIUS))
            .build();
    }

    /**
     * Removes provider metrics and all related elements from {@code sub}.
     * 
     * @param sub the submodel elements collection to remove the elements from
     */
    public static void removeProviderMetricsFromAasSubmodel(SubmodelElementCollection sub) {

        /* System Disk Capacity metrics */
        sub.deleteElement(SYSTEM_DISK_FREE);
        sub.deleteElement(SYSTEM_DISK_TOTAL);
        sub.deleteElement(SYSTEM_DISK_USABLE);
        sub.deleteElement(SYSTEM_DISK_USED);

        /* System Physical Memory metrics */
        sub.deleteElement(SYSTEM_MEMORY_FREE);
        sub.deleteElement(SYSTEM_MEMORY_TOTAL);
        sub.deleteElement(SYSTEM_MEMORY_USAGE);
        sub.deleteElement(SYSTEM_MEMORY_USED);
    }

}
