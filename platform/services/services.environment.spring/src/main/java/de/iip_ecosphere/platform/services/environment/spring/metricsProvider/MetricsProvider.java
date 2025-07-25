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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PreDestroy;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import de.iip_ecosphere.platform.services.environment.metricsProvider.CapacityBaseUnit;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor
    .CollectionSupplier;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor
    .PushMeterPredicate;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * This class represents an interface to manage the Micrometer-API meters.<br>
 * An instance of this class is auto-wired into the Stream Application and will
 * act as a metrics provider for the service application.<br>
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
 * All meters will be accessible via HTTP requests through the endpoint provided
 * by the Spring Boot Application by adding the resource names as final part of
 * the URI. If the requested metric has tags, they are passed as query
 * parameters: {@code 
 * http://192.168.1.111:8080/actuator/metrics/my.metric?tag=tagKey:tagValue&tag=anotherTagKey:anotherTagValue}<br>
 * There are a few properties that can be modified using the application.yml
 * file. These properties are:
 * <ul>
 * <li>schedulerrate: the rate at which the system metrics are gathered to
 * reduce gauge calculation overload</li>
 * <li>memorybaseunit: the base unit we want to use for the physical memory
 * metrics</li>
 * <li>diskbaseunit: the base unit we want to use for the disk capacity
 * metrics</li>
 * </ul>
 * It is important to note that all previously configurable properties have a
 * default value, so there is no need obligation to set them in the file and
 * that the names given to the base units must correspond a valid value from
 * {@link CapacityBaseUnit#values()}. In order to comply with flexibility and
 * YAML, the names are not case sensitive and will correctly parse to their
 * equivalent values independently of upper or lower case letters.
 * 
 * @author Miguel Gomez
 */
@Component
@ConfigurationProperties(prefix = "metricsprovider")
public class MetricsProvider extends de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider {

    // Configurable data from the application.yml file

    /* By default, the scheduled task runs every 2 seconds */
    private static final String SCHEDULE_RATE = "${metricsprovider.schedulerrate:2000}";
    
    /**
     * Supplies all collection elements that match the {@code deviceId}.
     */
    private static final CollectionSupplier ALL_ELEMENTS_SUPPLIER = new CollectionSupplier() {

        @Override
        public List<ElementsAccess> get(ElementsAccess parent, String deviceId) {
            List<ElementsAccess> result = null;
            ElementsAccess coll = parent.getSubmodelElementCollection(
                AasUtils.fixId(AasPartRegistry.NAME_COLLECTION_SERVICES));
            if (coll instanceof SubmodelElementCollection) { // find id within collection
                SubmodelElementCollection sec = (SubmodelElementCollection) coll;
                result = new ArrayList<ElementsAccess>();
                String fDeviceId = AasUtils.fixId(deviceId);
                for (SubmodelElement e : sec.elements()) {
                    if (e instanceof ElementsAccess) {
                        ElementsAccess ea = (ElementsAccess) e;
                        Property resource = ea.getProperty("resource");
                        if (null != resource) {
                            try {
                                if (fDeviceId.equals(resource.getValue())) {
                                    result.add(ea);
                                }
                            } catch (ExecutionException ex) {
                                LoggerFactory.getLogger(MetricsProvider.class).warn(
                                    "Cannot read resource property: {}", ex.getMessage());
                            }
                        }
                        
                    }
                }
            }
            return result;
        }
        
    };
    
    /**
     * Tag matching predicate. Pre-matched deviceIds assumed.
     */
    private static final PushMeterPredicate TAG_PREDICATE = new PushMeterPredicate() {
        
        @Override
        public boolean test(ElementsAccess parent, JsonValue meter) {
            boolean allowPush = true;
            JsonObject m = meter.asJsonObject();
            JsonArray tags = m.getJsonArray("availableTags");
            if (null != tags && !tags.isEmpty()) {
                Property prop = parent.getProperty("id");
                if (null != prop) {
                    try {
                        String tag = TAG_SERVICE_SERVICE + ":" + prop.getValue();
                        allowPush = false;
                        for (int i = 0; !allowPush && i < tags.size(); i++) {
                            allowPush = tags.getString(i).equals(tag);
                        }
                    } catch (ExecutionException e) {
                        LoggerFactory.getLogger(MetricsProvider.class).warn(
                            "Cannot read id property: {}", e.getMessage());
                    }
                }
            } 
            return allowPush;
        }
    };
    
    
    /* By default, the base unit for the memory metrics is bytes */
    @Value("${metricsprovider.memorybaseunit:bytes}")
    private String memoryBaseUnitString;

    /* By default the base unit for disk capacity is kilobytes */
    @Value("${metricsprovider.diskbaseunit:kilobytes}")
    private String diskBaseUnitString;
    private boolean update = false;
    private TransportConnector connector;
    private boolean connectorFailed;
    @Autowired
    private TransportSetup transport;

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
     // with lambda, non-native monitoring data will be grabbed from Transport
        super(registry, !MetricsAasConstructor.LAMBDA_SETTERS_SUPPORTED); 
    }
    
    /**
     * Sets the basic values that are usually injected. [testing outside Spring]
     * 
     * @param transport the transport instance
     */
    public void setInjectedValues(TransportSetup transport) {
        this.transport = transport;
        diskBaseUnitString = "kilobytes";
        memoryBaseUnitString = "bytes";
    }

    /**
     * Registers the extra system metrics onto the registry.<br>
     * The extra system metrics include the physical memory values and the disk
     * values, which are metrics not automatically recorded by Micrometer-API.
     */
    public void registerNonNativeSystemMetrics() {
        setMemoryBaseUnit(CapacityBaseUnit.valueOf(memoryBaseUnitString.toUpperCase()));
        setDiskBaseUnit(CapacityBaseUnit.valueOf(diskBaseUnitString.toUpperCase()));
        super.registerNonNativeSystemMetrics();
    }

    /**
     * This operation calculates the values for the extra system metrics not exposed
     * by Micrometer-API.<br>
     * Even though this sacrifices the real time values of these metrics, we gain
     * speed when requesting the metrics as we no longer have to calculate the
     * values upon request. The {@code SCHEDULE_RATE} indicates the time in between
     * calculations. [public for testing outside spring]
     */
    @Scheduled(fixedRateString = SCHEDULE_RATE)
    @Override
    public void calculateMetrics() {
        super.calculateMetrics();
        final String id = Id.getDeviceId();
        if (null == connector && null != transport) {
            try {
                connector = TransportFactory.createConnector();
                connector.connect(transport.createParameter());
            } catch (IOException e) {
                LoggerFactory.getLogger(MetricsProvider.class).error(
                    "Cannot create transport connector: {}", e.getMessage());
                connectorFailed = true;
            }
        }
        String json = toJson(id, update, DEFAULT_METER_FILTERS); 
        if (null != connector && !connectorFailed) {
            try {
                connector.asyncSend(MetricsAasConstants.TRANSPORT_SERVICE_METRICS_CHANNEL, json);
            } catch (IOException e) {
                LoggerFactory.getLogger(MetricsProvider.class).error(
                    "Cannot sent monitoring message: {}", e.getMessage());
            }
            update = true;
        }
        MetricsAasConstructor.pushToAas(json, AasPartRegistry.NAME_SUBMODEL_SERVICES, 
            ALL_ELEMENTS_SUPPLIER, update, TAG_PREDICATE);
    }
    
    /**
     * Clean up at shutdown.
     */
    @PreDestroy
    public void destroy() {
        MetricsAasConstructor.clear();
        if (null != connector && !connectorFailed) {
            try {
                connector.disconnect();
            } catch (IOException e) {
            }
        }
    }
    
}
