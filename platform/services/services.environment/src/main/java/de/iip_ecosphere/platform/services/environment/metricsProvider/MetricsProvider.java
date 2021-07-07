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

package de.iip_ecosphere.platform.services.environment.metricsProvider;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.util.concurrent.AtomicDouble;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.MeterNotFoundException;

import com.sun.management.OperatingSystemMXBean;

/**
 * This class represents an interface to manage the Micrometer-API meters.<br>
 * The operations available in this class are:
 * <ul>
 * <li>Consulting the amount of custom meters of each type</li>
 * <li>Modifying, consulting or deleting a custom gauge</li>
 * <li>Modifying, consulting or deleting a custom counter</li>
 * <li>Modifying, consulting or deleting a custom timer</li>
 * <li>Consult the current capacity base unit for the Memory and Disk
 * metrics</li>
 * </ul>
 * It is recommended to use the dot notation to name the meters, i.e.
 * my.custom.meter, as it is the one used by default by the already existing
 * metrics exposed by Micrometer<br>
 * It is also mentioned that the Timer meter is the most resource consuming
 * meter. It is recommended to use the already existing global timers instead of
 * creating a new one.<br>
 * 
 * @author Miguel Gomez
 */
@SuppressWarnings("restriction")
public class MetricsProvider {

    // Some of the system metrics that we want to expose
    public static final String SYS_MEM_TOTAL = "system.memory.total";
    public static final String SYS_MEM_FREE = "system.memory.free";
    public static final String SYS_MEM_USED = "system.memory.used";
    public static final String SYS_MEM_USAGE = "system.memory.usage";

    public static final String SYS_DISK_TOTAL = "system.disk.total";
    public static final String SYS_DISK_FREE = "system.disk.free";
    public static final String SYS_DISK_USABLE = "system.disk.usable";
    public static final String SYS_DISK_USED = "system.disk.used";

    // Error Messages
    protected static final String ID_NOT_FOUND_ERRMSG = ": no item found with this identifier!";
    protected static final String NON_POSITIVE_ERRMSG = ": is not a positive number!";
    protected static final String NULL_ARG = " has a null value. This argument cannot be null!";

    // Tools
    private final MeterRegistry registry;
    private final OperatingSystemMXBean osmxb;

    // Flag required for correct initialization of system metrics
    private boolean init;
    
    /* By default, the base unit for the memory metrics is bytes */
    private CapacityBaseUnit memoryBaseUnit = CapacityBaseUnit.BYTES;

    /* By default the base unit for disk capacity is kilobytes */
    private CapacityBaseUnit diskBaseUnit = CapacityBaseUnit.KILOBYTES;

    // Metric containers
    private final Map<String, AtomicDouble> gauges;
    private final Map<String, Counter> counters;
    private final Map<String, Timer> timers;

    // Attributes to simplify gauges
    private double sysMemTotal;
    private double sysMemFree;
    private double sysMemUsed;
    private double sysMemUsage;
    private double sysDiskTotal;
    private double sysDiskFree;
    private double sysDiskUsable;
    private double sysDiskUsed;

    /**
     * Create a new Metrics Provider Instance.<br>
     * The Metrics Provider will have a map of metrics that can be operated by the
     * client via the appropriate methods, allowing the user to add new custom
     * metrics when needed and manipulate them in a uniform manner. <br>
     * This constructor should be called automatically by the Spring boot framework
     * and accessed by an autowired attribute as a result.
     * 
     * @param registry where new Meters are registered. Injected by the Spring Boot
     *                 Application
     * @throws IllegalArgumentException if the registry is null
     */
    public MetricsProvider(MeterRegistry registry) {
        if (registry == null) {
            throw new IllegalArgumentException("Registry is null!");
        }

        // Obtain references for the tools
        this.registry = registry;
        osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Initiate the containers
        gauges = new HashMap<String, AtomicDouble>();
        counters = new HashMap<String, Counter>();
        timers = new HashMap<String, Timer>();

        init = true;
    }
    
    /**
     * Registers the extra system metrics onto the registry.<br>
     * The extra system metrics include the physical memory values and the disk
     * values, which are metrics not automatically recorded by Micrometer-API.
     */
    public void registerNonNativeSystemMetrics() {
        registerMemoryMetrics();
        registerDiskMetrics();
        Gauge.builder(SYS_MEM_USAGE, () -> sysMemUsage).description("Current percentage of physical memory in use")
                .register(registry);
    }

    /**
     * Registers the physical memory metrics except for the usage percentage.<br>
     * It can be
     * called multiple times during execution in order to update the memory base
     * unit.
     */
    public void registerMemoryMetrics() {
        Gauge.builder(SYS_MEM_TOTAL, () -> sysMemTotal).description("Total Physical memory of the system")
                .baseUnit(memoryBaseUnit.stringValue()).register(registry);
        Gauge.builder(SYS_MEM_FREE, () -> sysMemFree).description("Free Physical memory of the system")
                .baseUnit(memoryBaseUnit.stringValue()).register(registry);
        Gauge.builder(SYS_MEM_USED, () -> sysMemUsed).description("Physical memory currently in use")
                .baseUnit(memoryBaseUnit.stringValue()).register(registry);
    }

    /**
     * Registers the disk capacity metrics.<br>
     * It can be
     * called multiple times during execution in order to update the disk capacity
     * base unit.
     */
    public void registerDiskMetrics() {
        Gauge.builder(SYS_DISK_TOTAL, () -> sysDiskTotal).description("Total disk capacity of the system")
                .baseUnit(diskBaseUnit.stringValue()).register(registry);
        Gauge.builder(SYS_DISK_FREE, () -> sysDiskFree).description("Total free disk capacity of the system")
                .baseUnit(diskBaseUnit.stringValue()).register(registry);
        Gauge.builder(SYS_DISK_USABLE, () -> sysDiskUsable).description("Total usable disk capacity of the system")
                .baseUnit(diskBaseUnit.stringValue()).register(registry);
        Gauge.builder(SYS_DISK_USED, () -> sysDiskUsed)
                .description("Current total disk capacity currently in use or unavailable")
                .baseUnit(diskBaseUnit.stringValue()).register(registry);
    }

    /**
     * Removes the physical memory metrics from the registry.<br>
     * This method is required as the
     * previous memory metrics have to be removed and registered again after
     * changing the base unit in order for the description to update correctly.
     */
    public void removeMemoryMetrics() {
        registry.remove(registry.get(SYS_MEM_TOTAL).meter());
        registry.remove(registry.get(SYS_MEM_FREE).meter());
        registry.remove(registry.get(SYS_MEM_USED).meter());
    }

    /**
     * Removes the disk capacity metrics from the registry.<br>
     * This method is required as the previous
     * disk capacity metrics have to be removed and registered again after changing
     * the base unit in order for the description to update correctly.
     */
    public void removeDiskMetrics() {
        registry.remove(registry.get(SYS_DISK_TOTAL).meter());
        registry.remove(registry.get(SYS_DISK_FREE).meter());
        registry.remove(registry.get(SYS_DISK_USABLE).meter());
        registry.remove(registry.get(SYS_DISK_USED).meter());
    }

    /**
     * Consult the number of custom gauges that are registered by the
     * MetricsProvider.
     * 
     * @return number of custom gauges currently registered
     */
    public int getNumberOfCustomGauges() {
        return gauges.size();
    }

    /**
     * Consult the number of custom counters that are registered by the
     * MetricsProvider.
     * 
     * @return number of custom counters currently registered
     */
    public int getNumberOfCustomCounters() {
        return counters.size();
    }

    /**
     * Consult the number of custom timers that are registered by the
     * MetricsProvider.
     * 
     * @return number of custom timers currently registered
     */
    public int getNumberOfCustomTimers() {
        return timers.size();
    }

    /**
     * Adds a gauge value to the MeterRegistry.<br>
     * If the identifier does not correspond to an existing gauge, a new gauge will
     * be created and registered. If it is an existing gauge, the value will simply
     * be modified.
     * 
     * @param gaugeId identifier for the gauge
     * @param value   value we want to set the gauge to
     * @throws IllegalArgumentException if the identifier is null
     */
    public void addGaugeValue(String gaugeId, double value) {
        if (gaugeId == null) {
            throw new IllegalArgumentException("gaugeId" + NULL_ARG);
        }
        if (gauges.containsKey(gaugeId)) {
            gauges.get(gaugeId).set(value);
        } else {
            AtomicDouble gauge = registry.gauge(gaugeId, new AtomicDouble(value));
            gauges.put(gaugeId, gauge);
        }
    }

    /**
     * Remove a custom gauge from the Meter Registry.
     * 
     * @param gaugeId identifier of the custom gauge
     * @throws IllegalArgumentException if there is no gauge with that identifier
     */
    public void removeGauge(String gaugeId) {
        if (!gauges.containsKey(gaugeId)) {
            throw new IllegalArgumentException(gaugeId + ID_NOT_FOUND_ERRMSG);
        }
        gauges.remove(gaugeId);
        registry.remove(registry.get(gaugeId).meter());
    }

    /**
     * Retrieves the value of a custom Gauge.<br>
     * If no gauge is found with that identifier, this method will return zero
     * 
     * @param gaugeId identifier of the custom gauge
     * @return current value of the custom gauge or {@code 0.0} if there is no gauge
     *         with the requested identifier
     */
    public double getGaugeValue(String gaugeId) {
        if (gauges.containsKey(gaugeId)) {
            return gauges.get(gaugeId).doubleValue();
        } else {
            return 0.0;
        }
    }
    
    /**
     * Retrieves the value of a registered Gauge, i.e., custom, non-native ones of this provider or 
     * micrometer/pre-registered ones<br>
     * If no gauge is found with that identifier, this method will return zero.
     * 
     * @param gaugeId identifier of the gauge
     * @return current value of the gauge or {@code 0.0} if there is no
     *         gauge with the requested identifier
     */
    public double getRegisteredGaugeValue(String gaugeId) {
        try { // before default ones are registered, the registry request may lead to exception
            return registry.get(gaugeId).gauge().value();
        } catch (MeterNotFoundException e) {
            return getGaugeValue(gaugeId);
        }
    }

    /**
     * Creates a new counter and registers it in the MeterRegistry.
     * 
     * @param counterId identifier of the counter
     */
    private void addCounter(String counterId) {
        Counter counter = registry.counter(counterId);
        counters.put(counterId, counter);
    }

    /**
     * Increases the counter by a certain value.<br>
     * If the identifier does not correspond to an existing counter, a new counter
     * will be created and registered. If it is an existing counter, the value will
     * simply be incremented by the amount requested.
     * 
     * @param counterId identifier of the custom counter
     * @param value     the amount we want to increases the counter
     * @throws IllegalArgumentException if {@code value} is negative or if
     *                                  {@code counterId} is null
     */
    public void increaseCounterBy(String counterId, double value) {
        if (counterId == null) {
            throw new IllegalArgumentException("counterId" + NULL_ARG);
        }
        if (value < 0.0) {
            throw new IllegalArgumentException(value + NON_POSITIVE_ERRMSG);
        }
        if (!counters.containsKey(counterId)) {
            addCounter(counterId);
        }
        counters.get(counterId).increment(value);
    }

    /**
     * Increases the counter by one. <br>
     * If the identifier does not correspond to an existing counter, a new counter
     * will be created and registered. If it is an existing counter, the value will
     * simply be incremented.
     * 
     * @param counterId identifier of the custom counter
     * @throws IllegalArgumentException if {@code value} is negative or if
     *                                  {@code counterId} is null
     */
    public void increaseCounter(String counterId) {
        increaseCounterBy(counterId, 1.0);
    }

    /**
     * Remove a custom counter from the Meter Registry.
     * 
     * @param counterId identifier of the custom counter
     * @throws IllegalArgumentException if there is no counter with that identifier
     */
    public void removeCounter(String counterId) {
        if (!counters.containsKey(counterId)) {
            throw new IllegalArgumentException(counterId + ID_NOT_FOUND_ERRMSG);
        }

        counters.remove(counterId);
        registry.remove(registry.get(counterId).meter());
    }

    /**
     * Retrieves the value of a custom Counter.<br>
     * If no counter is found with that identifier, this method will return zero.
     * 
     * @param counterId identifier of the custom counter
     * @return current value of the custom counter or {@code 0.0} if there is no
     *         counter with the requested identifier
     */
    public double getCounterValue(String counterId) {
        if (counters.containsKey(counterId)) {
            return counters.get(counterId).count();
        } else {
            return 0.0;
        }
    }

    /**
     * Retrieves the value of a registered Counter, i.e., custom, non-native ones of this provider or 
     * micrometer/pre-registered ones<br>
     * If no counter is found with that identifier, this method will return zero.
     * 
     * @param counterId identifier of the counter
     * @return current value of the counter or {@code 0.0} if there is no
     *         counter with the requested identifier
     */
    public double getRegisteredCounterValue(String counterId) {
        try { // before default ones are registered, the registry request may lead to exception
            return registry.get(counterId).counter().count();
        } catch (MeterNotFoundException e) {
            return getCounterValue(counterId);
        }
    }

    /**
     * Create a new custom timer to be added to the Meter registry.
     * 
     * @param timerId identifier of the custom timer
     * @throws IllegalArgumentException if the identifier is null
     */
    protected void addTimer(String timerId) {
        if (timerId == null) {
            throw new IllegalArgumentException("timerId" + NULL_ARG);
        }
        Timer timer = registry.timer(timerId);
        timers.put(timerId, timer);
    }

    /**
     * Remove a custom timer from the Registry.
     * 
     * @param timerId identifier of the custom timer
     * @throws IllegalArgumentException if there is no timer with that identifier
     */
    public void removeTimer(String timerId) {
        if (!timers.containsKey(timerId)) {
            throw new IllegalArgumentException(timerId + ID_NOT_FOUND_ERRMSG);
        }

        timers.remove(timerId);
        registry.remove(registry.get(timerId).meter());
    }

    /**
     * Records the execution of a runnable using a timer.
     * 
     * @param timerId  identifier of the custom timer
     * @param runnable runnable function that we want to record
     * @throws IllegalArgumentException if the runnable or the identifier are null
     */
    public void recordWithTimer(String timerId, Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("runnable" + NULL_ARG);
        }
        if (!timers.containsKey(timerId)) {
            addTimer(timerId);
        }

        timers.get(timerId).record(runnable);
    }

    /**
     * Records the execution of a supplier using a timer.
     * 
     * @param <T>      the datatype that the supplier returns
     * @param timerId  identifier of the custom timer
     * @param supplier supplier function that we want to record
     * @throws IllegalArgumentException if the identifier or the supplier are null
     * @return the value returned by the supplier
     */
    public <T> T recordWithTimer(String timerId, Supplier<T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier" + NULL_ARG);
        }
        if (!timers.containsKey(timerId)) {
            addTimer(timerId);
        }

        return timers.get(timerId).record(supplier);
    }

    /**
     * Adds time from a specific timer.
     * 
     * @param timerId identifier of the custom timer
     * @param time    amount of time we want to record
     * @param unit    base unit of the time we want to record
     * @throws IllegalArgumentException if the identifier or the supplier are null
     * @throws IllegalArgumentException if the {@code time} is negative
     */
    public void recordWithTimer(String timerId, long time, TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException("cannot record negative time!");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit" + NULL_ARG);
        }
        if (!timers.containsKey(timerId)) {
            addTimer(timerId);
        }

        timers.get(timerId).record(time, unit);
    }

    /**
     * Retrieves the total time from the timer in its base unit.<br>
     * If no timer is found with that identifier, this method will return zero. The
     * base unit for a timer is, by default, seconds.
     * 
     * @param timerId identifier of the custom timer
     * @return total time recorded by the timer or {@code 0.0} if no timer is found
     *         with that identifier
     */
    public double getTotalTimeFromTimer(String timerId) {
        if (timers.containsKey(timerId)) {
            Timer t = timers.get(timerId);
            return t.totalTime(t.baseTimeUnit());
        } else {
            return 0.0;
        }
    }

    /**
     * Retrieves the maximum time recorded by the timer in its base unit.<br>
     * If no timer is found with that identifier, this method will return zero. The
     * base unit for a timer is, by default, seconds.
     * 
     * @param timerId identifier of the custom timer
     * @return maximum time recorded by the timer or {@code 0.0} if no timer is
     *         found with that identifier
     */
    public double getMaxTimeFromTimer(String timerId) {
        if (timers.containsKey(timerId)) {
            Timer t = timers.get(timerId);
            return t.max(t.baseTimeUnit());
        } else {
            return 0.0;
        }
    }

    /**
     * Retrieves the number of times a timer has been used.<br>
     * If no timer is found with that identifier, this method will return zero.
     * 
     * @param timerId identifier of the custom timer
     * @return number of times this timer was called or {@code 0} if no timer is
     *         found with that identifier
     */
    public long getTimerCount(String timerId) {
        if (timers.containsKey(timerId)) {
            return timers.get(timerId).count();
        } else {
            return 0;
        }
    }
    
    /**
     * Retrieves the value of a registered Timer, i.e., custom, non-native ones of this provider or 
     * micrometer/pre-registered ones<br>
     * If no timer is found with that identifier, this method will return zero.
     * 
     * @param timerId identifier of the timer
     * @return current value of the timer or {@code 0} if there is no
     *         timer with the requested identifier
     */
    public long getRegisteredTimerCount(String timerId) {
        try { // before default ones are registered, the registry request may lead to exception
            return registry.get(timerId).timer().count();
        } catch (MeterNotFoundException e) {
            return getTimerCount(timerId);
        }
    }

    /**
     * This operation calculates the values for the extra system metrics not exposed
     * by Micrometer-API.<br>
     * Even though this sacrifices the real time values of these metrics, we gain
     * speed when requesting the metrics as we no longer have to calculate the
     * values upon request. The {@code SHEDULE_RATE} indicates the time in between
     * calculations.
     */
    public void calculateNonNativeSystemMetrics() {
        if (init) {
            // Register the values for the extra system metrics
            registerNonNativeSystemMetrics();
            init = false;
        }

        sysMemTotal = osmxb.getTotalPhysicalMemorySize() / memoryBaseUnit.byteValue();
        sysMemFree = osmxb.getFreePhysicalMemorySize() / memoryBaseUnit.byteValue();
        sysMemUsed = sysMemTotal - sysMemFree;
        sysMemUsage = sysMemUsed / sysMemTotal;

        File[] dirs = File.listRoots();
        sysDiskTotal = 0.0;
        sysDiskFree = 0.0;
        sysDiskUsable = 0.0;

        for (File dir : dirs) {
            sysDiskTotal += dir.getTotalSpace();
            sysDiskFree += dir.getFreeSpace();
            sysDiskUsable += dir.getUsableSpace();
        }

        sysDiskTotal /= diskBaseUnit.byteValue();
        sysDiskFree /= diskBaseUnit.byteValue();
        sysDiskUsable /= diskBaseUnit.byteValue();

        sysDiskUsed = sysDiskTotal - sysDiskFree;
    }

    /**
     * Changes the memory base unit.
     * 
     * @param memoryBaseUnit new capacity base unit for the memory
     * @throws IllegalArgumentException if the {@code memoryBaseUnit} is null
     */
    public void setMemoryBaseUnit(CapacityBaseUnit memoryBaseUnit) {
        if (memoryBaseUnit == null) {
            throw new IllegalArgumentException("memoryBaseUnit" + NULL_ARG);
        }
        this.memoryBaseUnit = memoryBaseUnit;
    }

    /**
     * Consults the capacity base unit used for the physical memory metrics.
     * 
     * @return capacity base unit for the memory metrics
     */
    public CapacityBaseUnit getMemoryBaseUnit() {
        return memoryBaseUnit;
    }

    /**
     * Changes the disk capacity base unit.
     * 
     * @param diskBaseUnit new capacity base unit for the disk capacity
     * @throws IllegalArgumentException if the {@code memoryBaseUnit} is null
     */
    public void setDiskBaseUnit(CapacityBaseUnit diskBaseUnit) {
        if (diskBaseUnit == null) {
            throw new IllegalArgumentException(diskBaseUnit + NULL_ARG);
        }
        this.diskBaseUnit = diskBaseUnit;
    }

    /**
     * Consults the capacity base unit used for the disk capacity metrics.
     * 
     * @return capacity base unit for the disk capacity metrics
     */
    public CapacityBaseUnit getDiskBaseUnit() {
        return diskBaseUnit;
    }

    /**
     * Retrieves a meter as a JSON object.<br>
     * The requested meter is located and parsed as a JsonObject to be sent via
     * HTTP.
     * 
     * @param name name of the meter we want to retrieve
     * @param tags tags the meter has
     * @return the JSON object representing the meter
     * @throws IllegalArgumentException if no meter is found with that name
     */
    public String getMeter(String name, Iterable<Tag> tags) {
        try {
            Meter meter = registry.get(name).tags(tags).meter();
            return jsonParser(meter);
        } catch (MeterNotFoundException mnfe) {
            throw new IllegalArgumentException(mnfe.getMessage());
        }
    }

    /**
     * Retrieves a custom gauge as a JSON object.<br>
     * The requested gauge is located within the map and parsed as a JsonObject to
     * be sent via HTTP.
     * 
     * @param name name of the custom gauge we want to retrieve
     * @return the JSON object representing the gauge
     * @throws IllegalArgumentException if no custom gauge is found with that name
     */
    public String getGauge(String name) {
        if (!gauges.containsKey(name)) {
            throw new IllegalArgumentException(name + ID_NOT_FOUND_ERRMSG);
        } else {
            return jsonParser(registry.get(name).gauge());
        }
    }

    /**
     * Retrieves a custom counter as a JSON object.<br>
     * The requested counter is located within the map and parsed as a JsonObject to
     * be sent via HTTP.
     * 
     * @param name name of the custom counter we want to retrieve
     * @return the JSON object representing the counter
     * @throws IllegalArgumentException if no custom counter is found with that name
     */
    public String getCounter(String name) {
        if (!counters.containsKey(name)) {
            throw new IllegalArgumentException(name + ID_NOT_FOUND_ERRMSG);
        } else {
            return jsonParser(registry.get(name).counter());
        }
    }

    /**
     * Retrieves a custom timer as a JSON object.<br>
     * The requested timer is located within the map and parsed as a JsonObject to
     * be sent via HTTP.
     * 
     * @param name name of the custom timer we want to retrieve
     * @return the JSON object representing the timer
     * @throws IllegalArgumentException if no custom timer is found with that name
     */
    public String getTimer(String name) {
        if (!timers.containsKey(name)) {
            throw new IllegalArgumentException(name + ID_NOT_FOUND_ERRMSG);
        } else {
            return jsonParser(registry.get(name).timer());
        }
    }

    /**
     * Parses a meter into a JsonObject to be sent via HTTP.<br>
     * Due to the current limitations with inserting the actual
     * {@link javax.json.Json} libraries inside this component, this method acts
     * like a crude substitute that creates a String that can later be parsed into a
     * valid JsonObject.<br>
     * In order to maintain a certain uniformity, the resulting JSON object mimics
     * the structure that Micrometer-API metrics have when exposed by the Spring
     * Boot Actuator.<br>
     * It is important to add that if the description contains any {@code "}
     * characters, they will be exchanged for {@code ''} due to the limitations we
     * currently have regarding JSON parsing.
     * 
     * @param meter meter we want to parse
     * @return meter parsed as a String compatible with a JsonObject
     */
    private String jsonParser(Meter meter) {
        String description = meter.getId().getDescription();
        if (description != null) {
            description = description.replaceAll("\"", "''");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(meter.getId().getName()).append("\",");
        sb.append("\"description\":\"").append(description).append("\",");
        sb.append("\"baseUnit\":\"").append(meter.getId().getBaseUnit()).append("\",");
        sb.append("\"measurements\":[");

        for (Measurement m : meter.measure()) {
            sb.append("{");
            sb.append("\"statistic\":\"").append(m.getStatistic().toString()).append("\",");
            sb.append("\"value\":").append(m.getValue());
            sb.append("},");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("],\"availableTags\":[]}");

        return sb.toString();
    }

    /**
     * Returns a list of the names of the registered custom gauges.<br>
     * The list is returned as a JsonArray containing Strings.
     * 
     * @return JsonArray with the list of names of all the registered custom gauges
     */
    public String getCustomGaugeList() {
        return mapJsonParser(gauges);
    }

    /**
     * Returns a list of the names of the registered custom counters.<br>
     * The list is returned as a JsonArray containing Strings.
     * 
     * @return JsonArray with the list of names of all the registered custom
     *         counters
     */
    public String getCustomCounterList() {
        return mapJsonParser(counters);
    }

    /**
     * Returns a list of the names of the registered custom timers.<br>
     * The list is returned as a JsonArray containing Strings.
     * 
     * @return JsonArray with the list of names of all the registered custom timers
     */
    public String getCustomTimerList() {
        return mapJsonParser(timers);
    }

    /**
     * Returns a list of names of the relevant Meters registered by micrometer that
     * have tags.
     * 
     * @return JsonArray with the list of names of the relevant
     *         micrometer-registered meters with tags
     */
    public String getTaggedMeterList() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("\"jvm.buffer.count\",");
        sb.append("\"jvm.buffer.memory.used\",");
        sb.append("\"jvm.buffer.total.capacity\",");
        sb.append("\"jvm.gc.pause\",");
        sb.append("\"jvm.memory.committed\",");
        sb.append("\"jvm.memory.max\",");
        sb.append("\"jvm.memory.used\",");
        sb.append("\"jvm.threads.states\",");
        sb.append(" \"logback.events\"");
        sb.append("]");

        return sb.toString();
    }

    /**
     * Returns a list of names of the relevant Meters registered by micrometer that
     * have no tags.
     * 
     * @return JsonArray with the list of names of the relevant
     *         micrometer-registered meters with no tags
     */
    public String getSimpleMeterList() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("\"jvm.classes.loaded\",");
        sb.append("\"jvm.classes.unloaded\",");
        sb.append("\"jvm.gc.live.data.size\",");
        sb.append("\"jvm.gc.max.data.size\",");
        sb.append("\"jvm.gc.memory.allocated\",");
        sb.append("\"jvm.gc.memory.promoted\",");
        sb.append("\"jvm.threads.daemon\",");
        sb.append("\"jvm.threads.live\",");
        sb.append("\"jvm.threads.peak\",");
        sb.append("\"process.cpu.usage\",");
        sb.append("\"process.start.time\",");
        sb.append("\"process.uptime\",");
        sb.append("\"system.cpu.count\",");
        sb.append("\"system.cpu.usage\",");
        sb.append("\"system.disk.free\",");
        sb.append("\"system.disk.total\",");
        sb.append("\"system.disk.usable\",");
        sb.append("\"system.disk.used\",");
        sb.append("\"system.memory.free\",");
        sb.append("\"system.memory.total\",");
        sb.append("\"system.memory.usage\",");
        sb.append("\"system.memory.used\"");
        sb.append("]");

        return sb.toString();
    }

    /**
     * Parses one of the custom meter maps into a JsonArray containing the
     * names.<br>
     * This method is to be used by {@link MetricsProvider#getCustomCounterList()},
     * {@link MetricsProvider#getCustomGaugeList()} and @link
     * MetricsProvider#getCustomTimerList()}.
     * 
     * @param map custom meter map
     * @return JsonArray of the names of the custom meters from that map
     */
    private String mapJsonParser(Map<String, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Entry<String, ?> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\",");
        }
        if (sb.length() > 1) { // for empty list, we otherwise cut off the lead in 
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");

        return sb.toString();
    }

}
