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

import de.iip_ecosphere.platform.transport.streams.StreamNames;

/**
 * Collection of constant name values for submodel properties.<br>
 * This class acts as a collection of constant values used mostly by the
 * {@link MetricsAasConstructor} class, but can also be used by the AAS in
 * operation to access the properties exposed by the Metrics Provider without
 * manually writing the String.
 * 
 * @author Miguel Gomez
 */
public class MetricsAasConstants {
    
    public static final String TRANSPORT_SERVICE_METRICS_CHANNEL = StreamNames.SERVICE_METRICS;

    /* Java Virtual Machine (JVM) Buffer Metrics */
    public static final String JVM_BUFFER_COUNT = "jvmbuffercount";
    public static final String JVM_BUFFER_MEMORY_USED = "jvmbuffermemoryused";
    public static final String JVM_BUFFER_TOTAL_CAPACITY = "jvmbuffertotalcapacity";

    /* Java Virtual Machine (JVM) Class Counter Metrics */
    public static final String JVM_CLASSES_LOADED = "jvmclassesloaded";
    public static final String JVM_CLASSES_UNLOADED = "jvmclassesunloaded";

    /* Java Virtual Machine (JVM) Garbage Collector (GC) Metrics */
    public static final String JVM_GC_LIVE_DATA_SIZE = "jvmgclivedatasize";
    public static final String JVM_GC_MAX_DATA_SIZE = "jvmgcmaxdatasize";
    public static final String JVM_GC_MEMORY_ALLOCATED = "jvmgcmemoryallocated";
    public static final String JVM_GC_MEMORY_PROMOTED = "jvmgcmemorypromoted";
    public static final String JVM_GC_PAUSE = "jvmgcpause";

    /* Java Virtual Machine (JVM) Virtual Memory Metrics */
    public static final String JVM_MEMORY_COMMITTED = "jvmmemorycommitted";
    public static final String JVM_MEMORY_MAX = "jvmmemorymax";
    public static final String JVM_MEMORY_USED = "jvmmemoryused";

    /* Java Virtual Machine (JVM) Thread Metrics */
    public static final String JVM_THREADS_DAEMON = "jvmthreadsdaemon";
    public static final String JVM_THREADS_LIVE = "jvmthreadslive";
    public static final String JVM_THREADS_PEAK = "jvmthreadspeak";
    public static final String JVM_THREADS_STATES = "jvmthreadsstates";

    /* Logback events */
    public static final String LOGBACK_EVENTS = "logbackevents";

    /* Process Metrics */
    public static final String PROCESS_CPU_USAGE = "processcpuusage";
    public static final String PROCESS_START_TIME = "processstarttime";
    public static final String PROCESS_UPTIME = "processuptime";

    /* System CPU metrics */
    public static final String SYSTEM_CPU_COUNT = "systemcpucount";
    public static final String SYSTEM_CPU_USAGE = "systemcpuusage";

    /* System Disk Capacity metrics */
    public static final String SYSTEM_DISK_FREE = "Storage_Free";
    public static final String SYSTEM_DISK_TOTAL = "Storage_Capacity"; // IDTA
    public static final String SYSTEM_DISK_USABLE = "Storage_Usable";
    public static final String SYSTEM_DISK_USED = "Allocated_Storage"; // IDTA
    
    /* System Physical Memory metrics */
    public static final String SYSTEM_MEMORY_FREE = "Memory_Free";
    public static final String SYSTEM_MEMORY_TOTAL = "Memory_Capacity"; // IDTA
    public static final String SYSTEM_MEMORY_USAGE = "Allocated_Memory";
    public static final String SYSTEM_MEMORY_USED = "Memory_Used"; // IDTA

    /* Custom tags */
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String BODY = "body";

    /* Configuration tags */
    public static final String SET_MEMORY_BASE_UNIT = "setmemorybaseunit";
    public static final String SET_DISK_BASE_UNIT = "setdiskbaseunit";

    /* Attributes for the JsonArrays from tagged meters */
    public static final String TAGS_ATTR = "tags";
    public static final String METER_ATTR = "meter";

    /* List Properties */
    public static final String GAUGE_LIST = "gaugelist";
    public static final String COUNTER_LIST = "counterlist";
    public static final String TIMER_LIST = "timerlist";
    public static final String TAGGED_METER_LIST = "taggedmeterlist";
    public static final String SIMPLE_METER_LIST = "simplemeterlist";

}
