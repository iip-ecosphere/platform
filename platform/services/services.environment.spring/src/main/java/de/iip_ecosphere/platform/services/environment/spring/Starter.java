package de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;

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
    private final Environment environment;

    /**
     * Creates an instance.
     * 
     * @param environment the Spring environment
     */
    @Autowired
    public Starter(Environment environment) {
        this.environment = environment;

        // start the command server
        try {
            // assuming that deployment.yml variants for testing contain the same service descriptions (modulo 
            // technical information)
            YamlArtifact art = YamlArtifact.readFromYaml(
                getClass().getClassLoader().getResourceAsStream("/deployment.yml"));
            // in a real service, this may happen differently
            ServiceMapper mapper = new ServiceMapper(Starter.getProtocolBuilder());
            MetricsExtractorRestClient metricsClient = null;
            String tmp = environment.getProperty("server.port");
            try {
                int port = Integer.parseInt(tmp);
                metricsClient = new MetricsExtractorRestClient("localhost", port);
            } catch (NumberFormatException e) {
                System.out.println("Cannot read spring application server port: " + tmp + "; " + e.getMessage());    
            }
            for (YamlService service : art.getServices()) {
                Service s = createService(service);
                mapper.mapService(s);
                if (null != metricsClient) {
                    mapper.mapMetrics(s, metricsClient);
                }
            }
            Starter.start();
        } catch (IOException e) {
            System.out.println("Cannot find service descriptor/start command server.");
        }
        
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
     * Creates a service instance.
     * 
     * @param service the service description information
     * @return the service instance
     */
    protected abstract Service createService(YamlService service);
    
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
