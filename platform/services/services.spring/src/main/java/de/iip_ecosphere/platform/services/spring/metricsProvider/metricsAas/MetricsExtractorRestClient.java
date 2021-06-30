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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Class that implements a REST client to retrieve the Meters from the Metrics
 * Provider.<br>
 * This class contains the method implementations to send different requests to
 * the Metrics Provider. Apart from having a method that sends a GET request to
 * retrieve all the metrics exposed by the Metrics Provider, it also has a
 * series of methods that allows as to update or create a custom meter from the
 * Metrics provider as well as methods to delete it. This methods should always
 * be used to retrieve the values instead of using another REST client.<br>
 * The methods that follow the IIP-Ecosphere AAS signature do not check the
 * validity of their arguments. Part of this is checked by the IIP-Ecosphere
 * framework and the other part by the Metrics Provider REST Service.
 * 
 * @author Miguel Gomez
 */
public class MetricsExtractorRestClient {

    /* Constants to build the path */
    private static final String PROTOCOL = "http://";
    private static final String SIMPLE_ENDPOINT = "simple-meter";
    private static final String TAGGED_ENDPOINT = "tagged-meter";
    private static final String COUNTERS_ENDPOINT = "counters";
    private static final String GAUGES_ENDPOINT = "gauges";
    private static final String TIMERS_ENDPOINT = "timers";

    private static final String CONFIG_MEMORY_BASE_UNIT = "config/memory-base-unit";
    private static final String CONFIG_DISK_BASE_UNIT = "config/disk-base-unit";

    /* Tags that go with the memory metrics */
    private static final String AREA_HEAP = "area:heap";
    private static final String ID_SURVIVOR_SPACE = "id:PS Survivor Space";
    private static final String ID_OLD_GEN = "id:PS Old Gen";
    private static final String ID_EDEN_SPACE = "id:PS Eden Space";

    private static final String AREA_NONHEAP = "area:nonheap";
    private static final String ID_METASPACE = "id:Metaspace";
    private static final String ID_CODE_CACHE = "id:Code Cache";
    private static final String ID_COMPRESSED_CLASS_SPACE = "id:Compressed Class Space";

    /* Tags that go with the thread metric */
    private static final String STATE_RUNNABLE = "state:runnable";
    private static final String STATE_BLOCKED = "state:blocked";
    private static final String STATE_WAITING = "state:waiting";
    private static final String STATE_TIMED_WAITING = "state:timed-waiting";
    private static final String STATE_NEW = "state:new";
    private static final String STATE_TERMINATED = "state:terminated";

    /* Tags that go with the metric buffer metric */
    private static final String ID_DIRECT = "id:direct";
    private static final String ID_MAPPED = "id:mapped";

    /* Tags for the Logback events */
    private static final String LEVEL_WARN = "level:warn";
    private static final String LEVEL_DEBUG = "level:debug";
    private static final String LEVEL_ERROR = "level:error";
    private static final String LEVEL_TRACE = "level:trace";
    private static final String LEVEL_INFO = "level:info";

    /* Tags that go with the GC pause metrics */
    private static final String CAUSE = "cause:Metadata GC Threshold";
    private static final String CAUSE_FAILURE = "cause:Allocation Failure";

    private static final String ACTION_MAJOR = "action:end of major GC";
    private static final String ACTION_MINOR = "action:end of minor GC";

    /* Web Target */
    private WebTarget webTarget;

    /**
     * Initializes a new Metrics Extractor REST Client.<br>
     * Once the instance is created, the Client will have a Web Resource that will
     * be the connection point to the server hosting the Metrics Provider REST
     * Service.
     * 
     * @param hostAddr inet address of the host that has the REST service
     * @param portNo   port number were the REST service is hosted on the server
     * @throws IllegalArgumentException if the host address is empty or if the port
     *                                  number is negative
     */
    public MetricsExtractorRestClient(String hostAddr, int portNo) {
        if (hostAddr == null || hostAddr.isEmpty()) {
            throw new IllegalArgumentException("The host address is empty!");
        }
        if (portNo < 0) {
            throw new IllegalArgumentException("Port cannot be a negative number!");
        }
        String uri = PROTOCOL + hostAddr + ":" + portNo;
        webTarget = ClientBuilder.newClient().target(uri);
    }

    /**
     * Sends an HTTP GET request to the REST Service.<br>
     * The response body will be a String that can be parsed into a JsonObject if
     * the response is OK.
     * 
     * @param endpoint endpoint where the resource is
     * @param resource resource we want to retrieve
     * @param tags     tags that the resource has (if any)
     * @return response body of the GET request
     */
    private String sendGetRequest(String endpoint, String resource, String... tags) {
        WebTarget request = webTarget.path(endpoint).path(resource).queryParam("tag", (Object[]) tags);
        Invocation.Builder invocationBuilder = request.request(MediaType.APPLICATION_JSON);
        try {
            return invocationBuilder.get().readEntity(String.class);
        } catch (ProcessingException e) {
            // That resource was not found, so null is returned
            return null;
        }
    }

    /**
     * Sends an HTTP PUT request to the REST service.<br>
     * If the response is OK, the value will have been added to the corresponding
     * custom metric map.
     * 
     * @param endpoint endpoint where the request is sent
     * @param body     request body
     * @throws IllegalArgumentException if the request body causes an error in the
     *                                  server
     */
    private void sendPutRequest(String endpoint, String body) {
        Invocation.Builder invocationBuilder = webTarget.path(endpoint).request(MediaType.APPLICATION_JSON);
        try {
            invocationBuilder.put(Entity.json(body));
        } catch (ProcessingException e) {
            throw new IllegalArgumentException("Error code from the server:  " + e.getMessage());
        }
    }

    /**
     * Sends HTTP DELETE request to the REST service.<br>
     * If the response is OK, the value will have been deleted from the
     * corresponding custom metric map and deregistered from the Meter Registry.
     * 
     * @param endpoint endpoint where the request is sent
     * @param resource resource we want to delete
     * @throws IllegalStateException if the deletion is not possible at the
     *                               requested moment, returning an error code from
     *                               the server
     */
    private void sendDeleteRequest(String endpoint, String resource) {
        Invocation.Builder invocationBuilder = webTarget.path(endpoint).path(resource).request(
            MediaType.APPLICATION_JSON);
        try {
            invocationBuilder.delete();
        } catch (ProcessingException e) {
            throw new IllegalStateException("Error code from the server:  " + e.getMessage());
        }
    }

    /**
     * Retrieves the number of buffers from the JVM.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the number of buffers
     *         from the JVM sorted by id
     */
    protected String getJvmBufferCount() {
        return getBufferMetric("jvm.buffer.count");
    }

    /**
     * Retrieves the amount of buffer memory used by the JVM.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the buffer memory used
     *         by the JVM sorted by id
     */
    protected String getJvmBufferMemoryUsed() {
        return getBufferMetric("jvm.buffer.memory.used");
    }

    /**
     * Retrieves the total capacity of the buffers from the JVM.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the total buffer
     *         capacity of the JVM sorted by id
     */
    protected String getJvmBufferTotalCapacity() {
        return getBufferMetric("jvm.buffer.memory.used");
    }

    /**
     * Retrieves the number of classes loaded in the resource being monitored.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of loaded classes
     */
    protected String getJvmClassesLoaded() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.classes.loaded");
    }

    /**
     * Retrieves the number of classes that has been unloaded from the JVM since
     * execution started.<br>
     * The metric received is of type {@code MeterType#COUNTER}.
     * 
     * @return a JsonObject representing the Counter of unloaded classes
     */
    protected String getJvmClassesUnloaded() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.classes.unloaded");
    }

    /**
     * Retrieves the live data size of the garbage collector from the process being
     * monitored.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of GC live data size
     */
    protected String getJvmGcLiveDataSize() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.gc.live.data.size");
    }

    /**
     * Retrieves the max data size of the garbage collector from the process being
     * monitored.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of GC max data size
     */
    protected String getJvmGcMaxDataSize() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.gc.max.data.size");
    }

    /**
     * Retrieves the allocated memory of the garbage collector from the process
     * being monitored.<br>
     * The metric received is of type {@code MeterType#COUNTER}.
     * 
     * @return a JsonObject representing the Counter of GC allocated memory
     */
    protected String getJvmGcMemoryAllocated() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.gc.memory.allocated");
    }

    /**
     * Retrieves the amount the memory of the garbage collector from the process
     * being monitored has been promoted.<br>
     * The metric received is of type {@code MeterType#COUNTER}.
     * 
     * @return a JsonObject representing the Counter of GC promoted memory
     */
    protected String getJvmGcMemoryPromoted() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.gc.memory.promoted");
    }

    /**
     * Retrieves the timers for the Garbage Collector pause sorted by cause and
     * action.<br>
     * The metric received is of type {@code MeterType#TIMER}. The Timers will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Timers of the GC pause
     */
    protected String getJvmGcPause() {
        final String name = "jvm.gc.pause";
        StringBuilder sb = new StringBuilder();

        String aux = sendGetRequest(TAGGED_ENDPOINT, name, CAUSE, ACTION_MAJOR);

        if (aux == null) {
            sb.append("[");
            sb.append("{\"tags\":[\"").append(CAUSE).append("\",\"").append(ACTION_MAJOR).append("\"],");
            sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, CAUSE_FAILURE, ACTION_MINOR))
                    .append("}");
            sb.append("]");
        } else {
            sb.append("[");
            sb.append("{\"tags\":[\"").append(CAUSE).append("\",\"").append(ACTION_MAJOR).append("\"],");
            sb.append("\"meter\":").append(aux).append("},");
            sb.append("{\"tags\":[\"").append(CAUSE).append("\",\"").append(ACTION_MINOR).append("\"],");
            sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, CAUSE, ACTION_MINOR)).append("}");
            sb.append("]");
        }

        return sb.toString();
    }

    /**
     * Retrieves the amount of committed memory from the JVM.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the amount of commited
     *         JVM memory sorted by area and id
     */
    protected String getJvmMemoryCommited() {
        return getMemoryMetric("jvm.memory.committed");
    }

    /**
     * Retrieves the max memory from the JVM.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the max JVM memory
     *         sorted by area and id
     */
    protected String getJvmMemoryMax() {
        return getMemoryMetric("jvm.memory.max");
    }

    /**
     * Retrieves the amount of used memory from the JVM.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the amount of used JVM
     *         memory sorted by area and id
     */
    protected String getJvmMemoryUsed() {
        return getMemoryMetric("jvm.memory.used");
    }

    /**
     * Retrieves the number of Daemon threads running in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of Daemon threads
     */
    protected String getJvmThreadsDaemon() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.threads.daemon");
    }

    /**
     * Retrieves the number of live threads running in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of live threads
     */
    protected String getJvmThreadsLive() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.threads.live");
    }

    /**
     * Retrieves the peak number of threads running in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of peak threads
     */
    protected String getJvmThreadsPeak() {
        return sendGetRequest(SIMPLE_ENDPOINT, "jvm.threads.peak");
    }

    /**
     * Retrieves the number of threads running in the resource sorted by state.<br>
     * The metric received is of type {@code MeterType#GAUGE}. The Gauges will be
     * arranged in a JsonArray of JsonObjects. Said objects have a first attribute
     * indicating the used tags and the second attribute will have the JsonObject
     * that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Gauges of the number of threads
     *         sorted by state
     */
    protected String getJvmThreadsStates() {
        final String name = "jvm.threads.states";
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append("{\"tags\":[\"").append(STATE_RUNNABLE).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, STATE_RUNNABLE)).append("},");
        sb.append("{\"tags\":[\"").append(STATE_BLOCKED).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, STATE_BLOCKED)).append("},");
        sb.append("{\"tags\":[\"").append(STATE_WAITING).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, STATE_WAITING)).append("},");
        sb.append("{\"tags\":[\"").append(STATE_TIMED_WAITING).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, STATE_TIMED_WAITING)).append("},");
        sb.append("{\"tags\":[\"").append(STATE_NEW).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, STATE_NEW)).append("},");
        sb.append("{\"tags\":[\"").append(STATE_TERMINATED).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, STATE_TERMINATED)).append("}");
        sb.append("]");

        return sb.toString();
    }

    /**
     * Retrieves the count of logback events from the resource sorted by level.<br>
     * The metric received is of type {@code MeterType#COUNTER}. The Counters will
     * be arranged in a JsonArray of JsonObjects. Said objects have a first
     * attribute indicating the used tags and the second attribute will have the
     * JsonObject that was retrieved from the REST service.
     * 
     * @return a JsonArray representing the set of Counters of the count of logback
     *         events sorted by level
     */
    protected String getLogbackEvents() {
        final String name = "logback.events";
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append("{\"tags\":[\"").append(LEVEL_WARN).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, LEVEL_WARN)).append("},");
        sb.append("{\"tags\":[\"").append(LEVEL_DEBUG).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, LEVEL_DEBUG)).append("},");
        sb.append("{\"tags\":[\"").append(LEVEL_ERROR).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, LEVEL_ERROR)).append("},");
        sb.append("{\"tags\":[\"").append(LEVEL_TRACE).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, LEVEL_TRACE)).append("},");
        sb.append("{\"tags\":[\"").append(LEVEL_INFO).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, LEVEL_INFO)).append("}");
        sb.append("]");

        return sb.toString();
    }

    /**
     * Retrieves percentage of the CPU that the process is using.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of process CPU usage
     */
    protected String getProcessCpuUsage() {
        return sendGetRequest(SIMPLE_ENDPOINT, "process.cpu.usage");
    }

    /**
     * Retrieves the start time of the process being monitored.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of process start time
     */
    protected String getProcessStartTime() {
        return sendGetRequest(SIMPLE_ENDPOINT, "process.start.time");
    }

    /**
     * Retrieves the total time the process being monitored has been running.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of process uptime
     */
    protected String getProcessUptime() {
        return sendGetRequest(SIMPLE_ENDPOINT, "process.uptime");
    }

    /**
     * Retrieves the number of CPUs running in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of CPU count
     */
    protected String getSystemCpuCount() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.cpu.count");
    }

    /**
     * Retrieves percentage of usage of the CPU running in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of system CPU usage
     */
    protected String getSystemCpuUsage() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.cpu.usage");
    }

    /**
     * Retrieves the free disk capacity in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of free disk space
     */
    protected String getSystemDiskFree() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.disk.free");
    }

    /**
     * Retrieves the total disk capacity in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of total disk space
     */
    protected String getSystemDiskTotal() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.disk.total");
    }

    /**
     * Retrieves the usable disk capacity in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of usable disk space
     */
    protected String getSystemDiskUsable() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.disk.usable");
    }

    /**
     * Retrieves the used disk capacity in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of used disk space
     */
    protected String getSystemsDiskUsed() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.disk.used");
    }

    /**
     * Retrieves the amount of free physical memory in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of free physical memory
     */
    protected String getSystemMemoryFree() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.memory.free");
    }

    /**
     * Retrieves the total amount of physical memory in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of total physical memory
     */
    protected String getSystemMemoryTotal() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.memory.total");
    }

    /**
     * Retrieves the percentage of usage of physical memory in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of usage of physical memory
     */
    protected String getSystemMemoryUsage() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.memory.usage");
    }

    /**
     * Retrieves the amount of used physical memory in the resource.<br>
     * The metric received is of type {@code MeterType#GAUGE}.
     * 
     * @return a JsonObject representing the Gauge of used physical memory
     */
    protected String getSystemMemoryUsed() {
        return sendGetRequest(SIMPLE_ENDPOINT, "system.memory.used");
    }

    /**
     * Retrieves a custom gauge.<br>
     * All the metrics received are of type {@code MeterType#GAUGE}.
     * 
     * @param name URN of the custom gauge
     * @return a JsonObject representing the requested Custom Gauge
     */
    protected String getCustomGauge(String name) {
        return sendGetRequest(GAUGES_ENDPOINT, name);
    }

    /**
     * Updates a Custom Gauge using the REST service.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * that will represent the body of the HTTP PUT request. Said body corresponds
     * to the JsonObject provided by {@link de.iip_ecosphere.platform.services.environment.metricsprovider.clientside.
     * meterrepresentation.GaugeRepresentation#getUpdater()}
     * 
     * @param args object array containing the body of the PUT request
     * @return NULL, nothing is returned
     * @throws IllegalArgumentException if the number of arguments is not correct or
     *                                  if the argument is null
     */
    protected Object updateCustomGauge(Object[] args) {
        String body = String.valueOf(args[0]);
        sendPutRequest(GAUGES_ENDPOINT, body);

        // Nothing is returned
        return null;
    }

    /**
     * Deletes a Custom Gauge using the REST service.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * that will represent the resource URN of the HTTP DELETE request. Said
     * resource URN corresponds to the name that was given to the custom Gauge.
     * 
     * @param args object array containing the resource URN for the DELETE request
     * @return NULL, nothing is returned
     */
    protected Object deleteCustomGauge(Object[] args) {
        String resource = String.valueOf(args[0]);
        sendDeleteRequest(GAUGES_ENDPOINT, resource);

        // Nothing is returned
        return null;
    }

    /**
     * Retrieves a custom counter.<br>
     * All the metrics received are of type {@code MeterType#COUNTER}.
     * 
     * @param name URN of the custom counter
     * @return a JsonObject representing the requested Custom Counter
     */
    protected String getCustomCounter(String name) {
        return sendGetRequest(COUNTERS_ENDPOINT, name);
    }

    /**
     * Updates a Custom Counter using the REST service.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * that will represent the body of the HTTP PUT request. Said body corresponds
     * to the JsonObject provided by {@link de.iip_ecosphere.platform.services.environment.metricsprovider.
     * clientside.meterrepresentation.CounterRepresentation#getUpdater()}
     * 
     * @param args object array containing the body of the PUT request
     * @return NULL, nothing is returned
     */
    protected Object updateCustomCounter(Object[] args) {
        String body = String.valueOf(args[0]);
        sendPutRequest(COUNTERS_ENDPOINT, body);

        // Nothing is returned
        return null;
    }

    /**
     * Deletes a Custom Counter using the REST service.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * that will represent the resource URN of the HTTP DELETE request. Said
     * resource URN corresponds to the name that was given to the custom Counter.
     * 
     * @param args object array containing the resource URN for the DELETE request
     * @return NULL, nothing is returned
     */
    protected Object deleteCustomCounter(Object[] args) {
        String resource = String.valueOf(args[0]);
        sendDeleteRequest(COUNTERS_ENDPOINT, resource);

        // Nothing is returned
        return null;
    }

    /**
     * Retrieves a custom timer.<br>
     * All the metrics received are of type {@code MeterType#TIMER}.
     * 
     * @param name URN of the custom gauge
     * @return a JsonObject representing the requested Custom Timer
     */
    protected String getCustomTimer(String name) {
        return sendGetRequest(TIMERS_ENDPOINT, name);
    }

    /**
     * Updates a Custom Timer using the REST service.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * that will represent the body of the HTTP PUT request. Said body corresponds
     * to the JsonObject provided by {@link de.iip_ecosphere.platform.services.environment.metricsprovider.clientside.
     * meterrepresentation.TimerRepresentation#getUpdater()}
     * 
     * @param args object array containing the body of the PUT request
     * @return NULL, nothing is returned
     */
    protected Object updateCustomTimer(Object[] args) {
        String body = String.valueOf(args[0]);
        sendPutRequest(TIMERS_ENDPOINT, body);

        // Nothing is returned
        return null;
    }

    /**
     * Deletes a Custom Timer using the REST service.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * that will represent the resource URN of the HTTP DELETE request. Said
     * resource URN corresponds to the name that was given to the custom Timer.
     * 
     * @param args object array containing the resource URN for the DELETE request
     * @return NULL, nothing is returned
     */
    protected Object deleteCustomTimer(Object[] args) {
        String resource = String.valueOf(args[0]);
        sendDeleteRequest(TIMERS_ENDPOINT, resource);

        // Nothing is returned
        return null;
    }

    /**
     * Retrieves a memory metric from the REST service.<br>
     * The memory metrics exposed by Micrometer use the same tags, so this method is
     * created to avoid code repetition.
     * 
     * @param name name of the memory metric
     * @return the JsonArray with the requested metrics
     */
    private String getMemoryMetric(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append("{\"tags\":[\"").append(AREA_HEAP).append("\",\"").append(ID_SURVIVOR_SPACE).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, AREA_HEAP, ID_SURVIVOR_SPACE))
                .append("},");
        sb.append("{\"tags\":[\"").append(AREA_HEAP).append("\",\"").append(ID_OLD_GEN).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, AREA_HEAP, ID_OLD_GEN)).append("},");
        sb.append("{\"tags\":[\"").append(AREA_HEAP).append("\",\"").append(ID_EDEN_SPACE).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, AREA_HEAP, ID_EDEN_SPACE)).append("},");

        sb.append("{\"tags\":[\"").append(AREA_NONHEAP).append("\",\"").append(ID_METASPACE).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, AREA_NONHEAP, ID_METASPACE)).append("},");
        sb.append("{\"tags\":[\"").append(AREA_NONHEAP).append("\",\"").append(ID_CODE_CACHE).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, AREA_NONHEAP, ID_CODE_CACHE)).append("},");
        sb.append("{\"tags\":[\"").append(AREA_NONHEAP).append("\",\"").append(ID_COMPRESSED_CLASS_SPACE)
                .append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, AREA_NONHEAP, ID_COMPRESSED_CLASS_SPACE))
                .append("}");
        sb.append("]");

        return sb.toString();
    }

    /**
     * Retrieves a buffer metric from the REST service.<br>
     * The buffer metrics exposed by Micrometer use the same tags, so this method is
     * created to avoid code repetition.
     * 
     * @param name name of the buffer metric
     * @return the JsonArray with the requested metrics
     */
    private String getBufferMetric(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append("{\"tags\":[\"").append(ID_DIRECT).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, ID_DIRECT)).append("},");
        sb.append("{\"tags\":[\"").append(ID_MAPPED).append("\"],");
        sb.append("\"meter\":").append(sendGetRequest(TAGGED_ENDPOINT, name, ID_MAPPED)).append("}");
        sb.append("]");

        return sb.toString();
    }

    /**
     * Updates the memory base unit used for the system physical memory metrics.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * which is a JSON object that contains the new unit. For example, to set the
     * base unit to kilobytes, we send a JsonObject like this one:
     * 
     * <pre>
     * {
     *     "unit":"kilobytes"
     * }
     * </pre>
     * 
     * @param args object array containing the body of the PUT request
     * @return NULL, nothing is returned
     */
    protected Object setMemoryBaseUnit(Object[] args) {
        String body = String.valueOf(args[0]);
        sendPutRequest(CONFIG_MEMORY_BASE_UNIT, body);

        // Nothing is returned
        return null;
    }

    /**
     * Updates the capacity base unit used for the system disk capacity metrics.<br>
     * This method is "forced" to follow the AAS signature, so there are some
     * constraints. This method expects a single argument of type {@link String}
     * which is a JSON object that contains the new unit. For example, to set the
     * base unit to kilobytes, we send a JsonObject like this one:
     * 
     * <pre>
     * {
     *     "unit":"kilobytes"
     * }
     * </pre>
     * 
     * @param args object array containing the body of the PUT request
     * @return NULL, nothing is returned
     */
    protected Object setDiskBaseUnit(Object[] args) {
        String body = String.valueOf(args[0]);
        sendPutRequest(CONFIG_DISK_BASE_UNIT, body);

        // Nothing is returned
        return null;
    }

    /**
     * Retrieves the list of custom gauges registered in the Metrics Provider.
     * 
     * @return a JsonArray representing a list with the names of all the custom
     *         gauges
     */
    protected String getGaugeList() {
        return sendGetRequest(GAUGES_ENDPOINT, "");
    }

    /**
     * Retrieves the list of custom counters registered in the Metrics Provider.
     * 
     * @return a JsonArray representing a list with the names of all the custom
     *         counters
     */
    protected String getCounterList() {
        return sendGetRequest(COUNTERS_ENDPOINT, "");
    }

    /**
     * Retrieves the list of custom timers registered in the Metrics Provider.
     * 
     * @return a JsonArray representing a list with the names of all the custom
     *         timers
     */
    protected String getTimerList() {
        return sendGetRequest(TIMERS_ENDPOINT, "");
    }

    /**
     * Retrieves the list of tagged meters registered in the Metrics Provider.
     * 
     * @return a JsonArray representing a list with the names of relevant tagged
     *         meters in the meter registry
     */
    protected String getTaggedMeterList() {
        return sendGetRequest(TAGGED_ENDPOINT, "");
    }

    /**
     * Retrieves the list of non-tagged meters registered in the Metrics Provider.
     * 
     * @return a JsonArray representing a list with the names of relevant non-tagged
     *         meters in the meter registry
     */
    protected String getSimpleMeterList() {
        return sendGetRequest(SIMPLE_ENDPOINT, "");
    }

}
