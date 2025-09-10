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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import de.iip_ecosphere.platform.services.environment.ServiceMapper;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClient;
import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import de.iip_ecosphere.platform.support.yaml.YamlFile;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
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
    
    private static ConfigurableApplicationContext ctx;
    private static Environment environment;
    private static int port = 8080; // assumed default
    
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
        // start the command server
        try {
            // assuming that deployment.yml variants for testing contain the same service descriptions (modulo 
            // technical information)
            YamlArtifact art = YamlArtifact.readFromYaml(ResourceLoader.getResourceAsStream("deployment.yml"));
            List<Service> services = createServices(art);
            if (null != services) { 
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
        PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE); // "local" plugins
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
        if (!startServer(args)) {
            parseExternConnections(args, e -> Transport.addGlobalRoutingKey(e));
            getSetup(); // ensure instance
            args = augmentByAppId(args);
            runPlugin(args);
        } // else starts server in parse
    }
    
}
