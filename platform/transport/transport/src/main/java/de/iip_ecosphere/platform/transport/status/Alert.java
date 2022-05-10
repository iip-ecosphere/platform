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

package de.iip_ecosphere.platform.transport.status;

import java.io.IOException;

import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.streams.StreamNames;

/**
 * Represents a generic platform alert.
 * 
 * @author Holger Eichelberger, SSE
 * @author Matjaž Cerkvenik (original author DEvent, see monitoring.prometheus).
 */
public class Alert {
    
    public static final String ALERT_STREAM = StreamNames.ALERTS;

    private String uid;
    private String correlationId;
    private long timestamp;
    private long firstTimestamp;
    private long lastTimestamp;
    private long clearTimestamp;
    private String source;
    private String alertname;
    private String info;
    private String instance;
    private String severity;
    private String priority;
    private String tags;
    private String description;
    private String eventType = "5";
    private String probableCause = "1024";
    private String currentValue;
    private String url;
    private String status;
    private String ruleExpression = "";
    private String ruleTimeLimit;

    /** 
     * Returns the unique ID of notification.
     * 
     * @return the UID (may be <b>null</b> or empty)
     */
    public String getUid() {
        return uid;
    }

    /** 
     * Defines the unique ID of notification.
     * 
     * @param uid the UID
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /** 
     * Returns the correlation ID identifying the same type of events.
     * 
     * @return the correlation ID (may be <b>null</b> or empty)
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /** 
     * Defines the correlation ID identifying the same type of events.
     * 
     * @param correlationId the correlation ID
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    /** 
     * Returns the timestamp of the first occurrence.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /** 
     * Defines the timestamp of the first occurrence.
     * 
     * @param timestamp  timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 
     * Returns the timestamp of the first occurrence.
     *  
     * @return the timestamp of the first occurrence
     */
    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    /** 
     * Changes the timestamp of the first occurrence.
     *  
     * @param firstTimestamp the timestamp of the first occurrence
     */
    public void setFirstTimestamp(long firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    /** 
     * Returns the timestamp of the last occurrence.
     *  
     * @return the timestamp of the last occurrence
     */
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    /** 
     * Changes the timestamp of the last occurrence.
     *  
     * @param lastTimestamp the timestamp of the last occurrence
     */
    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    /** 
     * Returns the timestamp when the alert was cleared.
     * 
     * @return the timestamp
     */
    public long getClearTimestamp() {
        return clearTimestamp;
    }

    /** 
     * Changes the timestamp when the alert was cleared.
     * 
     * @param clearTimestamp the timestamp
     */
    public void setClearTimestamp(long clearTimestamp) {
        this.clearTimestamp = clearTimestamp;
    }

    /** 
     * Returns source (id, IP) who caused/sent the notification.
     * 
     * @return the source id/IP
     */
    public String getSource() {
        return source;
    }

    /** 
     * Changes the source (id, IP) who caused/sent the notification.
     * 
     * @param source the source id/IP
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /** 
     * Returns information about the alert.
     * 
     * @return information, may be <b>null</b> or empty
     */
    public String getInfo() {
        return info;
    }

    /** 
     * Changes information about the alert.
     * 
     * @param info information, may be <b>null</b> or empty
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /** 
     * Returns the name of the alert.
     * 
     * @return the name, may be <b>null</b> or empty
     */
    public String getAlertname() {
        return alertname;
    }

    /** 
     * Changes the name of the alert.
     * 
     * @param alertname the name, may be <b>null</b> or empty
     */
    public void setAlertname(String alertname) {
        this.alertname = alertname;
    }

    /** 
     * Returns source (id, IP) who caused/sent the notification.
     * 
     * @return the source id/IP, may be <b>null</b> or empty
     */
    public String getInstance() {
        return instance;
    }

    /** 
     * Changes the instance (id, IP) who caused/sent the notification. Depending on the 
     * event, may be same as {@link #getSource()}
     * 
     * @param instance the instance id/IP
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /** 
     * Returns the severity of the notification. [currently no enum here]
     * 
     * @return the severity, may be <b>null</b> or empty
     */
    public String getSeverity() {
        return severity;
    }

    /** 
     * Defines the severity of the notification. [currently no enum here]
     * 
     * @param severity the severity
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** 
     * Returns the urgency of the notification.
     * 
     * @return the urgency, may be <b>null</b> or empty
     */
    public String getPriority() {
        return priority;
    }

    /** 
     * Changes the urgency of the notification.
     * 
     * @param priority the urgency 
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /** 
     * Returns a comma-separated list of custom tags (labels).
     * 
     * @return the tags, may be <b>null</b> or empty
     */
    public String getTags() {
        return tags;
    }

    /** 
     * Defines a comma-separated list of custom tags (labels).
     * 
     * @param tags the tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /** 
     * Returns an additional description of notification.
     * 
     * @return the description, may be <b>null</b> or empty
     */
    public String getDescription() {
        return description;
    }

    /** 
     * Changes the additional description of notification.
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** 
     * Returns the status of the alert: firing or resolved.
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /** 
     * Changes the status of the alert: firing or resolved.
     * 
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 
     * Returns the event type according to ITU X.733.
     * 
     * @return the event type, may be <b>null</b> or empty
     */
    public String getEventType() {
        return eventType;
    }

    /** 
     * Defines the event type according to ITU X.733.
     * 
     * @param eventType the event type
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /** 
     * Returns the probable cause according to ITU X.733.
     * 
     * @return the probable cause
     */
    public String getProbableCause() {
        return probableCause;
    }

    /** 
     * Defines the probable cause according to ITU X.733.
     * 
     * @param probableCause the probable cause
     */
    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    /** 
     * Returns the current (metric) value causing the alert.
     * 
     * @return the current value
     */
    public String getCurrentValue() {
        return currentValue;
    }

    /** 
     * Returns the current (metric) value causing the alert.
     * 
     * @param currentValue the current value
     */
    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    /** 
     * Returns URL of somewhere associated to the alert, e.g. grafana.
     * 
     * @return the URL, may be <b>null</b> or empty
     */
    public String getUrl() {
        return url;
    }

    /** 
     * Defines URL of somewhere associated to the alert, e.g. grafana.
     * 
     * @param url the URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /** 
     * Returns a rule that caused this notification.
     * 
     * @return the rule expression, may be <b>null</b> or empty
     */
    public String getRuleExpression() {
        return ruleExpression;
    }

    /** 
     * Defines a rule that caused this notification.
     * 
     * @param ruleExpression the rule expression
     */
    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    /**
     * Returns a time limit set on the rule.
     * 
     * @return the time limit, may be <b>null</b> or empty
     */
    public String getRuleTimeLimit() {
        return ruleTimeLimit;
    }

    /**
     * Changes the time limit set on the rule.
     * 
     * @param ruleTimeLimit the time limit, may be <b>null</b> or empty
     */
    public void setRuleTimeLimit(String ruleTimeLimit) {
        this.ruleTimeLimit = ruleTimeLimit;
    }

    @Override
    public String toString() {
        return "Alert{" 
            + "uid='" + uid + '\'' 
            + ", correlationId='" + correlationId + '\'' 
            + ", timestamp=" + timestamp 
            + ", firstTimestamp=" + firstTimestamp 
            + ", lastTimestamp=" + lastTimestamp 
            + ", clearTimestamp=" + clearTimestamp 
            + ", source='" + source + '\'' 
            + ", alertname='" + alertname + '\'' 
            + ", info='" + info + '\'' 
            + ", instance='" + instance + '\'' 
            + ", severity='" + severity + '\'' 
            + ", priority='" + priority + '\'' 
            + ", tags='" + tags + '\'' 
            + ", description='" + description + '\'' 
            + ", eventType='" + eventType + '\'' 
            + ", probableCause='" + probableCause + '\'' 
            + ", currentValue='" + currentValue + '\'' 
            + ", url='" + url + '\'' 
            + ", status='" + status + '\'' 
            + ", ruleExpression='" + ruleExpression + '\'' 
            + ", ruleTimeLimit='" + ruleTimeLimit + '\'' 
            + '}';
    }
    
    /**
     * Sends this message to the given connector on {@code #ALERT STREAM}. [convenience]
     * 
     * @param conn the connector
     * @throws IOException if sending fails
     */
    public void send(TransportConnector conn) throws IOException {
        conn.asyncSend(ALERT_STREAM, this);
    }

}
