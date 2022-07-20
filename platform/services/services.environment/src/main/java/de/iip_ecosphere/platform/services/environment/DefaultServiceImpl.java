package de.iip_ecosphere.platform.services.environment;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Default service implementation realizing the left open methods of {@link AbstractService} empty. Uses 
 * {@link #reconfigure(Map, ParameterConfigurerProvider, boolean, ServiceState)},  
 * and {@link #rollbackReconfigurationOnFailure()} to generically implement {@link #reconfigure(Map)}, i.e., define
 * the relevant parameters for
 */
public class DefaultServiceImpl extends AbstractService {

    /**
     * Fallback constructor setting most fields to "empty" default values.
     * 
     * @param kind the service kind
     */
    protected DefaultServiceImpl(ServiceKind kind) {
        super(kind);
    }

    /**
     * Fallback constructor setting most fields to "empty" default values.
     * 
     * @param id the id of the service
     * @param kind the service kind
     */
    protected DefaultServiceImpl(String id, ServiceKind kind) {
        super(id, kind);
    }

    // checkstyle: stop parameter number check

    /**
     * Creates a default service.
     * 
     * @param id the id of the service
     * @param name the name of the service
     * @param version the version of the service
     * @param description a description of the service, may be empty
     * @param isDeployable whether the service is decentrally deployable
     * @param isTopLevel whether the service is a top-level (non-nested) service
     * @param kind the service kind
     */
    protected DefaultServiceImpl(String id, String name, Version version, String description, boolean isDeployable, 
        boolean isTopLevel, ServiceKind kind) {
        super(id, name, version, description, isDeployable, isTopLevel, kind);
    }

    // checkstyle: resume parameter number check

    /**
     * Creates a default service from YAML information.
     * 
     * @param yaml the service information as read from YAML
     */
    protected DefaultServiceImpl(YamlService yaml) {
        super(yaml);
    }
    
    /**
     * Creates an abstract service from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    protected DefaultServiceImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
    }

    @Override
    public void update(URI location) throws ExecutionException {
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
    }

}
