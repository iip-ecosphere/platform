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
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.Irdi;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

import static de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants.*;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
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

    /**
     * Default supplier for the submodel identified by the given id.
     */
    public static final CollectionSupplier DFLT_SUBMODEL_SUPPLIER = 
        (p, id) -> CollectionUtils.toList(p.getSubmodelElementCollection(AasUtils.fixId(id)));
        
    /**
     * Default push meter predicate which always returns true.
     */
    public static final PushMeterPredicate PREDICATE_ALWAYS_TRUE = 
        (p, j) -> true;
    
    /**
     * Does the underlying AAS implementation execute Lambda-Setters for AAS properties. If not, we activate
     * a less performant fallback. Somewhen between BaSyx 1.1 and BaSyx 1.3 we lost this, but this may just depend
     * on the underlying Java version.
     */
    public static final boolean LAMBDA_SETTERS_SUPPORTED = false; 

    private static Map<String, TransportConnector> conns = new HashMap<>();
    private static Map<String, JsonObjectHolder> holders = new HashMap<>();

    private static Submodel monSubModel;
    private static boolean monSubModelFailed = false;
    private static Map<String, String> monMapping = new HashMap<>();
    static {
        monMapping.put(MetricsProvider.SYS_DISK_FREE, SYSTEM_DISK_FREE);
        monMapping.put(MetricsProvider.SYS_DISK_TOTAL, SYSTEM_DISK_TOTAL);
        monMapping.put(MetricsProvider.SYS_DISK_USABLE, SYSTEM_DISK_USABLE);
        monMapping.put(MetricsProvider.SYS_DISK_USED, SYSTEM_DISK_USED);

        monMapping.put(MetricsProvider.SYS_MEM_FREE, SYSTEM_MEMORY_FREE);
        monMapping.put(MetricsProvider.SYS_MEM_TOTAL, SYSTEM_MEMORY_TOTAL);
        monMapping.put(MetricsProvider.SYS_MEM_USAGE, SYSTEM_MEMORY_USAGE);
        monMapping.put(MetricsProvider.SYS_MEM_USED, SYSTEM_MEMORY_USED);

        monMapping.put(MetricsProvider.DEVICE_CPU_TEMPERATURE, DEVICE_CPU_TEMPERATURE);
        monMapping.put(MetricsProvider.DEVICE_CASE_TEMPERATURE, DEVICE_CASE_TEMPERATURE);

        monMapping.put(MetricsProvider.SERVICE_TUPLES_SENT, SERVICE_TUPLES_SENT);
        monMapping.put(MetricsProvider.SERVICE_TUPLES_RECEIVED, SERVICE_TUPLES_RECEIVED);
        monMapping.put(MetricsProvider.SERVICE_TIME_PROCESSED, SERVICE_TIME_PROCESSED);
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

    /**
     * Returns a (unmodifiable) mapping of monitoring meter names to AAS names.
     * 
     * @return the default (unmodifiable) monitoring mapping
     */
    public static Map<String, String> getMonitoringMapping() {
        return Collections.unmodifiableMap(monMapping);
    }

    /**
     * Alternative approach to update the metric values. Can be called from the regular sending thread. Enabled only if 
     * not {@link #LAMBDA_SETTERS_SUPPORTED}. Uses the default meter-shortId names mapping defined in this class.
     * Initial approach, not really performant. May have to be throttled.
     * 
     * @param json the JSON to be sent to the monitoring channel
     * @param submodel the submodel to update (elements are assumed to be in a submodel elements collection on 
     *     the next level)
     * @param cSupplier collection supplier
     * @param update whether this is an update or the first call
     * @param mPredicate optional predicate to identify whether pushing a value shall happen, may be <b>null</b> 
     *     then {@link #PREDICATE_ALWAYS_TRUE} is used
     */
    public static void pushToAas(String json, String submodel, CollectionSupplier cSupplier, boolean update, 
        PushMeterPredicate mPredicate) {
        pushToAas(json, submodel, cSupplier, update, monMapping, mPredicate);
    }
    
    /**
     * Generic collection supplier.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface CollectionSupplier {
        
        /**
         * Returns the element access instances to be processed further during 
         * {@link MetricsAasConstructor#pushToAas(ElementsAccess, java.util.Map.Entry, Map, PushMeterPredicate)}.
         * 
         * @param parent the parent access to derive the elements from
         * @param deviceId the device id as passed in from the monitoring data
         * @return the element access instances to push
         */
        public List<ElementsAccess> get(ElementsAccess parent, String deviceId);
        
    }

    /**
     * Predicate to determine whether pushing a meter value shall happen.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PushMeterPredicate {

        /**
         * Tests whether pushing {@code meter} to {@code parent}. DeviceId is already matched.
         * 
         * @param parent the parent element
         * @param meter the meter
         * @return {@code true} if pushing is enabled, {@code false} if disabled and the meter shall not be pushed
         */
        public boolean test(ElementsAccess parent, JsonValue meter);
        
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Alternative approach to update the metric values. Can be called from the regular sending thread. Enabled only if 
     * not {@link #LAMBDA_SETTERS_SUPPORTED}. Initial approach, not really performant. May have to be throttled.
     * 
     * @param json the JSON to be sent to the monitoring channel
     * @param submodel the submodel to update (elements are assumed to be in a submodel elements collection on 
     *     the next level)
     * @param cSupplier collection supplier within {@code submodel}
     * @param update whether this is an update or the first call
     * @param monMapping the meter-shortId mapping to use
     * @param mPredicate optional predicate to identify whether pushing a value shall happen, may be <b>null</b> 
     *     then {@link #PREDICATE_ALWAYS_TRUE} is used
     */
    public static void pushToAas(String json, String submodel, CollectionSupplier cSupplier,  
        boolean update, Map<String, String> monMapping, PushMeterPredicate mPredicate) {
        if (!LAMBDA_SETTERS_SUPPORTED && null == monSubModel && !monSubModelFailed) {
            try {
                monSubModel = ActiveAasBase.getSubmodel(submodel);
            } catch (IOException e) {
                LoggerFactory.getLogger(MetricsAasConstructor.class).error(
                    "Obtaining submodel '{}' to push monitoring data to failed: {}", submodel, e.getMessage());

                System.out.println("ERROR: " + e.getMessage());
                monSubModelFailed = true;
            }
        }
        if (null != monSubModel) {
            mPredicate = null == mPredicate ? PREDICATE_ALWAYS_TRUE : mPredicate;
            JsonObject obj = Json.createReader(new StringReader(json)).readObject();
            String id = obj.getString("id");
            if (null != id) {
                List<ElementsAccess> coll = cSupplier.get(monSubModel, id);
                if (null != coll) {
                    JsonObject meters = obj.getJsonObject("meters");    
                    if (null != meters) {
                        for (ElementsAccess c : coll) {
                            for (Map.Entry<String, JsonValue> ent : meters.entrySet()) {
                                pushToAas(c, ent, monMapping, mPredicate);
                            }
                        }
                    }
                }
            }
        }
    }

    // checkstyle: resume parameter number check

    /**
     * Pushes a JSON metrics entry to {@code coll}.
     * 
     * @param coll the collection to push to
     * @param ent the metrics entry containing metrics name and measurement
     * @param monMapping the meter-shortId mapping to use
     * @param mPredicate optional predicate to identify whether pushing a value shall happen, may be <b>null</b> 
     *     then {@link #PREDICATE_ALWAYS_TRUE} is used
     */
    private static void pushToAas(ElementsAccess coll, Map.Entry<String, JsonValue> ent, 
        Map<String, String> monMapping, PushMeterPredicate mPredicate) {
        String idShort = monMapping.get(ent.getKey());
        if (null != idShort) {
            JsonValue json = ent.getValue();
            if (mPredicate.test(coll, json)) {
                AasUtils.setPropertyValueSafe(coll, idShort, getMeasurement(json.toString())); // implicit conversion
            }
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
            return getMeasurement(json);
        }

    }

    /**
     * Extracts the measurement out of the metrics JSON.
     * 
     * @param json the JSON, may be empty or <b>null</b>
     * @return the measurement, may be <b>null</b>
     */
    private static Object getMeasurement(String json) {
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
    
    // checkstyle: stop parameter number check

    /**
     * Creates a monitoring property.
     * 
     * @param smBuilder the parent submodel builder
     * @param type the type of the property
     * @param channel the transport channel (ignored if not {@link #LAMBDA_SETTERS_SUPPORTED})
     * @param id the id to react on (ignored if not {@link #LAMBDA_SETTERS_SUPPORTED})
     * @param setup the transport setup (ignored if not {@link #LAMBDA_SETTERS_SUPPORTED})
     * @param metricsName the name of the metrics as defined in the {@link MetricsProvider}
     * @param semId the optional semantic id (may be <b>null</b> for none)
     * @return the created property
     */
    public static Property createProperty(SubmodelElementContainerBuilder smBuilder, Type type, String channel, 
        String id, TransportSetup setup, String metricsName, String semId) {
        String idShort = monMapping.get(metricsName);
        PropertyBuilder pBuilder = smBuilder.createPropertyBuilder(idShort).setType(type);
        if (LAMBDA_SETTERS_SUPPORTED) {
            pBuilder.bind(new MeterGetter(channel, id, setup, metricsName), InvocablesCreator.READ_ONLY);
        } else {
            Object dflt;
            switch (type) { // typical defaults for monitoring here for now
            case AAS_INTEGER:
                dflt = 0;
                break;
            case FLOAT:
                dflt = 0.0;
                break;
            case DOUBLE:
                dflt = 0.0;
                break;
            case INTEGER:
                dflt = 0;
                break;
            default:
                dflt = null;
                break;
            }
            if (null != dflt) {
                pBuilder.setValue(dflt); // needed by BaSyx 1.0.1
            }
        }
        if (null != semId) {
            pBuilder.setSemanticId(semId);
        }
        return pBuilder.build();
    }

    // checkstyle: resume parameter number check

    /**
     * Adds system metrics to the submodel/elements. If {@link #LAMBDA_SETTERS_SUPPORTED}, values are bound against 
     * a transport connector receiver.
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
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_DISK_FREE, 
            Irdi.AAS_IRDI_UNIT_BYTE);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_DISK_TOTAL, 
            Irdi.AAS_IRDI_UNIT_BYTE);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_DISK_USABLE, 
            Irdi.AAS_IRDI_UNIT_BYTE);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_DISK_USED, 
            Irdi.AAS_IRDI_UNIT_BYTE);

        /* System Physical Memory metrics, string as JSON meter is transferred  */
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_MEM_FREE, 
            Irdi.AAS_IRDI_UNIT_BYTE);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_MEM_TOTAL, 
            Irdi.AAS_IRDI_UNIT_BYTE);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_MEM_USAGE, 
            Irdi.AAS_IRDI_UNIT_PERCENT);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SYS_MEM_USED, 
            Irdi.AAS_IRDI_UNIT_BYTE);
        
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.DEVICE_CPU_TEMPERATURE, 
            Irdi.AAS_IRDI_UNIT_DEGREE_CELSIUS);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.DEVICE_CASE_TEMPERATURE, 
            Irdi.AAS_IRDI_UNIT_DEGREE_CELSIUS);
    }

    /**
     * Adds service metrics to the submodel/elements. If {@link #LAMBDA_SETTERS_SUPPORTED}, values are bound against 
     * a transport connector receiver.
     * 
     * @param smBuilder submodel/elements builder of the AAS
     * @param filter    metrics filter, may be <b>null</b> for all (currently ignored)
     * @param channel   the transport channel to listen to
     * @param id        the metrics provider id to listen to
     * @param setup     the transport setup
     */
    public static void addServiceMetricsToAasSubmodel(SubmodelElementContainerBuilder smBuilder, 
        Predicate<String> filter, String channel, String id, TransportSetup setup) {
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SERVICE_TUPLES_SENT, null);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SERVICE_TUPLES_RECEIVED, null);
        createProperty(smBuilder, Type.DOUBLE, channel, id, setup, MetricsProvider.SERVICE_TIME_PROCESSED, 
            Irdi.AAS_IRDI_UNIT_MILLISECOND);
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
