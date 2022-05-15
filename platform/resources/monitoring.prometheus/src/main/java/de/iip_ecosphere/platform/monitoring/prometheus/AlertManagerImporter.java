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

package de.iip_ecosphere.platform.monitoring.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.Alert;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.model.DSeverity;
import si.matjazcerkvenik.alertmonitor.model.PrometheusSyncTask;
import si.matjazcerkvenik.alertmonitor.model.alertmanager.AlertmanagerProcessor;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PAlert;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApi;
import si.matjazcerkvenik.alertmonitor.model.prometheus.PrometheusApiException;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;
import si.matjazcerkvenik.alertmonitor.util.AmProps;
import si.matjazcerkvenik.alertmonitor.util.Formatter;
import si.matjazcerkvenik.alertmonitor.util.TaskManager;

/**
 * A prometheus alert manager importer. This class assumes that 
 * {@link Transport#setTransportSetup(java.util.function.Supplier) Transport}  is set up.
 * 
 * @author Matjaž Cerkvenik (original author of {@link PrometheusSyncTask} from which we 
 *   took over code as we cannot hook into {@link DAO}). 
 * @author Holger Eichelberger, SSE
 */
public class AlertManagerImporter {
    
    private static Logger logger = LoggerFactory.getLogger(AlertManagerImporter.class);
    private Timer timer;
    private SyncTask syncTask;
    
    /**
     * Starts the importer.
     */
    public void start() {
        PrometheusMonitoringSetup setup = PrometheusMonitoringSetup.getInstance();
        AmProps.ALERTMONITOR_PROMETHEUS_SERVER = setup.getAlertMgr().toServerUri();
        AmProps.ALERTMONITOR_HTTP_CLIENT_READ_TIMEOUT_SEC = 2;
        AmProps.ALERTMONITOR_KAFKA_ENABLED = false;
        AmProps.ALERTMONITOR_MONGODB_ENABLED = false;
        AmProps.ALERTMONITOR_PSYNC_INTERVAL_SEC = 1;
        syncTask = new SyncTask();
        timer = new Timer("IIP Prometheus alert manager sync task");
        timer.schedule(syncTask, 0, 1000);
    }

    /**
     * A sync task in the style of {@link PrometheusSyncTask} as we cannot hook into the {@link DAO} with a 
     * platform mechanism.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class SyncTask extends TimerTask {

        private PrometheusApi api = new PrometheusApi();

        @Override
        public void run() {
            logger.info("PSYNC: === starting periodic synchronization ===");
            AmMetrics.lastPsyncTimestamp = System.currentTimeMillis();

            try {
                List<PAlert> activeAlerts = api.alerts();
                if (activeAlerts == null) {
                    logger.error("PSYNC: null response returned");
                    logger.info("PSYNC: === Periodic synchronization complete ===");
                    AmMetrics.psyncFailedCount++;
                    DAO.getInstance().addWarning("psync", "Synchronization is failing");
                    return;
                }

                // all alerts retrieved by psync
                List<DEvent> pSyncAlerts = new ArrayList<>();
                for (PAlert alert : activeAlerts) {
                    logger.debug(alert.toString());

                    DEvent e = createDEvent(alert);
                    // set prometheusId
                    String[] lblArray = AmProps.ALERTMONITOR_PROMETHEUS_ID_LABELS.split(",");
                    String s = "{";
                    for (int i = 0; i < lblArray.length; i++) {
                        s += lblArray[i].trim() + "=\"" 
                            + alert.getLabels().getOrDefault(lblArray[i].trim(), "-") + "\", ";
                    }
                    s = s.substring(0, s.length() - 2) + "}";
                    e.setPrometheusId(s);

                    // set all other labels
                    e.setOtherLabels(alert.getLabels());

                    if (!alert.getState().equals("firing")) {
                        // ignore alerts in pending state
                        continue;
                    }
                    e.setStatus("firing");

                    // add tags
                    // eg: severity (but not clear), priority
                    if (!e.getSeverity().equals(DSeverity.CLEAR)) {
                        e.setTags(e.getTags() + "," + e.getSeverity());
                    }
                    e.setTags(e.getTags() + "," + e.getPriority());

                    // environment variable substitution
                    e.setNodename(AlertmanagerProcessor.substitute(e.getNodename()));
                    e.setInfo(AlertmanagerProcessor.substitute(e.getInfo()));
                    e.setDescription(AlertmanagerProcessor.substitute(e.getDescription()));
                    e.setTags(AlertmanagerProcessor.substitute(e.getTags()));
                    e.setUrl(AlertmanagerProcessor.substitute(e.getUrl()));

                    // set unique ID of event
                    e.generateUID();
                    // set correlation ID
                    e.generateCID();

                    logger.debug("PSYNC: " + e.toString());
                    pSyncAlerts.add(e);

                } // for each alert

                synchronizeAlerts(pSyncAlerts, true);

                AmMetrics.psyncSuccessCount++;
                AmMetrics.alertmonitor_psync_success.set(1);
                DAO.getInstance().removeWarning("psync");

            } catch (PrometheusApiException e) {
                logger.error("PSYNC: failed to synchronize alarms; root cause: " + e.getMessage());
                AmMetrics.psyncFailedCount++;
                AmMetrics.alertmonitor_psync_success.set(0);
                DAO.getInstance().addWarning("psync", "Synchronization is failing");
            }

            logger.info("PSYNC: === Periodic synchronization complete ===");
        }
        
        @Override
        public boolean cancel() {
            DAO.getInstance().getDataManager().close();
            TaskManager.getInstance().stopDbMaintenanceTimer();
            return super.cancel();
        }

        
        /**
         * Creates a {@link DEvent} from a {@link PAlert}.
         * 
         * @param alert the alert
         * @return the event
         */
        private DEvent createDEvent(PAlert alert) {
            DEvent e = new DEvent();
            e.setTimestamp(System.currentTimeMillis());
            e.setAlertname(alert.getLabels().getOrDefault(DEvent.LBL_ALERTNAME, "-unknown-"));
            e.setSource("PSYNC");
            e.setUserAgent("");
            e.setInstance(alert.getLabels().getOrDefault(DEvent.LBL_INSTANCE, "-"));
            e.setHostname(Formatter.stripInstance(e.getInstance()));
            e.setNodename(alert.getLabels().getOrDefault(DEvent.LBL_NODENAME, e.getInstance()));
            e.setInfo(alert.getLabels().getOrDefault(DEvent.LBL_INFO, "-"));
            e.setJob(alert.getLabels().getOrDefault(DEvent.LBL_JOB, "-"));
            e.setTags(alert.getLabels().getOrDefault(DEvent.LBL_TAGS, ""));
            e.setSeverity(alert.getLabels().getOrDefault(DEvent.LBL_SEVERITY, "indeterminate"));
            e.setPriority(alert.getLabels().getOrDefault(DEvent.LBL_PRIORITY, "low"));
            e.setGroup(alert.getLabels().getOrDefault(DEvent.LBL_GROUP, "unknown"));
            e.setEventType(alert.getLabels().getOrDefault(DEvent.LBL_EVENTTYPE, "5"));
            e.setProbableCause(alert.getLabels().getOrDefault(DEvent.LBL_PROBABLECAUSE, "1024"));
            e.setCurrentValue(alert.getAnnotations().getOrDefault(DEvent.LBL_CURRENTVALUE, "-"));
            e.setUrl(alert.getLabels().getOrDefault(DEvent.LBL_URL, ""));
            if (alert.getLabels().containsKey(DEvent.LBL_DESCRIPTION)) {
                e.setDescription(alert.getLabels().getOrDefault(DEvent.LBL_DESCRIPTION, "-"));
            } else {
                e.setDescription(alert.getAnnotations().getOrDefault(DEvent.LBL_DESCRIPTION, "-"));
            }
            return e;
        }
        
    }

    /**
     * Synchronizes the alerts with the platform.
     * 
     * @param alertList the alerts
     * @param psync comes from PSync (ignored)
     */
    private void synchronizeAlerts(List<DEvent> alertList, boolean psync) {
        for (DEvent e: alertList) {
            Alert alert = new Alert();
            e.setUid(e.getUid());
            e.setCorrelationId(e.getCorrelationId());
            e.setTimestamp(e.getTimestamp());
            e.setFirstTimestamp(e.getFirstTimestamp());
            e.setLastTimestamp(e.getLastTimestamp());
            e.setClearTimestamp(e.getClearTimestamp());
            e.setAlertname(e.getAlertname());
            e.setSource(e.getSource());
            e.setInstance(e.getInstance());
            e.setInfo(e.getInfo());
            e.setTags(e.getTags());
            e.setSeverity(e.getSeverity());
            e.setPriority(e.getPriority());
            e.setEventType(e.getEventType());
            e.setProbableCause(e.getProbableCause());
            e.setCurrentValue(e.getCurrentValue());
            e.setUrl(e.getUrl());
            e.setDescription(e.getDescription());
            e.setStatus(e.getStatus());
            e.setRuleExpression(e.getRuleExpression());
            e.setRuleTimeLimit(e.getRuleTimeLimit());
            Transport.sendAlert(alert);
        }
    }

    /**
     * Stops the importer.
     */
    public void stop() {
        if (null != syncTask) {
            syncTask.cancel();
            syncTask = null;
        }
        if (null != timer) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }

}
