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

package de.iip_ecosphere.platform.services.environment.spring.metricsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.services.environment.metricsProvider.CapacityBaseUnit;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;

/**
 * This class provides a RESTful Service to access the {@link MetricsProvider}
 * instance running in the Spring Service. <br>
 * The idea behind this RESTful Service is to continue down the line of a
 * uniform HTTP communication between AAS and the MetricsProvider that would
 * allow the AAS to update and delete values from the provider remotely. This
 * can be used to unify the custom metrics from services running elsewhere into
 * a single MetricsProvider instance.<br>
 * The services offered by this class are:
 * <ul>
 * <li>Update a custom gauge</li>
 * <li>Increase a custom counter</li>
 * <li>Record time events with a custom timer</li>
 * <li>Delete a custom metric</li>
 * <li>Change the memory base unit for the physical memory metrics</li>
 * <li>Change the disk capacity base unit for the physical memory metrics</li>
 * </ul>
 * Needless to say, only custom metrics can be modified. System metrics cannot
 * be changed as they are directly extracted from the system.
 * 
 * @author Miguel Gomez
 */
@RestController
public class MetricsProviderRestService {

    private final MetricsProvider metricsProvider;

    /**
     * Creates a new RestMetricsUpdater instance.<br>
     * This method should be called by the Spring Boot Application that will inject
     * the {@link MetricsProvider} instance running in the service.
     * 
     * @param metricsProvider metrics provider instance of the service
     */
    public MetricsProviderRestService(MetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    /**
     * Retrieves a list of all the custom gauges present in the Metrics Provider.
     * 
     * @return a JSON Array with a list of all the custom gauges registered
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/gauges")
    public String getCustomGaugeList() {
        return metricsProvider.getCustomGaugeList();
    }

    /**
     * Updates a custom gauge value.<br>
     * The request body is expected to have plain text with two values separated by
     * a comma, being the first value the URN (or name) of the gauge we want to
     * modify and the second the value we want the gauge to have.<br>
     * Similar to the implementation of
     * {@link MetricsProvider#addGaugeValue(String, double)}, if there is no gauge
     * with the indicated URN, said gauge is created. Otherwise, the gauge is
     * retrieved and the value is updated.<br>
     * Following the REST standards, it is indicated that the entity being updated
     * is, technically, the Gauge map, which involves creating or updating a custom
     * gauge within.
     * 
     * @param body request body containing the CSV representing the Gauge update
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/gauges")
    public void putGaugeValue(@RequestBody String body) {
        Map<String, String> json = parseCounterdGaugeAndConfig(body);
        metricsProvider.addGaugeValue(json.get("name"), Double.valueOf(json.get("value")));
    }

    /**
     * Deletes a custom gauge.<br>
     * The custom gauge is deleted and removed from the registry, similar to the
     * execution of {@link MetricsProvider#removeGauge(String)}.
     * 
     * @param name URN of the gauge we want to remove
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/gauges/{name}")
    public void deleteGauge(@PathVariable String name) {
        metricsProvider.removeGauge(name);
    }

    /**
     * Retrieves a list of all the custom counters present in the Metrics Provider.
     * 
     * @return a JSON Array with a list of all the custom counters registered
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/counters")
    public String getCustomCounterList() {
        return metricsProvider.getCustomCounterList();
    }

    /**
     * Increases a custom counter value.<br>
     * The request body is expected to have plain text with two values separated by
     * a comma, being the first value the URN (or name) of the counter we want to
     * modify and the second the value by which we want to increase the counter.<br>
     * Similar to the implementation of
     * {@link MetricsProvider#increaseCounterBy(String, double)}, if there is no
     * counter with the indicated URN, said counter is created. Otherwise, the
     * counter is retrieved and the value is incremented.<br>
     * Following the REST standards, it is indicated that the entity being updated
     * is, technically, the Counter map, which involves creating or updating a
     * custom counter within.
     * 
     * @param body request body containing the CSV representing the Counter update
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/counters")
    public void putCounterValue(@RequestBody String body) {
        Map<String, String> json = parseCounterdGaugeAndConfig(body);
        metricsProvider.increaseCounterBy(json.get("name"), Double.valueOf(json.get("increment")));
    }

    /**
     * Deletes a custom counter.<br>
     * The custom counter is deleted and removed from the registry, similar to the
     * execution of {@link MetricsProvider#removeCounter(String)}.
     * 
     * @param name URN of the counter we want to remove
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/counters/{name}")
    public void deleteCounter(@PathVariable String name) {
        metricsProvider.removeCounter(name);
    }

    /**
     * Retrieves a list of all the custom timers present in the Metrics Provider.
     * 
     * @return a JSON Array with a list of all the custom timers registered
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/timers")
    public String getTimerCounterList() {
        return metricsProvider.getCustomTimerList();
    }

    /**
     * Increases a custom timer value.<br>
     * The request body is expected to have plain text with multiple values
     * separated by commas, being the first value the URN (or name) of the timer we
     * want to modify and the remaining values the amounts of time in nanoseconds
     * that we want to record with the timer.<br>
     * Similar to the implementation of
     * {@link MetricsProvider#recordWithTimer(String, long, TimeUnit)}, if there is
     * no timer with the indicated URN, said timer is created. Otherwise, the timer
     * is retrieved and the values are recorded.<br>
     * Following the REST standards, it is indicated that the entity being updated
     * is, technically, the Timer map, which involves creating or updating a custom
     * timer within.
     * 
     * @param body request body containing the CSV representing the Timer update
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/timers")
    public void putTimerValue(@RequestBody String body) {
        Map<String, Object> json = parseTimer(body);
        String name = (String) json.get("name");
        long[] recordings = (long[]) json.get("recordings");
        for (int i = 0; i < recordings.length; i++) {
            metricsProvider.recordWithTimer(name, recordings[i], TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Deletes a custom timer.<br>
     * The custom timer is deleted and removed from the registry, similar to the
     * execution of {@link MetricsProvider#removeTimer(String)}.
     * 
     * @param name URN of the timer we want to remove
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/timers/{name}")
    public void deleteTimer(@PathVariable String name) {
        metricsProvider.removeTimer(name);
    }

    /**
     * Changes the base unit for the physical memory metrics of the system.<br>
     * The body will contain a single string that has a valid
     * {@link CapacityBaseUnit}. Maintaining the implementation used for the
     * {@code application.yml} file, this is not letter case sensitive and the
     * String will be accepted regardless of lower-case and upper-case values.
     * 
     * @param body request body containing the String representing the value we want
     *             to set as memory base unit
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/config/memory-base-unit")
    public void changeMemoryBaseUnit(@RequestBody String body) {
        Map<String, String> json = parseCounterdGaugeAndConfig(body);
        metricsProvider.setMemoryBaseUnit(CapacityBaseUnit.valueOf(json.get("unit").toUpperCase()));
        metricsProvider.removeMemoryMetrics();
        metricsProvider.registerMemoryMetrics();
    }

    /**
     * Changes the base unit for the disk capacity metrics of the system.<br>
     * The body will contain a single string that has a valid
     * {@link CapacityBaseUnit}. Maintaining the implementation used for the
     * {@code application.yml} file, this is not letter case sensitive and the
     * String will be accepted regardless of lower-case and upper-case values.
     * 
     * @param body request body containing the String representing the value we want
     *             to set as disk capacity base unit
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/config/disk-base-unit")
    public void changeDiskBaseUnit(@RequestBody String body) {
        Map<String, String> json = parseCounterdGaugeAndConfig(body);
        metricsProvider.setDiskBaseUnit(CapacityBaseUnit.valueOf(json.get("unit").toUpperCase()));
        metricsProvider.removeDiskMetrics();
        metricsProvider.registerDiskMetrics();
    }

    /**
     * Provides a list of all tagged meters registered.<br>
     * All the tagged meters are meters extracted by micrometer and cannot be
     * modified.
     * 
     * @return list of the tagged micrometer meters
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/tagged-meter")
    public String getTaggedMeterList() {
        return metricsProvider.getTaggedMeterList();
    }

    /**
     * Retrieves a meter with tags.<br>
     * As some of the system meters have tags, this method will allow said metrics
     * to be retrieved using the appropriate tags.
     * 
     * @param name   name of the meter
     * @param params tags of the meter
     * @return requested meter in JSON format
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/tagged-meter/{name}")
    public String getMeter(@PathVariable String name,
            @RequestParam(required = false) MultiValueMap<String, String> params) {

        List<Tag> tags = new ArrayList<Tag>();
        String[] splt;
        for (String tag : params.get("tag")) {
            splt = tag.split(":");
            tags.add(new ImmutableTag(splt[0], splt[1]));
        }

        return getMeter(name, tags);
    }

    /**
     * Provides a list of all simple meters registered.<br>
     * All the simple meters are meters extracted by micrometer and cannot be
     * modified.
     * 
     * @return list of the simple micrometer meters
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/simple-meter")
    public String getSimpleMeterList() {
        return metricsProvider.getSimpleMeterList();
    }

    /**
     * Retrieves a meter with no tags.<br>
     * Most of the meters do not have tags, so this method will be used to retrieve
     * most metrics. This method will also retrieve the first value found in the
     * registry if we request a tagged metrics without specifying the tags.
     * 
     * @param name name of the meter
     * @return requested meter in JSON format
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/simple-meter/{name}")
    public String getMeter(@PathVariable String name) {
        return getMeter(name, new ArrayList<Tag>());
    }

    /**
     * Retrieves a JSON Object representation of the meter from the MetricsProvider.
     * 
     * @param name of the meter
     * @param list of tags
     * @return JsonObject representation of the meter
     */
    private String getMeter(String name, List<Tag> list) {
        return metricsProvider.getMeter(name, list);
    }

    /**
     * Retrieves a custom gauge.
     * 
     * @param name of the gauge
     * @return JsonObject representation of the Gauge.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/gauges/{name}")
    public String getGauge(@PathVariable String name) {
        return metricsProvider.getGauge(name);
    }

    /**
     * Retrieves a custom counter.
     * 
     * @param name of the counter
     * @return JsonObject representation of the counter.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/counters/{name}")
    public String getCounter(@PathVariable String name) {
        return metricsProvider.getCounter(name);
    }

    /**
     * Retrieves a custom timer.
     * 
     * @param name of the timer
     * @return JsonObject representation of the timer.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/timers/{name}")
    public String getTimer(@PathVariable String name) {
        return metricsProvider.getTimer(name);
    }

    /**
     * Parses a Json Object into a Map to extract the values.<br>
     * Quick fix to the Json Part not working. This Method parses Counters, Gauges
     * and configuration updaters.
     * 
     * @param json String representing a JsonObject
     * @return JsonObject mapped as a Map
     */
    private Map<String, String> parseCounterdGaugeAndConfig(String json) {
        Map<String, String> mappedJson = new HashMap<String, String>();
        String s = json.substring(1, json.length() - 1).replace("\"", "");
        String[] split = s.split(":|,");

        for (int i = 0; i < split.length; i += 2) {
            mappedJson.put(split[i], split[i + 1]);
        }

        return mappedJson;
    }

    /**
     * Parses a Json Object into a Map to extract the values.<br>
     * Quick fix to the Json Part not working. This Method parses Timers.
     * 
     * @param json String representing a JsonObject
     * @return JsonObject mapped as a Map
     */
    private Map<String, Object> parseTimer(String json) {
        Map<String, Object> mappedJson = new HashMap<String, Object>();
        String s = json.substring(1, json.length() - 1).replace("\"", "");
        String[] split = s.split(":");

        mappedJson.put(split[0], split[1].split(",")[0]);

        String[] arr = split[2].substring(1, split[2].length() - 1).split(",");
        long[] recordings = new long[arr.length];

        for (int i = 0; i < arr.length; i++) {
            recordings[i] = Long.valueOf(arr[i]);
        }

        mappedJson.put(split[1].split(",")[1], recordings);

        return mappedJson;
    }

}
