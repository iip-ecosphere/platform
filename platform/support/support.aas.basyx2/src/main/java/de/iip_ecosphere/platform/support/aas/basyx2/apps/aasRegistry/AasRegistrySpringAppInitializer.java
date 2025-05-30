package de.iip_ecosphere.platform.support.aas.basyx2.apps.aasRegistry;

import de.iip_ecosphere.platform.support.aas.basyx2.apps.common.ClasspathResourceLoadingInitializer;

/**
 * Initializer for AAS registries.
 */
public class AasRegistrySpringAppInitializer extends ClasspathResourceLoadingInitializer {

    /**
     * Creates an instance.
     */
    public AasRegistrySpringAppInitializer() {
        super(".*basyx\\.aasregistry-service-\\d.*");
    }
    
}