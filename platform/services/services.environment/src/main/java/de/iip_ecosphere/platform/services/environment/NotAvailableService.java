package de.iip_ecosphere.platform.services.environment;

import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * Not-available service acting as stub/dummy with service id.
 * 
 * @author Holger Eichelberger, SSE
 */
class NotAvailableService extends AbstractService {

    /**
     * Creates a not-available service instance.
     * 
     * @param id the service Id
     */
    protected NotAvailableService(String id) {
        super(id, ServiceKind.NOT_AVAILABLE); // 
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