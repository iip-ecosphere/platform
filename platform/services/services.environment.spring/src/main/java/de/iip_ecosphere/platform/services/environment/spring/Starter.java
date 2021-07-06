package de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceMapper;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClient;
import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;

@ComponentScan(basePackageClasses = MetricsProvider.class)
@EnableScheduling
@Import({MetricsProvider.class})
@Component
public abstract class Starter extends de.iip_ecosphere.platform.services.environment.Starter {

    private static ConfigurableApplicationContext ctx;
    private static Environment environment;

    /**
     * Creates an instance.
     * 
     * @param env the Spring environment
     */
    @Autowired
    public Starter(Environment env) {
        environment = env;

        // start the command server
        try {
            // assuming that deployment.yml variants for testing contain the same service descriptions (modulo 
            // technical information)
            YamlArtifact art = YamlArtifact.readFromYaml(
                getClass().getClassLoader().getResourceAsStream("/deployment.yml"));
            List<Service> services = createServices(art);
            if (null != services) { 
                ServiceMapper mapper = new ServiceMapper(Starter.getProtocolBuilder());
                MetricsExtractorRestClient metricsClient = createMetricsClient(environment);
                for (Service service : services) {
                    mapService(mapper, service, metricsClient);
                }
            }
            Starter.start();
        } catch (IOException e) {
            System.out.println("Cannot find service descriptor/start command server.");
        }
        
    }

    /**
     * Maps a service through a given mapper and the default metrics client. [Convenience method for generation]
     * 
     * @param mapper the service mapper instance 
     * @param service the service to be mapped
     * 
     * @see #createMetricsClient()
     * @see #mapService(ServiceMapper, Service, MetricsExtractorRestClient)
     */
    public static void mapService(ServiceMapper mapper, Service service) {
        mapService(mapper, service, createMetricsClient());
    }

    /**
     * Maps a service through a given mapper and metrics client.
     * 
     * @param mapper the service mapper instance 
     * @param service the service to be mapped
     * @param metricsClient the metrics client (may be <b>null</b> for none)
     */
    public static void mapService(ServiceMapper mapper, Service service, MetricsExtractorRestClient metricsClient) {
        mapper.mapService(service);
        if (null != metricsClient) {
            mapper.mapMetrics(service, metricsClient);
        }
    }

    /**
     * Creates a metrics client.
     * 
     * @param environment the Spring environment
     * @return the metrics REST client, may be <b>null</b>
     */
    public static MetricsExtractorRestClient createMetricsClient(Environment environment) {
        MetricsExtractorRestClient metricsClient = null;
        String tmp = environment.getProperty("server.port");
        try {
            int port = Integer.parseInt(tmp);
            metricsClient = new MetricsExtractorRestClient("localhost", port);
        } catch (NumberFormatException e) {
            System.out.println("Cannot read spring application server port: " + tmp + "; " + e.getMessage());    
        }
        return metricsClient;
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
     * Main function.
     * 
     * @param cls the class to start
     * @param args command line arguments
     */
    public static void main(Class<? extends Starter> cls, String[] args) {
        Starter.parse(args);
        // start spring cloud app
        SpringApplication app = new SpringApplication(cls);
        ctx = app.run(args);
    }
    
}
