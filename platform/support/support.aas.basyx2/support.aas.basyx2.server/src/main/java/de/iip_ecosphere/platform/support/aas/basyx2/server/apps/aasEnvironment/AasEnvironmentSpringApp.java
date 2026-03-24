package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.aasEnvironment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames;

/**
 * Spring application for starting an in-memory AAS environment service.
 * 
 * @author Monika Staciwa, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { BaSyxNames.PACKAGE_BASYX_AASENV }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AasEnvironmentTypeFilter.class))
@Component
@Import({
    org.eclipse.digitaltwin.basyx.aasenvironment.http.AASEnvironmentConfiguration.class,
    org.eclipse.digitaltwin.basyx.aasenvironment.http.AasEnvironmentApiHTTPController.class
})
public class AasEnvironmentSpringApp {
    
    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AasEnvironmentSpringApp.class, args);
    }
    
/*  Unclear how this relates to the instances creatd in the other apps.
 
    @Bean 
    public AasBackendProvider getAasBackendProvider() {
        return new AasInMemoryBackendProvider();
    }
    
    @Bean 
    public FileRepository getFileRepository() {
        return new InMemoryFileRepository();
    }
    
    @Bean 
    public AasRepository getAasRepository(AasBackendProvider backend, FileRepository fileRepository) {
        return new CrudAasRepository(backend, new InMemoryAasServiceFactory(fileRepository), "");
    }
    
    @Bean 
    public SubmodelRepository getSubmodelRepository(FileRepository fileRepository) {
        return new SimpleSubmodelRepositoryFactory(
                new SubmodelInMemoryBackendProvider(), 
                new InMemorySubmodelServiceFactory(fileRepository))
                .create();
    }
    
    @Bean
    public InMemoryConceptDescriptionBackend backend() {
        return new InMemoryConceptDescriptionBackend();
    }
    
    @Bean
    public ConceptDescriptionRepository getConceptDescriptionRepository(InMemoryConceptDescriptionBackend backend) {
        return CrudConceptDescriptionRepositoryFactory.builder().backend(backend).create();
    }
    
    @Primary
    @Bean
    public DefaultAASEnvironment aasEnvironment(AasRepository aasRepository, 
            SubmodelRepository submodelRepository, ConceptDescriptionRepository conceptDescriptionRepository) {
        return new DefaultAASEnvironment(aasRepository, submodelRepository, conceptDescriptionRepository);
    }*/

}
