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
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonArray;
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

    /**
     * Adds all the Metric Provider's meters as submodel properties and provides
     * implementation to retrieve them.<br>
     * These properties cannot be set, as they are extracted from the resource that
     * the Metrics Provider is monitoring. The getter function of these properties
     * will return a String that maps into a JSON value. Some of the meters will
     * return a simple JsonObject that can later be mapped into a Meter
     * Representation using the appropriate method. Those metrics that have multiple
     * values assigned to them that are mapped using different tags will return a
     * JsonArray composed of JsonObjects. Each of this JsonObjects will have two
     * attributes: first one being the Tags used to retrieve that particular meter,
     * saved as a JsonArray; and a second attribute containing the Meter as a
     * JsonObject.<br>
     * The properties that return this type of JsonArray are:
     * <ul>
     * <li>JVM buffer count</li>
     * <li>JVM buffer memory used</li>
     * <li>JVM buffer total capacity</li>
     * <li>JVM garbage collector pause timer</li>
     * <li>Committed JVM memory</li>
     * <li>Maximum JVM memory</li>
     * <li>Used JVM memory</li>
     * <li>Threads states</li>
     * <li>Logback events</li>
     * </ul>
     * 
     * @param bundle the bundle containing the required elements to add the
     *               properties and their implementation
     * @throws IllegalArgumentException if the bundle is null
     */
    public static void addMetricsToBundle(MetricsAasConstructionBundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("The bundle is null!");
        }
        addMetricsToAasSubmodel(bundle.getSubmodelBuilder(), bundle.getInvocablesCreator(), bundle.getFilter(), 
            bundle.getNameMapper());
        addMetricsProtocols(bundle.getProtocolBuilder(), bundle.getClient(), bundle.getFilter(), 
            bundle.getNameMapper());
    }

    /**
     * Adds metrics to the submodel/elements that are also covered by direct provider access in 
     * {@link #addMetricsProtocols(ProtocolServerBuilder, MetricsProvider, Predicate, Function)}.
     * 
     * @param smBuilder submodel/elements builder of the AAS
     * @param iCreator  invocables creator of the AAS
     * @param filter    metrics filter, may be <b>null</b> for all (currently ignored)
     * @param nameMapper the implementation name mapper
     */
    public static void addProviderMetricsToAasSubmodel(SubmodelElementContainerBuilder smBuilder, 
        InvocablesCreator iCreator, Predicate<String> filter, Function<String, String> nameMapper) {

        /* Meter lists */
        smBuilder.createPropertyBuilder(GAUGE_LIST).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(GAUGE_LIST)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(COUNTER_LIST).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(COUNTER_LIST)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(TIMER_LIST).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(TIMER_LIST)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(TAGGED_METER_LIST).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(TAGGED_METER_LIST)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SIMPLE_METER_LIST).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SIMPLE_METER_LIST)), InvocablesCreator.READ_ONLY).build();

        /* System Disk Capacity metrics, string as JSON meter is transferred  */
        smBuilder.createPropertyBuilder(SYSTEM_DISK_FREE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_DISK_FREE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_TOTAL).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_DISK_TOTAL)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USABLE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_DISK_USABLE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_DISK_USED)), InvocablesCreator.READ_ONLY).build();

        /* System Physical Memory metrics, string as JSON meter is transferred  */
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_FREE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_MEMORY_FREE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_TOTAL).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_MEMORY_TOTAL)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USAGE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_MEMORY_USAGE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_MEMORY_USED)), InvocablesCreator.READ_ONLY).build();
    }
    
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
         * Returns a list value.
         * 
         * @param name the name of the list, see {@link MetricsProvider}
         * @return the list value or <b>null</b>
         */
        public String getList(String name) {
            String result = "";
            if (null != obj) {
                JsonArray array = obj.getJsonArray(name);
                if (null != array) {
                    result = array.toString();
                }
            }
            return result;
        }

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
     * Implements a list getter based on {@link JsonObjectHolder}.
     * 
     * @author Holger Eichelberger, SSE
     */
    @SuppressWarnings("unused")
    private static class ListGetter implements Supplier<Object>, Serializable {

        private static final long serialVersionUID = -4387599926801687791L;
        private String name;
        private String id;
        private String channel;
        private TransportSetup setup;

        /**
         * Creates a list getter instance.
         * 
         * @param channel the transport channel
         * @param id the id to react on
         * @param setup the transport setup
         * @param name the list name {@link MetricsProvider}
         */
        private ListGetter(String channel, String id, TransportSetup setup, String name) {
            this.id = id;
            this.name = name;
            this.channel = channel;
            this.setup = setup;
        }
        
        @Override
        public Object get() {
            return getHolder(id, channel, setup).getList(name);
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
            return getHolder(id, channel, setup).getMeter(name);
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
        
        /* Meter lists */
        /*smBuilder.createPropertyBuilder(GAUGE_LIST).setType(Type.STRING)
            .bind(new ListGetter(channel, id, setup, MetricsProvider.GAUGE_LIST), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(COUNTER_LIST).setType(Type.STRING)
            .bind(new ListGetter(channel, id, setup, MetricsProvider.COUNTER_LIST), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(TIMER_LIST).setType(Type.STRING)
            .bind(new ListGetter(channel, id, setup, MetricsProvider.TIMER_LIST), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(TAGGED_METER_LIST).setType(Type.STRING)
            .bind(new ListGetter(channel, id, setup, MetricsProvider.TAGGED_METER_LIST), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SIMPLE_METER_LIST).setType(Type.STRING)
            .bind(new ListGetter(channel, id, setup, MetricsProvider.SIMPLE_METER_LIST), InvocablesCreator.READ_ONLY)
            .build();*/

        /* System Disk Capacity metrics, string as JSON meter is transferred  */
        smBuilder.createPropertyBuilder(SYSTEM_DISK_FREE).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_FREE), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_TOTAL).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_TOTAL), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USABLE).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_USABLE), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USED).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_DISK_USED), InvocablesCreator.READ_ONLY)
            .build();

        /* System Physical Memory metrics, string as JSON meter is transferred  */
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_FREE).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_FREE), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_TOTAL).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_TOTAL), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USAGE).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_USAGE), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USED).setType(Type.STRING)
            .bind(new MeterGetter(channel, id, setup, MetricsProvider.SYS_MEM_USED), InvocablesCreator.READ_ONLY)
            .build();
    }

    /**
     * Removes provider metrics and all related elements from {@code sub}.
     * 
     * @param sub the submodel elements collection to remove the elements from
     */
    public static void removeProviderMetricsFromAasSubmodel(SubmodelElementCollection sub) {
        /* Meter lists */
        sub.deleteElement(GAUGE_LIST);
        sub.deleteElement(COUNTER_LIST);
        sub.deleteElement(TIMER_LIST);
        sub.deleteElement(TAGGED_METER_LIST);
        sub.deleteElement(SIMPLE_METER_LIST);

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
    
    /**
     * Adds the properties representing the Metrics to the AAS submodel/elements.
     * 
     * @param smBuilder submodel/elements builder of the AAS
     * @param iCreator  invocables creator of the AAS
     * @param filter    metrics filter, may be <b>null</b> for all (currently ignored)
     * @param nameMapper the implementation name mapper
     */
    public static void addMetricsToAasSubmodel(SubmodelElementContainerBuilder smBuilder, InvocablesCreator iCreator, 
        Predicate<String> filter, Function<String, String> nameMapper) {

        /* Java Virtual Machine (JVM) Buffer Metrics */
        smBuilder.createPropertyBuilder(JVM_BUFFER_COUNT).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_BUFFER_COUNT)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_BUFFER_MEMORY_USED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_BUFFER_MEMORY_USED)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_BUFFER_TOTAL_CAPACITY).setType(Type.STRING).bind(
            iCreator.createGetter(nameMapper.apply(JVM_BUFFER_TOTAL_CAPACITY)), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Class Counter Metrics */
        smBuilder.createPropertyBuilder(JVM_CLASSES_LOADED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_CLASSES_LOADED)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_CLASSES_UNLOADED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_CLASSES_UNLOADED)), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Garbage Collector (GC) Metrics */
        smBuilder.createPropertyBuilder(JVM_GC_LIVE_DATA_SIZE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_GC_LIVE_DATA_SIZE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_MAX_DATA_SIZE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_GC_MAX_DATA_SIZE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_MEMORY_ALLOCATED).setType(Type.STRING).bind(
            iCreator.createGetter(nameMapper.apply(JVM_GC_MEMORY_ALLOCATED)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_MEMORY_PROMOTED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_GC_MEMORY_PROMOTED)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_PAUSE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_GC_PAUSE)), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Virtual Memory Metrics */
        smBuilder.createPropertyBuilder(JVM_MEMORY_COMMITTED).setType(Type.STRING).bind(
            iCreator.createGetter(nameMapper.apply(JVM_MEMORY_COMMITTED)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_MEMORY_MAX).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_MEMORY_MAX)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_MEMORY_USED).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_MEMORY_USED)), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Thread Metrics */
        smBuilder.createPropertyBuilder(JVM_THREADS_DAEMON).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_THREADS_DAEMON)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_THREADS_LIVE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_THREADS_LIVE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_THREADS_PEAK).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_THREADS_PEAK)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_THREADS_STATES).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(JVM_THREADS_STATES)), InvocablesCreator.READ_ONLY).build();

        /* Logback events */
        smBuilder.createPropertyBuilder(LOGBACK_EVENTS).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(LOGBACK_EVENTS)), InvocablesCreator.READ_ONLY).build();

        /* Process Metrics */
        smBuilder.createPropertyBuilder(PROCESS_CPU_USAGE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(PROCESS_CPU_USAGE)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(PROCESS_START_TIME).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(PROCESS_START_TIME)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(PROCESS_UPTIME).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(PROCESS_UPTIME)), InvocablesCreator.READ_ONLY).build();

        /* System CPU metrics */
        smBuilder.createPropertyBuilder(SYSTEM_CPU_COUNT).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_CPU_COUNT)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_CPU_USAGE).setType(Type.STRING)
            .bind(iCreator.createGetter(nameMapper.apply(SYSTEM_CPU_USAGE)), InvocablesCreator.READ_ONLY).build();

        addProviderMetricsToAasSubmodel(smBuilder, iCreator, filter, nameMapper);

        /* Configuration operations */
        smBuilder.createOperationBuilder(SET_MEMORY_BASE_UNIT).setInvocable(
            iCreator.createInvocable(nameMapper.apply(SET_MEMORY_BASE_UNIT)))
                .addInputVariable(BODY, Type.STRING).build();
        smBuilder.createOperationBuilder(SET_DISK_BASE_UNIT).setInvocable(
            iCreator.createInvocable(nameMapper.apply(SET_DISK_BASE_UNIT)))
                .addInputVariable(BODY, Type.STRING).build();
    }

    /**
     * Adds the property implementations to the protocol server builder via the metrics extractor REST client.
     * 
     * @param pBuilder protocol server builder
     * @param client   instance of the REST client
     * @param filter   metrics filter, may be <b>null</b> for all (currently ignored)
     * @param nameMapper the implementation name mapper
     */
    public static void addMetricsProtocols(ProtocolServerBuilder pBuilder, MetricsExtractorRestClient client, 
        Predicate<String> filter, Function<String, String> nameMapper) {
        /* Meter lists */
        pBuilder.defineProperty(nameMapper.apply(GAUGE_LIST), () -> client.getGaugeList(), null);
        pBuilder.defineProperty(nameMapper.apply(COUNTER_LIST), () -> client.getCounterList(), null);
        pBuilder.defineProperty(nameMapper.apply(TIMER_LIST), () -> client.getTimerList(), null);
        pBuilder.defineProperty(nameMapper.apply(TAGGED_METER_LIST), () -> client.getTaggedMeterList(), null);
        pBuilder.defineProperty(nameMapper.apply(SIMPLE_METER_LIST), () -> client.getSimpleMeterList(), null);

        /* Java Virtual Machine (JVM) Buffer Metrics */
        pBuilder.defineProperty(nameMapper.apply(JVM_BUFFER_COUNT), () -> client.getJvmBufferCount(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_BUFFER_MEMORY_USED), () -> client.getJvmBufferMemoryUsed(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_BUFFER_TOTAL_CAPACITY), 
            () -> client.getJvmBufferTotalCapacity(), null);

        /* Java Virtual Machine (JVM) Class Counter Metrics */
        pBuilder.defineProperty(nameMapper.apply(JVM_CLASSES_LOADED), () -> client.getJvmClassesLoaded(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_CLASSES_UNLOADED), () -> client.getJvmClassesUnloaded(), null);

        /* Java Virtual Machine (JVM) Garbage Collector (GC) Metrics */
        pBuilder.defineProperty(nameMapper.apply(JVM_GC_LIVE_DATA_SIZE), () -> client.getJvmGcLiveDataSize(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_GC_MAX_DATA_SIZE), () -> client.getJvmGcMaxDataSize(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_GC_MEMORY_ALLOCATED), 
            () -> client.getJvmGcMemoryAllocated(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_GC_MEMORY_PROMOTED), () -> client.getJvmGcMemoryPromoted(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_GC_PAUSE), () -> client.getJvmGcPause(), null);

        /* Java Virtual Machine (JVM) Virtual Memory Metrics */
        pBuilder.defineProperty(nameMapper.apply(JVM_MEMORY_COMMITTED), () -> client.getJvmMemoryCommited(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_MEMORY_MAX), () -> client.getJvmMemoryMax(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_MEMORY_USED), () -> client.getJvmMemoryUsed(), null);

        /* Java Virtual Machine (JVM) Thread Metrics */
        pBuilder.defineProperty(nameMapper.apply(JVM_THREADS_DAEMON), () -> client.getJvmThreadsDaemon(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_THREADS_LIVE), () -> client.getJvmThreadsLive(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_THREADS_PEAK), () -> client.getJvmThreadsPeak(), null);
        pBuilder.defineProperty(nameMapper.apply(JVM_THREADS_STATES), () -> client.getJvmThreadsStates(), null);

        /* Logback events */
        pBuilder.defineProperty(nameMapper.apply(LOGBACK_EVENTS), () -> client.getLogbackEvents(), null);

        /* Process Metrics */
        pBuilder.defineProperty(nameMapper.apply(PROCESS_CPU_USAGE), () -> client.getProcessCpuUsage(), null);
        pBuilder.defineProperty(nameMapper.apply(PROCESS_START_TIME), () -> client.getProcessStartTime(), null);
        pBuilder.defineProperty(nameMapper.apply(PROCESS_UPTIME), () -> client.getProcessUptime(), null);

        /* System CPU metrics */
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_CPU_COUNT), () -> client.getSystemCpuCount(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_CPU_USAGE), () -> client.getSystemCpuUsage(), null);

        /* System Disk Capacity metrics */
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_FREE), () -> client.getSystemDiskFree(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_TOTAL), () -> client.getSystemDiskTotal(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_USABLE), () -> client.getSystemDiskUsable(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_USED), () -> client.getSystemsDiskUsed(), null);

        /* System Physical Memory metrics */
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_FREE), () -> client.getSystemMemoryFree(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_TOTAL), () -> client.getSystemMemoryTotal(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_USAGE), () -> client.getSystemMemoryUsage(), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_USED), () -> client.getSystemMemoryUsed(), null);

        /* Configuration operations */
        pBuilder.defineOperation(nameMapper.apply(SET_MEMORY_BASE_UNIT), (args) -> client.setMemoryBaseUnit(args));
        pBuilder.defineOperation(nameMapper.apply(SET_DISK_BASE_UNIT), (args) -> client.setDiskBaseUnit(args));
    }

    /**
     * Adds the property implementations to the protocol server builder directly asking a given metrics provider.
     * 
     * @param pBuilder protocol server builder
     * @param provider the metrics provider
     * @param filter   metrics filter, may be <b>null</b> for all (currently ignored)
     * @param nameMapper the implementation name mapper
     */
    public static void addMetricsProtocols(ProtocolServerBuilder pBuilder, MetricsProvider provider, 
        Predicate<String> filter, Function<String, String> nameMapper) {
        /* Meter lists */
        pBuilder.defineProperty(nameMapper.apply(GAUGE_LIST), () -> provider.getCustomGaugeList(), null);
        pBuilder.defineProperty(nameMapper.apply(COUNTER_LIST), () -> provider.getCustomCounterList(), null);
        pBuilder.defineProperty(nameMapper.apply(TIMER_LIST), () -> provider.getCustomTimerList(), null);
        pBuilder.defineProperty(nameMapper.apply(TAGGED_METER_LIST), () -> provider.getTaggedMeterList(), null);
        pBuilder.defineProperty(nameMapper.apply(SIMPLE_METER_LIST), () -> provider.getSimpleMeterList(), null);

        // initial set, some seem to come with micrometer/spring
        // align with addProviderMetricsToAasSubmodel
        
        /* Process Metrics */
        /* System CPU metrics */

        /* System Disk Capacity metrics */
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_FREE), 
            () -> getMeter(provider, MetricsProvider.SYS_DISK_FREE), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_TOTAL), 
            () -> getMeter(provider, MetricsProvider.SYS_DISK_TOTAL), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_USABLE), 
            () -> getMeter(provider, MetricsProvider.SYS_DISK_USABLE), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_DISK_USED), 
            () -> getMeter(provider, MetricsProvider.SYS_DISK_USED), null);

        /* System Physical Memory metrics */
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_FREE), 
            () -> getMeter(provider, MetricsProvider.SYS_MEM_FREE), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_TOTAL), 
            () -> getMeter(provider, MetricsProvider.SYS_MEM_TOTAL), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_USAGE), 
            () -> getMeter(provider, MetricsProvider.SYS_MEM_USAGE), null);
        pBuilder.defineProperty(nameMapper.apply(SYSTEM_MEMORY_USED), 
            () -> getMeter(provider, MetricsProvider.SYS_MEM_USED), null);
        /* Configuration operations */
    }
    
    /**
     * Returns a meter of the {@code provider}.
     * 
     * @param provider the metrics provider
     * @param name the meter name
     * @return <b>null</b> if the meter does not (yet) exist, the meter JSON else
     */
    private static String getMeter(MetricsProvider provider, String name) {
        String result;
        try {
            result = provider.getMeter(name, MetricsProvider.EMPTY_TAGS);
        } catch (IllegalArgumentException e) {
            result = null;
        }
        return result;
    }

    /**
     * Adds a custom metric to the AAS submodel.<br>
     * The metric will be mapped as a property and will have a getter that retrieves
     * the metric from the server side in the same way as the other metrics.
     * Additionally, two operations will be created to add the functionality
     * corresponding to the update and deletion of a metric. These operations will
     * have the same name as the custom metric adding the tags
     * {@link MetricsAasConstants#UPDATE} and {@link MetricsAasConstants#DELETE} in
     * front.<br>
     * For example, if the name of the custom meter is {@code custommeter}, the
     * property will be called {@code custommeter}, the update operation will be
     * called {@code updatecustommeter} and the delete operation
     * {@code deletecustommeter}.<br>
     * To define the type of meter, we have to specify it using {@link MeterType}
     * 
     * @param bundle the bundle containing the required elements to add the
     *               properties and their implementation
     * @param name   URN we want the custom metric to have
     * @param type   type of custom meter
     * @throws IllegalArgumentException if the name, the type and the bundle are
     *                                  null or empty
     */
    public static void addCustomMetric(MetricsAasConstructionBundle bundle, String name, MeterType type) {
        SubmodelElementContainerBuilder smBuilder = bundle.getSubmodelBuilder();
        InvocablesCreator iCreator = bundle.getInvocablesCreator();
        ProtocolServerBuilder pBuilder = bundle.getProtocolBuilder();
        MetricsExtractorRestClient client = bundle.getClient();
        Function<String, String> nameMapper = bundle.getNameMapper();
        
        addCustomMetricToSubmodel(smBuilder, iCreator, name, type, nameMapper);
        addCustomMetricProtocols(pBuilder, client, name, type, nameMapper);
    }

    /**
     * Adds a custom metric to the AAS submodel.<br>
     * The metric will be mapped as a property and will have a getter that retrieves
     * the metric from the server side in the same way as the other metrics.
     * Additionally, two operations will be created to add the functionality
     * corresponding to the update and deletion of a metric. These operations will
     * have the same name as the custom metric adding the tags
     * {@link MetricsAasConstants#UPDATE} and {@link MetricsAasConstants#DELETE} in
     * front.<br>
     * For example, if the name of the custom meter is {@code custommeter}, the
     * property will be called {@code custommeter}, the update operation will be
     * called {@code updatecustommeter} and the delete operation
     * {@code deletecustommeter}.<br>
     * To define the type of meter, we have to specify it using {@link MeterType}
     * 
     * @param smBuilder submodel/elements builder of the AAS
     * @param iCreator  invocables creator of the AAS
     * @param name   URN we want the custom metric to have
     * @param type   type of custom meter
     * @param nameMapper the implementation name mapper
     * @throws IllegalArgumentException if the name, the type and the bundle are null or empty
     */
    public static void addCustomMetricToSubmodel(SubmodelElementContainerBuilder smBuilder, InvocablesCreator iCreator, 
        String name, MeterType type, Function<String, String> nameMapper) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is null or is empty!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is null!");
        }

        smBuilder.createPropertyBuilder(name).setType(Type.STRING)
                .bind(iCreator.createGetter(nameMapper.apply(name)), InvocablesCreator.READ_ONLY).build();
        smBuilder.createOperationBuilder(UPDATE + name).setInvocable(
            iCreator.createInvocable(nameMapper.apply(UPDATE + name))).addInputVariable(BODY, Type.STRING).build();
        smBuilder.createOperationBuilder(DELETE + name).setInvocable(
            iCreator.createInvocable(nameMapper.apply(DELETE + name))).build();
    }

    /**
     * Adds a custom metric to the implementation protocol.<br>
     * The metric will be mapped as a property and will have a getter that retrieves
     * the metric from the server side in the same way as the other metrics.
     * Additionally, two operations will be created to add the functionality
     * corresponding to the update and deletion of a metric. These operations will
     * have the same name as the custom metric adding the tags
     * {@link MetricsAasConstants#UPDATE} and {@link MetricsAasConstants#DELETE} in
     * front.<br>
     * For example, if the name of the custom meter is {@code custommeter}, the
     * property will be called {@code custommeter}, the update operation will be
     * called {@code updatecustommeter} and the delete operation
     * {@code deletecustommeter}.<br>
     * To define the type of meter, we have to specify it using {@link MeterType}
     * 
     * @param pBuilder protocol server builder
     * @param client   instance of the REST client
     * @param name   URN we want the custom metric to have
     * @param type   type of custom meter
     * @param nameMapper the implementation name mapper
     * @throws IllegalArgumentException if the name, the type and the bundle are null or empty
     */
    public static void addCustomMetricProtocols(ProtocolServerBuilder pBuilder, MetricsExtractorRestClient client, 
        String name, MeterType type, Function<String, String> nameMapper) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is null or is empty!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is null!");
        }

        final Object[] deleteArg = {name};

        switch (type) {
        case COUNTER:
            pBuilder.defineProperty(nameMapper.apply(name), () -> client.getCustomCounter(name), null);
            pBuilder.defineOperation(nameMapper.apply(UPDATE + name), (args) -> client.updateCustomCounter(args));
            pBuilder.defineOperation(nameMapper.apply(DELETE + name), (args) -> client.deleteCustomCounter(deleteArg));
            break;
        case GAUGE:
            pBuilder.defineProperty(nameMapper.apply(name), () -> client.getCustomGauge(name), null);
            pBuilder.defineOperation(nameMapper.apply(UPDATE + name), (args) -> client.updateCustomGauge(args));
            pBuilder.defineOperation(nameMapper.apply(DELETE + name), (args) -> client.deleteCustomGauge(deleteArg));
            break;
        case TIMER:
            pBuilder.defineProperty(nameMapper.apply(name), () -> client.getCustomTimer(name), null);
            pBuilder.defineOperation(nameMapper.apply(UPDATE + name), (args) -> client.updateCustomTimer(args));
            pBuilder.defineOperation(nameMapper.apply(DELETE + name), (args) -> client.deleteCustomTimer(deleteArg));
            break;
        default:
            // shall not occur
            break;
        }
    }

}
