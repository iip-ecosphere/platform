package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.autodiscovery;

import org.eclipse.digitaltwin.basyx.aasdiscoveryservice.backend.CrudAasDiscoveryFactory;
import org.eclipse.digitaltwin.basyx.aasdiscoveryservice.backend.inmemory.InMemoryAasDiscoveryDocumentBackend;
import org.eclipse.digitaltwin.basyx.aasdiscoveryservice.core.AasDiscoveryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames;

/**
 * Spring application for starting an in-memory Autodiscovery service.
 * 
 * @author Monika Staciwa, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { BaSyxNames.PACKAGE_BASYX }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutodiscoveryTypeFilter.class))
@Component
@Import({
    org.eclipse.digitaltwin.basyx.aasdiscoveryservice.http.AasDiscoveryServiceHTTPConfiguration.class
})
public class AasAutodiscoverySpringApp {

    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AasAutodiscoverySpringApp.class, args);
    }

    /**
     * Returns the AAS discovery document backend.
     * 
     * @return the backend
     */
    @Bean
    public InMemoryAasDiscoveryDocumentBackend backend() {
        return new InMemoryAasDiscoveryDocumentBackend();
    }
    
    /**
     * Returns the AAS discovery service.
     * 
     * @param backend the backend, as injected by {@link #backend()}
     * @return the disovery service
     */
    @Bean
    public AasDiscoveryService aasDiscoveryService(InMemoryAasDiscoveryDocumentBackend backend) {
        return new CrudAasDiscoveryFactory(backend, "default").create();
    } 
    
}
