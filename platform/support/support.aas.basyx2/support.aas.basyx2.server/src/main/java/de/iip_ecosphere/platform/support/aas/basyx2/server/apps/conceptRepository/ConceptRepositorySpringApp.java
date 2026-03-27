package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.conceptRepository;

import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.ConceptDescriptionRepository;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.ConceptDescriptionRepositoryFactory;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.backend.CrudConceptDescriptionRepositoryFactory;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.backend.InMemoryConceptDescriptionBackend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames;

/**
 * Spring application for starting an concept description repository  service.
 * 
 * @author Monika Staciwa, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { BaSyxNames.PACKAGE_BASYX_CONCEPTREPO }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = ConceptRepositoryTypeFilter.class))
@Component
public class ConceptRepositorySpringApp {

    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ConceptRepositorySpringApp.class, args);
    }
    
    /**
     * Returns the AAS concept description backend.
     * 
     * @return the backend
     */
    @Bean
    public InMemoryConceptDescriptionBackend backend() {
        return new InMemoryConceptDescriptionBackend();
    }
    
    /**
     * Returns the concept description factory.
     * 
     * @param backend the backend, as injected by {@link #backend()}
     * @return the factory
     */
    @Bean 
    public static ConceptDescriptionRepositoryFactory getConceptDescFactory(InMemoryConceptDescriptionBackend backend) {
        return CrudConceptDescriptionRepositoryFactory.builder().backend(backend).buildFactory();
    }
    
    /**
     * Returns the concept description repository service.
     * 
     * @param backend the backend, as injected by {@link #backend()}
     * @return the repository
     */
    @Primary
    @Bean 
    public static ConceptDescriptionRepository getConceptDescRepository(InMemoryConceptDescriptionBackend backend) {
        return getConceptDescFactory(backend).create();
    }
    
}
