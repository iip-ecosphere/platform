package de.iip_ecosphere.platform.support.aas.basyx2.apps.submodelRegistry;

import de.iip_ecosphere.platform.support.aas.basyx2.apps.common.ClasspathResourceLoadingInitializer;

/**
 * Initializer for submodel registries.
 */
public class SubmodelRegistrySpringAppInitializer extends ClasspathResourceLoadingInitializer {
    
    /**
     * Creates an instance.
     */
    public SubmodelRegistrySpringAppInitializer() {
        super(".*basyx\\.submodelregistry-service-\\d.*");
    }
    
}