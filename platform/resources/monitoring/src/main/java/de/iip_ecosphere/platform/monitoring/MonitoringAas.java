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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.status.Alert;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.transport.streams.StreamNames;

/**
 * Implements a generic service that maps {@link TraceRecord} to an (application) AAS.
 * This service is in development/preliminary. The service does not take any data or produce data,
 * it is just meant to create up the trace record AAS entries. It can be used as a sink.
 * 
 * Currently, the service builds up the AAS of an application. However, this functionality
 * shall be moved that the platform is providing the AAS and the service just hooks the traces submodel into.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoringAas implements AasContributor {

    public static final String SUBMODEL_MONITORING = "Monitoring";
    public static final String SMEC_ALERTS = "Alerts";

    public static final String PROPERTY_ALERT_UID = "Uid";
    public static final String PROPERTY_ALERT_CORRELATION_ID = "CorrelationId";
    public static final String PROPERTY_ALERT_TIMESTAMP = "Timestamp";
    public static final String PROPERTY_ALERT_FIRSTTIMESTAMP = "FirstTimestamp";
    public static final String PROPERTY_ALERT_LASTTIMESTAMP = "LastTimestamp";
    public static final String PROPERTY_ALERT_CLEARTIMESTAMP = "ClearTimestamp";
    public static final String PROPERTY_ALERT_SOURCE = "Source";
    public static final String PROPERTY_ALERT_NAME = "AlertName";
    public static final String PROPERTY_ALERT_INFO = "Info";
    public static final String PROPERTY_ALERT_INSTANCE = "Instance";
    public static final String PROPERTY_ALERT_SEVERITY = "Severity";
    public static final String PROPERTY_ALERT_PRIORITY = "Priority";
    public static final String PROPERTY_ALERT_TAGS = "Tags";
    public static final String PROPERTY_ALERT_DESCRIPTION = "Description";
    public static final String PROPERTY_ALERT_EVENT_TYPE = "EventType";
    public static final String PROPERTY_ALERT_PROBABLE_CAUSE = "ProbableCause";
    public static final String PROPERTY_ALERT_CURRENT_VALUE = "CurrentValue";
    public static final String PROPERTY_ALERT_URL = "Url";
    public static final String PROPERTY_ALERT_STATUS = "Status";
    public static final String PROPERTY_ALERT_RULE_EXPRESSION = "RuleExpression";
    public static final String PROPERTY_ALERT_RULE_TIME_LIMIT = "RuleTimeLimit";
    
    private long timeout = 60 * 60 * 1000; // cleanup after 1 hour
    private long lastCleanup = System.currentTimeMillis();
    private long cleanupTimeout = 5 * 1000;
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        try {
            TransportConnector conn = Transport.createConnector();
            if (null != conn) {
                conn.setReceptionCallback(StreamNames.ALERTS, new AlertReceptionCallback());
            } else {
                LoggerFactory.getLogger(getClass()).error(
                    "Cannot setup monitoring alert reception: Transport not configured");
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot setup monitoring alert reception: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public Kind getKind() {
        return Kind.DYNAMIC;
    }
    
    /**
     * A trace reception callback calling {@link MonitoringAas#handleNew(TraceRecord) TraceToAas} in own threads.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class AlertReceptionCallback implements ReceptionCallback<Alert> {
        
        @Override
        public void received(Alert data) {
            new Thread(() -> handleNew(data)).start(); // thread pool?
        }

        @Override
        public Class<Alert> getType() {
            return Alert.class;
        }
        
    }
    
    /**
     * Handles a new alert and cleans up outdated ones.
     * 
     * @param data the alert data
     */
    private void handleNew(Alert data) {
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            SubmodelBuilder smBuilder = aas.createSubmodelBuilder(SUBMODEL_MONITORING, null);
            SubmodelElementCollectionBuilder smcBuilder = smBuilder.createSubmodelElementCollectionBuilder(
                SMEC_ALERTS, true, true);
            SubmodelElementCollectionBuilder entryBuilder = smBuilder.createSubmodelElementCollectionBuilder(
                AasUtils.fixId("Alert_" + data.getTimestamp()), true, true); // may also be UID
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_UID)
                .setValue(Type.STRING, safe(data.getUid()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_CORRELATION_ID)
                .setValue(Type.STRING, safe(data.getCorrelationId()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_TIMESTAMP)
                .setValue(Type.INTEGER, data.getTimestamp())
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_FIRSTTIMESTAMP)
                .setValue(Type.INTEGER, data.getFirstTimestamp())
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_LASTTIMESTAMP)
                .setValue(Type.INTEGER, data.getLastTimestamp())
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_CLEARTIMESTAMP)
                .setValue(Type.INTEGER, data.getClearTimestamp())
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_SOURCE)
                .setValue(Type.STRING, safe(data.getSource()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_NAME)
                .setValue(Type.STRING, safe(data.getAlertname()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_INFO)
                .setValue(Type.STRING, safe(data.getInfo()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_INSTANCE)
                .setValue(Type.STRING, safe(data.getInstance()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_SEVERITY)
                .setValue(Type.STRING, safe(data.getSeverity()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_PRIORITY)
                .setValue(Type.STRING, safe(data.getPriority()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_TAGS)
                .setValue(Type.STRING, safe(data.getTags()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_DESCRIPTION)
                .setValue(Type.STRING, safe(data.getDescription()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_EVENT_TYPE)
                .setValue(Type.STRING, safe(data.getEventType()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_PROBABLE_CAUSE)
                .setValue(Type.STRING, safe(data.getProbableCause()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_CURRENT_VALUE)
                .setValue(Type.STRING, safe(data.getCurrentValue()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_URL)
                .setValue(Type.STRING, safe(data.getUrl()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_STATUS)
                .setValue(Type.STRING, safe(data.getStatus()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_RULE_EXPRESSION)
                .setValue(Type.STRING, safe(data.getRuleExpression()))
                .build();
            smcBuilder.createPropertyBuilder(PROPERTY_ALERT_RULE_TIME_LIMIT)
                .setValue(Type.STRING, safe(data.getRuleTimeLimit()))
                .build();
            
            entryBuilder.build();
            smcBuilder.build();
            smBuilder.build();
            cleanup(aas);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot obtain IIP-Ecosphere platform AAS: {}", e.getMessage());
        }
    }
    
    /**
     * Returns at minimum a non-empty string.
     * 
     * @param string the input string
     * @return {@code string} or empty if <b>null</b>
     */
    private static String safe(String string) {
        return null == string ? "" : string;
    }
    
    /**
     * Cleans up outdated alerts.
     * 
     * @param aas the AAS to clean up
     */
    private void cleanup(Aas aas) {
        // remove outdated ones
        long now = System.currentTimeMillis();
        if (now - lastCleanup > cleanupTimeout) {
            long timestamp = now - timeout;
            Submodel sm = aas.getSubmodel(SUBMODEL_MONITORING);
            if (null != sm) {
                SubmodelElementCollection alerts = sm.getSubmodelElementCollection(SMEC_ALERTS);
                if (null != alerts) {
                    cleanup(alerts, timestamp);
                    lastCleanup = now;
                }
            }
        }
    }

    /**
     * Cleans up outdated alerts.
     * 
     * @param alerts the alerts SMEC.
     * @param timestamp the timestamp before alerts shall be cleared
     */
    private void cleanup(SubmodelElementCollection alerts, long timestamp) {
        List<SubmodelElement> delete = new ArrayList<>();
        for (SubmodelElement elt : alerts.elements()) {
            if (elt instanceof SubmodelElementCollection) {
                SubmodelElementCollection coll = (SubmodelElementCollection) elt;
                Property prop = coll.getProperty(PROPERTY_ALERT_TIMESTAMP);
                if (null != prop) {
                    try {
                        Object val = prop.getValue();
                        boolean del = false;
                        if (val instanceof Integer) {
                            del = ((Integer) val) < timestamp;
                        } else if (val instanceof Long) {
                            del = ((Long) val) < timestamp;
                        }
                        if (del) {
                            delete.add(elt);
                        }
                    } catch (ExecutionException e) {
                    }
                }
            }
        }
        for (SubmodelElement elt : delete) {
            alerts.deleteElement(elt.getIdShort());
        }
    }

}
