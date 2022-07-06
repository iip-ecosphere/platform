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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.monitoring.prometheus.ConfigModifier.ScrapeEndpoint;
import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusMonitoringSetup.PrometheusSetup;
import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.Transport;

/**
 * Platform lifecycle descriptor for prometheus monitoring. Unpacks and starts prometheus.
 * 
 * @author bettelsc
 */
public class PrometheusLifecycleDescriptor implements LifecycleDescriptor {
    
    public static final String PROMETHEUS = "prometheus";
    public static final String PROMETHEUS_VERSION = "2.34.0";
    public static final String ALERTMGR = "alertmanager";
    public static final String ALERTMGR_VERSION = "0.24.0";
    public static final String PROMETHEUS_CONFIG_INITIAL = "prometheus.yml.init";
    public static final String PROMETHEUS_CONFIG = "prometheus.yml";
    public static final String ALERTMGR_CONFIG = "alertmanager.yml";
    public static final String RESOURCES = "src/main/resources";
    
    private static boolean debug = false;
    
    private Process prometheusProcess;  
    private Process alertMgrProcess;  
    private File prometheusWorkingDirectory;
    private IipEcospherePrometheusExporter exporter;
    private AlertManagerImporter alertImporter;
    private ModifierRunnable modifierRunnable = new ModifierRunnable();
    private Deque<ConfigModifier> modifierQueue = new ConcurrentLinkedDeque<>();
    private Supplier<IipEcospherePrometheusExporter> exporterSupplier = () -> new IipEcospherePrometheusExporter();
    private Supplier<AlertManagerImporter> alertMgrSupplier = () -> new AlertManagerImporter();
    
    private Supplier<ConfigModifier> modifierSupplier = () -> { 
        return new ConfigModifier(getDefaultScrapePoints(), c -> modifierQueue.addLast(c) );
    };
    
    /**
     * Processes all modification requests.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ModifierRunnable implements Runnable {

        private boolean isRunning = true;
        
        @Override
        public void run() {
            while (isRunning) {
                ConfigModifier modifier = modifierQueue.poll();
                if (null != modifier) {
                    updateConfiguration(modifier, true);
                }
                TimeUtils.sleep(200);
            }
        }
        
        /**
         * Stops the runnable.
         */
        public void stop() {
            isRunning = false;
        }
        
    }

    /**
     * Returns the default scrape points.
     * 
     * @return the default scrape points
     */
    private static List<ScrapeEndpoint> getDefaultScrapePoints() {
        List<ScrapeEndpoint> result = new ArrayList<>(); // intentionally empty yet
        PrometheusMonitoringSetup setup = PrometheusMonitoringSetup.getInstance();
        // there must be at least one; late evaluation to have also ephemeral -1 changed to value
        result.add(new ScrapeEndpoint("prometheus", new Endpoint(Schema.HTTP, 
            "localhost", setup.getPrometheus().getExporter().getPort(), 
            IipEcospherePrometheusExporter.DEFAULT_METRICS_ENDPOINT)));
        return result;
    }
    
    /**
     * Allows changing the debug flag. [testing, debugging]
     * 
     * @param flag the new value of the debug flag, {@code true} with prometheus debug information
     */
    public static void setDebugFlag(boolean flag) {
        debug = flag;
    }

    /**
     * Updates the prometheus configuration. Rules may follow.
     * 
     * @param modifier the modifier to use
     * @param notify notify prometheus about the change, e.g., after initial writing
     */
    private void updateConfiguration(ConfigModifier modifier, boolean notify) {
        // TODO works only local, how to do modifications on remote?
        try {
            PrometheusSetup setup = PrometheusMonitoringSetup.getInstance().getPrometheus();
            File cfg = new File(prometheusWorkingDirectory, PROMETHEUS_CONFIG);
            Path initCfgPath = new File(prometheusWorkingDirectory, PROMETHEUS_CONFIG_INITIAL).toPath();
            Files.copy(initCfgPath, cfg.toPath(), StandardCopyOption.REPLACE_EXISTING);
            PrintWriter writer = new PrintWriter(new FileWriter(cfg, false));
            writer.println("global:");
            writer.println("  scrape_interval: " + setup.getScrapeInterval() + "ms");
            writer.println("  scrape_timeout: " + setup.getScrapeTimeoutSafe() + "ms");
            writer.println("  evaluation_interval: " + setup.getEvaluationInterval() + "ms");
            
            writer.println("");
            writer.println("scrape_configs:");
            for (ScrapeEndpoint e : modifier.scrapeEndpoints()) {
                Endpoint ep = e.getScrapePoint();
                writer.println("  - job_name: \"" + e.getName() + "\"");
                writer.println("    metrics_path: \"" + ep.getEndpoint() + "\"");
                writer.println("    scheme: \"" + ep.getSchema().name().toLowerCase() + "\"");
                writer.println("    static_configs:");
                writer.println("      - targets: [\"" + ep.getHost() + ":" + ep.getPort() + "\"]");
            }
            writer.println();
            ServerAddressHolder alertMgr = setup.getAlertMgr();
            if (alertMgr.getPort() > 0) {
                writer.println("# Alertmanager configuration");
                writer.println("alerting:");
                writer.println("  alertmanagers:");
                writer.println("    - api_version: v1");
                writer.println("      static_configs:");
                writer.println("        - targets:");
                writer.println("           - " + alertMgr.getHost() + ":" + alertMgr.getPort());
            }
            
            writer.println("rule_files:");
            writer.println("  # - \"first_rules.yml\"");
            writer.println("  # - \"second_rules.yml\"");
            
            writer.close();
            if (notify) {
                HttpClient httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(setup.getServer().getServerAddress().toServerUri() 
                    + "/-/reload");
                HttpResponse response = httpclient.execute(httppost);
                int code = response.getStatusLine().getStatusCode();
                if (code >= 400) {
                    String phrase = response.getStatusLine().getReasonPhrase();
                    LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class)
                        .info("Cannot update configuration. HTTP response: {} {}", code, phrase);
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class)
                .info("Cannot update configuration: {}", e.getMessage());
        }
    }
    
    /**
     * Returns a resource to use.
     * 
     * @param name the name of the resource
     * @return the resource
     * @throws IOException if the resource can (finally) not be loaded
     */
    private static InputStream getResource(String name) throws IOException {
        InputStream in = ResourceLoader.getResourceAsStream(name);
        if (in == null) { // testing fallback
            in = new FileInputStream(new File(RESOURCES, name));
        }
        return in;
    }
     
    @Override
    public void startup(String[] args) {
        PrometheusMonitoringSetup setup = PrometheusMonitoringSetup.getInstance();
        Transport.setTransportSetup(() -> setup.getTransport());

        if (!setup.getPrometheus().getExporter().isRunning()) {
            exporter = exporterSupplier.get();
            exporter.setModifierSupplier(modifierSupplier);
            exporter.start();
        }
        if (!setup.getPrometheus().getServer().isRunning()) {
            String zipName = AbstractProcessService.getExecutablePrefix(PROMETHEUS, PROMETHEUS_VERSION) + ".zip";
            String exeName = AbstractProcessService.getExecutableName(PROMETHEUS, PROMETHEUS_VERSION);
            String alertExeName = AbstractProcessService.getExecutableName(ALERTMGR, ALERTMGR_VERSION);
            prometheusWorkingDirectory = FileUtils.createTmpFolder("iip-prometheus");
            File prometheusFile = new File(prometheusWorkingDirectory, exeName);
            File alertMgrFile = new File(prometheusWorkingDirectory, alertExeName);
            try {
                InputStream in = getResource(PROMETHEUS_CONFIG);
                Path initCfgPath = new File(prometheusWorkingDirectory, PROMETHEUS_CONFIG_INITIAL).toPath();
                Files.copy(in, initCfgPath,  StandardCopyOption.REPLACE_EXISTING);
                in.close();
                Files.copy(initCfgPath, new File(prometheusWorkingDirectory, PROMETHEUS_CONFIG).toPath(),  
                    StandardCopyOption.REPLACE_EXISTING);

                in = getResource(ALERTMGR_CONFIG);
                Path alertCfgPath = new File(prometheusWorkingDirectory, ALERTMGR_CONFIG).toPath();
                Files.copy(in, alertCfgPath,  StandardCopyOption.REPLACE_EXISTING);
                in.close();
                updateConfiguration(new ConfigModifier(getDefaultScrapePoints(), null), false);

                in = getResource(zipName);
                JarUtils.extractZip(in, prometheusWorkingDirectory.toPath());
                in.close();
                prometheusFile.setExecutable(true);

                in = getResource(alertExeName);
                Path alertMgrPath = new File(prometheusWorkingDirectory, alertExeName).toPath();
                Files.copy(in, alertMgrPath,  StandardCopyOption.REPLACE_EXISTING);
                in.close();
                alertMgrFile.setExecutable(true);

                List<String> pArgs = new ArrayList<>();
                pArgs.add(alertMgrFile.getAbsolutePath());
                pArgs.add("--config.file=" + ALERTMGR_CONFIG);
                pArgs.add("--web.listen-address=:" + setup.getPrometheus().getAlertMgr().getPort());
                if (debug) {
                    pArgs.add("--log.level=debug");
                }
                ProcessBuilder procBuilder = new ProcessBuilder(pArgs);
                procBuilder.directory(prometheusWorkingDirectory);
                procBuilder.inheritIO();
                alertMgrProcess = procBuilder.start();
                LoggerFactory.getLogger(getClass()).info("{} {} started on port {}", ALERTMGR, ALERTMGR_VERSION, 
                    setup.getPrometheus().getAlertMgr().getPort());

                pArgs = new ArrayList<>();
                pArgs.add(prometheusFile.getAbsolutePath());
                pArgs.add("--config.file=" + PROMETHEUS_CONFIG);
                pArgs.add("--web.enable-lifecycle");
                pArgs.add("--web.listen-address=:" + setup.getPrometheus().getServer().getPort());
                if (debug) {
                    pArgs.add("--log.level=debug");
                }
                procBuilder = new ProcessBuilder(pArgs);
                procBuilder.directory(prometheusWorkingDirectory);
                procBuilder.inheritIO();
                prometheusProcess = procBuilder.start();
                LoggerFactory.getLogger(getClass()).info("{} {} started on port {}", PROMETHEUS, PROMETHEUS_VERSION, 
                    setup.getPrometheus().getServer().getPort());
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Starting Prometheus: {}", e.getMessage());
            }
        }

        new Thread(modifierRunnable).start();
        if (!setup.getPrometheus().getAlertMgr().isRunning()) {
            alertImporter = alertMgrSupplier.get();
            alertImporter.start();
        }
    } 

    /**
     * Defines the exporter supplier.
     * 
     * @param supplier the supplier
     */
    public void setExporterSupplier(Supplier<IipEcospherePrometheusExporter> supplier) {
        if (null != supplier) {
            this.exporterSupplier = supplier;
        }
    }
    
    /**
     * Deletes all files used in prometheus run.
     */
    public void deleteWorkingFiles() {
        if (prometheusWorkingDirectory != null) { // no full startup
            String exeName = AbstractProcessService.getExecutableName(PROMETHEUS, PROMETHEUS_VERSION);
            new File(prometheusWorkingDirectory.getAbsolutePath(), exeName).delete();
            String alertMgrName = AbstractProcessService.getExecutableName(PROMETHEUS, ALERTMGR);
            new File(prometheusWorkingDirectory.getAbsolutePath(), alertMgrName).delete();
            new File(prometheusWorkingDirectory.getAbsolutePath(), PROMETHEUS_CONFIG).delete();
            new File(prometheusWorkingDirectory.getAbsolutePath(), ALERTMGR_CONFIG).delete();
            // delete data?
        }
    }
    
    /**
     * Returns the exporter instance. [testing]
     * 
     * @return the exporter instance
     */
    public IipEcospherePrometheusExporter getExporter() {
        return exporter;
    }
    
    @Override
    public void shutdown() {
        if (null != alertImporter) {
            alertImporter.stop();
        }
        if (null != exporter) {
            exporter.stop();
        }
        modifierRunnable.stop();
        if (null != prometheusProcess) {
            prometheusProcess.destroyForcibly();
            LoggerFactory.getLogger(getClass()).info("{} {} shutdown", PROMETHEUS, PROMETHEUS_VERSION);
            prometheusProcess = null;
        }
        if (null != alertMgrProcess) {
            alertMgrProcess.destroyForcibly();
            LoggerFactory.getLogger(getClass()).info("{} {} shutdown", ALERTMGR, ALERTMGR_VERSION);
            alertMgrProcess = null;
        }
        Transport.releaseConnector();
        deleteWorkingFiles();
    }
    
    @Override
    public Thread getShutdownHook() {
        return new Thread(() -> shutdown());
    }
    
    @Override
    public int priority() {
        return LifecycleDescriptor.INIT_PRIORITY;
    }
    
}
