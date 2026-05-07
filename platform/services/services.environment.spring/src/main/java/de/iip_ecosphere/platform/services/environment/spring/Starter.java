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

package de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceMapper;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClient;
import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import de.iip_ecosphere.platform.support.yaml.YamlFile;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.support.plugins.PluginSetup;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * A specialized starter for Spring Cloud Stream in including the metrics provider.
 * 
 * @author Holger Eichelberger, SSE
 */
@ComponentScan(basePackageClasses = MetricsProvider.class)
@EnableScheduling
@Import({MetricsProvider.class})
@Component
public abstract class Starter extends de.iip_ecosphere.platform.services.environment.Starter 
    implements CommandLineRunner {

    public static final String OPT_SPRING_FUNCTION_DEF = "spring.cloud.function.definition";
    public static final String OPT_SPRING_BINDINGS_PREFIX = "spring.cloud.stream.bindings.";
    public static final String OPT_SPRING_BINDER_POSTFIX = ".binder";
    public static final String EXTERNAL_BINDER_NAME = "external";
    private static final String DEPLOYMENT_DESC = "deployment.yml";
    
    private static ConfigurableApplicationContext ctx;
    private static Environment environment;
    private static int port = 8080; // assumed default
    private static List<ServiceForMapping> startupSequence;
    private static int expectedServiceCount;
    
    @Autowired
    private ServerProperties serverProperties;

    /**
     * Creates an instance.
     * 
     * @param env the Spring environment
     */
    @Autowired
    public Starter(Environment env) {
        environment = env;
    }
    
    @Override
    public void run(String...args) throws Exception {
        initialize();
    }
    
    /**
     * Initializes the services (if available), starts the AAS command server.
     * 
     * @see #createServices(YamlArtifact)
     */
    public void initialize() {
        if (null != serverProperties && null != serverProperties.getPort()) {
            port = serverProperties.getPort();
            LoggerFactory.getLogger(Starter.class).info("Using spring application server port " + port);
        } else { // probably the same as server properties
            String tmp = null != environment ? environment.getProperty("server.port") : "";
            if (null != tmp) {
                try {
                    port = Integer.parseInt(tmp);
                    LoggerFactory.getLogger(Starter.class).info("Using spring application server port " + port);
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(Starter.class).error("Cannot read spring application server port: " + tmp 
                        + "; " + e.getMessage() + " using assumed default: " + port);    
                }
            } else {
                LoggerFactory.getLogger(Starter.class).info("Using (assumed default) spring application server port " 
                    + port);
            }
        }
        setCmdServerConfigurer(b -> b.forTomcat());
        // start the command server
        try {
            // assuming that deployment.yml variants for testing contain the same service descriptions (modulo 
            // technical information)
            YamlArtifact art = YamlArtifact.readFromYaml(ResourceLoader.getResourceAsStream(DEPLOYMENT_DESC));
            List<Service> services = createServices(art);
            if (null != services) { 
                Collections.sort(services, Service.START_COMPARATOR);
                ServiceMapper mapper = new ServiceMapper(Starter.getProtocolBuilder());
                for (Service service : services) {
                    mapService(mapper, service, true); // used by testing, may require individual information
                }
            }
            Starter.start();
        } catch (IOException e) {
            System.out.println("Cannot find service descriptor/start command server.");
        }
    }
    
    /**
     * Represents a service that shall be mapped.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ServiceForMapping {
        
        private String id;
        private AtomicReference<Runnable> mapping = new AtomicReference<>();

        /**
         * Creates the service that shall be mapped.
         * 
         * @param id the service id
         */
        private ServiceForMapping(String id) {
            this.id = id;
        }
        
        @Override
        public String toString() {
            return id;
        }
        
    }
    
    /**
     * In {@link #getServiceAutostart() autostart mode}, determine an enforced service startup sequence.
     * 
     * Requires {@link #parse(String...)} to be executed before.
     */
    private static void determineStartupSequence() {
        List<ServiceForMapping> startupSeq = null;
        try {
            YamlArtifact art = YamlArtifact.readFromYaml(ResourceLoader.getResourceAsStream(DEPLOYMENT_DESC));
            List<YamlService> services = art.getServices();
            Collections.sort(services, (s1, s2) 
                -> ServiceKind.START_COMPARATOR.compare(s1.getKind(), s2.getKind()));
            startupSeq = Collections.synchronizedList(services.stream()
                .filter(s -> s.isTopLevel()) // family but no family elements
                .map(s -> new ServiceForMapping(s.getServiceId()))
                .collect(Collectors.toList()));
            expectedServiceCount = startupSeq.size();
        } catch (IOException e) {
            LoggerFactory.getLogger(Starter.class).warn("Cannot determine services (count, sequence) to be started. "
                + "Mocking data ingestion may fail as {} cannot be found: {}", DEPLOYMENT_DESC, e.getMessage());
        }
        if (enforceStartSequence() && getServiceAutostart() && startupSeq != null) {
            // only in testing, only in a single JVM!
            startupSequence = startupSeq;
            LoggerFactory.getLogger(Starter.class).info("Enforcing startup sequence {}", startupSequence);
            // spring cloud stream does not support explicit startup sequences; messages among services would be one
            // option, but so far we need it only for testing; thread enforces "start" in specified sequence
            // as runnable calls mapping which sets services to STARTING and waits until RUNNING or FAILURE
            new Thread(() -> {
                while (true) {
                    int nullCount = 0;
                    // find the first non-null that can be mapped
                    for (int i = 0; i < startupSequence.size(); i++) {
                        ServiceForMapping s = startupSequence.get(i);
                        if (s != null) {
                            Runnable mapping = s.mapping.get();
                            if (mapping != null && nullCount == i) {
                                startupSequence.set(i, null);
                                mapping.run();
                            }
                        } else {
                            nullCount++;
                        }
                    }
                    if (nullCount == startupSequence.size()) {
                        startDataForMappedServices();
                        break;
                    }
                    TimeUtils.sleep(300);
                }
            }).start();
        }
        
    }

    /**
     * Creates a metrics client.
     * 
     * @param environment the Spring environment
     * @return the metrics REST client, may be <b>null</b>
     */
    public static MetricsExtractorRestClient createMetricsClient(Environment environment) {
        return new MetricsExtractorRestClient("localhost", port);
    }
    
    /**
     * Creates a metrics client based on the known Spring environment. Only available after 
     * {@link #main(Class, String[])}.
     * 
     * @return the metrics REST client, may be <b>null</b>
     */
    public static MetricsExtractorRestClient createMetricsClient() {
        return createMetricsClient(environment);
    }
    
    /**
     * Returns the application context. Only available after {@link #main(Class, String[])}.
     * 
     * @return the context, may be <b>null</b>
     */
    protected static ConfigurableApplicationContext getContext() {
        return ctx;
    }
    
    /**
     * Creates the relevant services from the given {@code artifact}.
     * 
     * @param artifact the artifact
     * @return the services (may be empty or <b>null</b> for none)
     */
    protected abstract List<Service> createServices(YamlArtifact artifact);
    
    /**
     * Returns the spring environment.
     * 
     * @return the spring environment
     */
    protected Environment getEnvironment() {
        return environment;
    }
    
    /**
     * Parses the external binder connections and turns them into external routing keys. [public for testing]
     * 
     * @param args the command line arguments
     * @param externalBindingConsumer consumer for the external connections
     */
    public static void parseExternConnections(String[] args, Consumer<String> externalBindingConsumer) {
        final String leadIn = CmdLine.PARAM_PREFIX + OPT_SPRING_BINDINGS_PREFIX;
        final String leadOut = OPT_SPRING_BINDER_POSTFIX + CmdLine.PARAM_VALUE_SEP + EXTERNAL_BINDER_NAME;
        for (String a: args) {
            if (a.startsWith(leadIn) && a.endsWith(leadOut)) {
                externalBindingConsumer.accept(
                    a.substring(0, a.length() - leadOut.length()).substring(leadIn.length()));
            }
        }
    }
    
    /**
     * Augments the command line arguments by spring cloud stream binder destination args containing the 
     * application id if {@link #getAppId()} is given.
     * 
     * @param args the command line arguments
     * @return the (augmented) command line arguments
     */
    public static String[] augmentByAppId(String[] args) {
        return augmentByAppId(args, getAppId(), () -> ResourceLoader.getResourceAsStream("application.yml"));
    }
    
    /**
     * Augments the command line arguments by spring cloud stream binder destination args containing the 
     * application id if given. [public for testing]
     * 
     * @param args the command line arguments
     * @param appId the application id, may be empty
     * @param appYamlSupplier provides access to the input stream (called if needed)
     * @return the (augmented) command line arguments
     */
    public static String[] augmentByAppId(String[] args, String appId, Supplier<InputStream> appYamlSupplier) {
        if (appId != null && appId.length() > 0) {
            appId = appId.replace(ServiceBase.APPLICATION_SEPARATOR, ""); // as before adding @, unsure
            List<String> res = CollectionUtils.toList(args);
            Object data = new Object();
            try {
                InputStream yamlIn = appYamlSupplier.get();
                Iterator<Object> it = Yaml.getInstance().loadAll(yamlIn);
                if (it.hasNext()) {
                    data = it.next(); // for now, ignore the other sub-documents here
                }
                FileUtils.closeQuietly(yamlIn);
            } catch (IOException e) {
                LoggerFactory.getLogger(Starter.class).warn("Cannot read from application {} YAML: {}", appId, 
                    e.getMessage());
            }
            LoggerFactory.getLogger(Starter.class).info("Augmenting stream bindings by appId {}", appId);
            final String bindingsPath = "spring.cloud.stream.bindings";
            final String[] bindingsFieldPath = bindingsPath.split("\\.");
            Map<Object, Object> tmp = YamlFile.getFieldAsMap(data, bindingsFieldPath);
            for (Map.Entry<Object, Object> ent : tmp.entrySet()) {
                String dest = YamlFile.getFieldAsString(ent.getValue(), "destination", null);
                if (null != dest) {
                    String[] dTmp = dest.split(",");
                    dest = "";
                    for (String d : dTmp) {
                        if (dest.length() > 0) {
                            dest = dest + ",";
                        }
                        dest = dest + appId + "_" + d;
                    }
                    String mod = CmdLine.PARAM_PREFIX  + bindingsPath + "." + ent.getKey() + ".destination" 
                        + CmdLine.PARAM_VALUE_SEP + dest;
                    res.add(mod);
                    LoggerFactory.getLogger(Starter.class).info(" Additional cmdline entry: {}", mod);
                }
            }
            args = res.toArray(new String[res.size()]);
        }
        return args;
    }
    
    /**
     * Main function.
     * 
     * @param cls the class to start
     * @param args command line arguments
     */
    public static void main(Class<? extends Starter> cls, String[] args) {
        registerDefaultPlugins(a -> {
            // start spring cloud app
            SpringApplication app = new SpringApplication(cls);
            ctx = app.run(a);
        });
        registerPlugin("springBroker", new TestSpringBroker());
        transferArgsToEnvironment(args);
        
        YamlSetup.loadPluginSetup(args);
        loadOktoPlugins();
        PluginSetup.setClassLoader(Starter.class.getClassLoader());
        PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE); // "local" plugins
        Yaml.resolveInstance(); // if not loaded via JSL in YamlSetup, re-try via plugin manager

        ResourceLoader.addTestExcludeFilters(); // exclude test JARs
        ResourceLoader.registerResourceResolver(new SpringResourceResolver()); // ensure spring resolution
        final String[] tmpArgs = args;
        setLocalTransportSetupSupplier(setup -> {
            TransportSetup result = null;
            TransportSetup external = YamlSetup.getExternalTransportSetup(tmpArgs);
            TransportSetup internal = YamlSetup.getInternalTransportSetup(tmpArgs);
            if (null != internal && null != external) {
                if (internal.getPort() != external.getPort()) {
                    result = internal;
                } else if (internal.getHost() != null && external.getHost() != null 
                     && !internal.getHost().equals(external.getHost())) {
                    result = internal;
                }
            }
            return result;
        });
        Starter.parse(args);
        determineStartupSequence();
        if (!startServer(args)) {
            parseExternConnections(args, e -> Transport.addGlobalRoutingKey(e));
            getSetup(); // ensure instance
            args = augmentByAppId(args);
            runPlugin(args);
        } // else starts server in parse
    }

    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * By default, do autostart.
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * 
     * @see #determineStartupSequence()
     */
    public static void mapService(Service service) {
        mapService(service, null);
    }
    
    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * By default, do autostart.
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * @param after code to be executed after mapping, may be <b>null</b>
     * 
     * @see #determineStartupSequence()
     */
    public static void mapService(Service service, Runnable after) {
        if (null != startupSequence) {
            String serviceId = service.getId();
            for (int i = 0; i < startupSequence.size(); i++) {
                ServiceForMapping s = startupSequence.get(i);
                if (s != null && s.mapping.get() == null && s.id.equals(serviceId)) {
                    s.mapping.set(() -> {
                        de.iip_ecosphere.platform.services.environment.Starter.mapService(service, 
                            service.getKind() != ServiceKind.SOURCE_SERVICE);
                        if (null != after) {
                            after.run();
                        }
                    });
                }
            }
        } else {
            de.iip_ecosphere.platform.services.environment.Starter.mapService(service);
            if (null != after) {
                after.run();
            }
        }
        startDataForMappedServices();
    }

    /**
     * Starts data for mapped services, delayed {@link ServiceState#RUNNING}.
     */
    private static void startDataForMappedServices() {
        // it's ok if it maps more services...
        startDataForMappedServices(c -> expectedServiceCount > 0 && c >= expectedServiceCount);
    }
    
}
