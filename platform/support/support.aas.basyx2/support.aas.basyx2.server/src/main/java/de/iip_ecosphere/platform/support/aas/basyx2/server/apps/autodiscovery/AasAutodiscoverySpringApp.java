package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.autodiscovery;

import org.eclipse.digitaltwin.basyx.aasdiscoveryservice.client.ConnectedAasDiscoveryService;
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
import de.iip_ecosphere.platform.support.setup.CmdLine;

/**
 * Spring application for starting an in-memory Autodiscovery service.
 * 
 * @author Monika Staciwa, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { BaSyxNames.PACKAGE_BASYX, BaSyxNames.PACKAGE_PLUGIN_BASYX_SERVER_COMMON }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutodiscoveryTypeFilter.class))
@Component
@Import({
    org.eclipse.digitaltwin.basyx.aasdiscoveryservice.http.AasDiscoveryServiceHTTPConfiguration.class
})
public class AasAutodiscoverySpringApp {

    private static String baseUrl;

    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        baseUrl = CmdLine.getArg(args, "baseURL", "http://localhost"); // initial
        SpringApplication.run(AasAutodiscoverySpringApp.class, args);
    }
    
    /**
     * Returns the AAS discovery service.
     * 
     * @return the disovery service
     */
    @Bean
    public AasDiscoveryService aasDiscoveryService() {
        return new ConnectedAasDiscoveryService(baseUrl);
    } 
    
}
