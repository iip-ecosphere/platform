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

package de.iip_ecosphere.platform.services.spring.metricsProvider.metricsAas;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

import static de.iip_ecosphere.platform.services.spring.metricsProvider.metricsAas.MetricsAasConstants.*;

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
 * @author Miguel Gomez
 */
public class MetricsAasConstructor {

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
        addMetricsToAasSubmodel(bundle.getSubmodelBuilder(), bundle.getInvocablesCreator());
        addMetricsProtocols(bundle.getProtocolBuilder(), bundle.getClient());
    }

    /**
     * Adds the properties representing the Metrics to the AAS submodel.
     * 
     * @param smBuilder submodel builder of the AAS
     * @param iCreator  invocables creator of the AAS
     */
    private static void addMetricsToAasSubmodel(SubmodelBuilder smBuilder, InvocablesCreator iCreator) {
        /* Meter lists */
        smBuilder.createPropertyBuilder(GAUGE_LIST).setType(Type.STRING)
                .bind(iCreator.createGetter(GAUGE_LIST), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(COUNTER_LIST).setType(Type.STRING)
                .bind(iCreator.createGetter(COUNTER_LIST), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(TIMER_LIST).setType(Type.STRING)
                .bind(iCreator.createGetter(TIMER_LIST), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(TAGGED_METER_LIST).setType(Type.STRING)
                .bind(iCreator.createGetter(TAGGED_METER_LIST), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SIMPLE_METER_LIST).setType(Type.STRING)
                .bind(iCreator.createGetter(SIMPLE_METER_LIST), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Buffer Metrics */
        smBuilder.createPropertyBuilder(JVM_BUFFER_COUNT).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_BUFFER_COUNT), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_BUFFER_MEMORY_USED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_BUFFER_MEMORY_USED), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_BUFFER_TOTAL_CAPACITY).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_BUFFER_TOTAL_CAPACITY), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Class Counter Metrics */
        smBuilder.createPropertyBuilder(JVM_CLASSES_LOADED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_CLASSES_LOADED), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_CLASSES_UNLOADED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_CLASSES_UNLOADED), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Garbage Collector (GC) Metrics */
        smBuilder.createPropertyBuilder(JVM_GC_LIVE_DATA_SIZE).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_GC_LIVE_DATA_SIZE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_MAX_DATA_SIZE).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_GC_MAX_DATA_SIZE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_MEMORY_ALLOCATED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_GC_MEMORY_ALLOCATED), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_MEMORY_PROMOTED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_GC_MEMORY_PROMOTED), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_GC_PAUSE).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_GC_PAUSE), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Virtual Memory Metrics */
        smBuilder.createPropertyBuilder(JVM_MEMORY_COMMITTED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_MEMORY_COMMITTED), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_MEMORY_MAX).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_MEMORY_MAX), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_MEMORY_USED).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_MEMORY_USED), InvocablesCreator.READ_ONLY).build();

        /* Java Virtual Machine (JVM) Thread Metrics */
        smBuilder.createPropertyBuilder(JVM_THREADS_DAEMON).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_THREADS_DAEMON), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_THREADS_LIVE).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_THREADS_LIVE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_THREADS_PEAK).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_THREADS_PEAK), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(JVM_THREADS_STATES).setType(Type.STRING)
                .bind(iCreator.createGetter(JVM_THREADS_STATES), InvocablesCreator.READ_ONLY).build();

        /* Logback events */
        smBuilder.createPropertyBuilder(LOGBACK_EVENTS).setType(Type.STRING)
                .bind(iCreator.createGetter(LOGBACK_EVENTS), InvocablesCreator.READ_ONLY).build();

        /* Process Metrics */
        smBuilder.createPropertyBuilder(PROCESS_CPU_USAGE).setType(Type.STRING)
                .bind(iCreator.createGetter(PROCESS_CPU_USAGE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(PROCESS_START_TIME).setType(Type.STRING)
                .bind(iCreator.createGetter(PROCESS_START_TIME), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(PROCESS_UPTIME).setType(Type.STRING)
                .bind(iCreator.createGetter(PROCESS_UPTIME), InvocablesCreator.READ_ONLY).build();

        /* System CPU metrics */
        smBuilder.createPropertyBuilder(SYSTEM_CPU_COUNT).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_CPU_COUNT), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_CPU_USAGE).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_CPU_USAGE), InvocablesCreator.READ_ONLY).build();

        addMetricsToAasSubmodel2(smBuilder, iCreator);
    }

    /**
     * Adds the configuration operations representing the Metrics to the AAS submodel.
     * 
     * @param smBuilder submodel builder of the AAS
     * @param iCreator  invocables creator of the AAS
     */
    private static void addMetricsToAasSubmodel2(SubmodelBuilder smBuilder, InvocablesCreator iCreator) {
        /* System Disk Capacity metrics */
        smBuilder.createPropertyBuilder(SYSTEM_DISK_FREE).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_DISK_FREE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_TOTAL).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_DISK_TOTAL), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USABLE).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_DISK_USABLE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_DISK_USED).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_DISK_USED), InvocablesCreator.READ_ONLY).build();

        /* System Physical Memory metrics */
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_FREE).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_MEMORY_FREE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_TOTAL).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_MEMORY_TOTAL), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USAGE).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_MEMORY_USAGE), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(SYSTEM_MEMORY_USED).setType(Type.STRING)
                .bind(iCreator.createGetter(SYSTEM_MEMORY_USED), InvocablesCreator.READ_ONLY).build();
        
        /* Configuration operations */
        smBuilder.createOperationBuilder(SET_MEMORY_BASE_UNIT)
                .setInvocable(iCreator.createInvocable(SET_MEMORY_BASE_UNIT)).addInputVariable(BODY, Type.STRING)
                .build();
        smBuilder.createOperationBuilder(SET_DISK_BASE_UNIT).setInvocable(iCreator.createInvocable(SET_DISK_BASE_UNIT))
                .addInputVariable(BODY, Type.STRING).build();
    }

    /**
     * Adds the property implementations to the protocol server builder.
     * 
     * @param pBuilder protocol server builder
     * @param client   instance of the REST client
     */
    private static void addMetricsProtocols(ProtocolServerBuilder pBuilder, MetricsExtractorRestClient client) {
        /* Meter lists */
        pBuilder.defineProperty(GAUGE_LIST, () -> client.getGaugeList(), null);
        pBuilder.defineProperty(COUNTER_LIST, () -> client.getCounterList(), null);
        pBuilder.defineProperty(TIMER_LIST, () -> client.getTimerList(), null);
        pBuilder.defineProperty(TAGGED_METER_LIST, () -> client.getTaggedMeterList(), null);
        pBuilder.defineProperty(SIMPLE_METER_LIST, () -> client.getSimpleMeterList(), null);

        /* Java Virtual Machine (JVM) Buffer Metrics */
        pBuilder.defineProperty(JVM_BUFFER_COUNT, () -> client.getJvmBufferCount(), null);
        pBuilder.defineProperty(JVM_BUFFER_MEMORY_USED, () -> client.getJvmBufferMemoryUsed(), null);
        pBuilder.defineProperty(JVM_BUFFER_TOTAL_CAPACITY, () -> client.getJvmBufferTotalCapacity(), null);

        /* Java Virtual Machine (JVM) Class Counter Metrics */
        pBuilder.defineProperty(JVM_CLASSES_LOADED, () -> client.getJvmClassesLoaded(), null);
        pBuilder.defineProperty(JVM_CLASSES_UNLOADED, () -> client.getJvmClassesUnloaded(), null);

        /* Java Virtual Machine (JVM) Garbage Collector (GC) Metrics */
        pBuilder.defineProperty(JVM_GC_LIVE_DATA_SIZE, () -> client.getJvmGcLiveDataSize(), null);
        pBuilder.defineProperty(JVM_GC_MAX_DATA_SIZE, () -> client.getJvmGcMaxDataSize(), null);
        pBuilder.defineProperty(JVM_GC_MEMORY_ALLOCATED, () -> client.getJvmGcMemoryAllocated(), null);
        pBuilder.defineProperty(JVM_GC_MEMORY_PROMOTED, () -> client.getJvmGcMemoryPromoted(), null);
        pBuilder.defineProperty(JVM_GC_PAUSE, () -> client.getJvmGcPause(), null);

        /* Java Virtual Machine (JVM) Virtual Memory Metrics */
        pBuilder.defineProperty(JVM_MEMORY_COMMITTED, () -> client.getJvmMemoryCommited(), null);
        pBuilder.defineProperty(JVM_MEMORY_MAX, () -> client.getJvmMemoryMax(), null);
        pBuilder.defineProperty(JVM_MEMORY_USED, () -> client.getJvmMemoryUsed(), null);

        /* Java Virtual Machine (JVM) Thread Metrics */
        pBuilder.defineProperty(JVM_THREADS_DAEMON, () -> client.getJvmThreadsDaemon(), null);
        pBuilder.defineProperty(JVM_THREADS_LIVE, () -> client.getJvmThreadsLive(), null);
        pBuilder.defineProperty(JVM_THREADS_PEAK, () -> client.getJvmThreadsPeak(), null);
        pBuilder.defineProperty(JVM_THREADS_STATES, () -> client.getJvmThreadsStates(), null);

        /* Logback events */
        pBuilder.defineProperty(LOGBACK_EVENTS, () -> client.getLogbackEvents(), null);

        /* Process Metrics */
        pBuilder.defineProperty(PROCESS_CPU_USAGE, () -> client.getProcessCpuUsage(), null);
        pBuilder.defineProperty(PROCESS_START_TIME, () -> client.getProcessStartTime(), null);
        pBuilder.defineProperty(PROCESS_UPTIME, () -> client.getProcessUptime(), null);

        /* System CPU metrics */
        pBuilder.defineProperty(SYSTEM_CPU_COUNT, () -> client.getSystemCpuCount(), null);
        pBuilder.defineProperty(SYSTEM_CPU_USAGE, () -> client.getSystemCpuUsage(), null);

        /* System Disk Capacity metrics */
        pBuilder.defineProperty(SYSTEM_DISK_FREE, () -> client.getSystemDiskFree(), null);
        pBuilder.defineProperty(SYSTEM_DISK_TOTAL, () -> client.getSystemDiskTotal(), null);
        pBuilder.defineProperty(SYSTEM_DISK_USABLE, () -> client.getSystemDiskUsable(), null);
        pBuilder.defineProperty(SYSTEM_DISK_USED, () -> client.getSystemsDiskUsed(), null);

        /* System Physical Memory metrics */
        pBuilder.defineProperty(SYSTEM_MEMORY_FREE, () -> client.getSystemMemoryFree(), null);
        pBuilder.defineProperty(SYSTEM_MEMORY_TOTAL, () -> client.getSystemMemoryTotal(), null);
        pBuilder.defineProperty(SYSTEM_MEMORY_USAGE, () -> client.getSystemMemoryUsage(), null);
        pBuilder.defineProperty(SYSTEM_MEMORY_USED, () -> client.getSystemMemoryUsed(), null);

        /* Configuration operations */
        pBuilder.defineOperation(SET_MEMORY_BASE_UNIT, (args) -> client.setMemoryBaseUnit(args));
        pBuilder.defineOperation(SET_DISK_BASE_UNIT, (args) -> client.setDiskBaseUnit(args));
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
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is null or is empty!");
        }
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle is null!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is null!");
        }
        SubmodelBuilder smBuilder = bundle.getSubmodelBuilder();
        InvocablesCreator iCreator = bundle.getInvocablesCreator();
        ProtocolServerBuilder pBuilder = bundle.getProtocolBuilder();
        MetricsExtractorRestClient client = bundle.getClient();

        smBuilder.createPropertyBuilder(name).setType(Type.STRING)
                .bind(iCreator.createGetter(name), InvocablesCreator.READ_ONLY).build();
        smBuilder.createOperationBuilder(UPDATE + name).setInvocable(iCreator.createInvocable(UPDATE + name))
                .addInputVariable(BODY, Type.STRING).build();
        smBuilder.createOperationBuilder(DELETE + name).setInvocable(iCreator.createInvocable(DELETE + name)).build();

        final Object[] deleteArg = {name};

        switch (type) {
        case COUNTER:
            pBuilder.defineProperty(name, () -> client.getCustomCounter(name), null);
            pBuilder.defineOperation(UPDATE + name, (args) -> client.updateCustomCounter(args));
            pBuilder.defineOperation(DELETE + name, (args) -> client.deleteCustomCounter(deleteArg));
            break;
        case GAUGE:
            pBuilder.defineProperty(name, () -> client.getCustomGauge(name), null);
            pBuilder.defineOperation(UPDATE + name, (args) -> client.updateCustomGauge(args));
            pBuilder.defineOperation(DELETE + name, (args) -> client.deleteCustomGauge(deleteArg));
            break;
        case TIMER:
            pBuilder.defineProperty(name, () -> client.getCustomTimer(name), null);
            pBuilder.defineOperation(UPDATE + name, (args) -> client.updateCustomTimer(args));
            pBuilder.defineOperation(DELETE + name, (args) -> client.deleteCustomTimer(deleteArg));
            break;
        default:
            // shall not occur
            break;
        }
    }

}
