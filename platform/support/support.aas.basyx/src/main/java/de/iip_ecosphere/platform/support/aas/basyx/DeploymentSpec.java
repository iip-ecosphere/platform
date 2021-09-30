package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;

import de.iip_ecosphere.platform.support.Endpoint;

/**
 * Stores basic common deployment information.
 * 
 * @author Holger Eichelberger, SSE
 */
class DeploymentSpec {
    
    private Endpoint endpoint;
    private BaSyxContextConfiguration contextConfig;
    private BaSyxContext context;
    private IAASRegistry registry;
    private Map<String, BaSyxAasDescriptor> descriptors = new HashMap<>();
    
    /**
     * Creates a deployment specification without setting the endpoint, context and context configuration, e.g., 
     * to set the registry later and to register descriptors.
     */
    DeploymentSpec() {
    }
    
    /**
     * Creates an unencrypted deployment specification based on a given {@code endpoint}, but without setting the 
     * registry.
     * 
     * @param endpoint the endpoint
     */
    DeploymentSpec(Endpoint endpoint) {
        this(endpoint, "", false, null, null);
    }

    /**
     * Creates an unencrypted deployment specification based on a given {@code endpoint}, but without setting the 
     * registry.
     * 
     * @param endpoint the endpoint
     * @param docPath the document path, may be empty
     */
    DeploymentSpec(Endpoint endpoint, String docPath) {
        this(endpoint, docPath, false, null, null);
    }

    /**
     * Creates a deployment specification based on a given {@code endpoint}, but without setting the registry.
     * 
     * @param endpoint the endpoint
     * @param docPath the document path, may be empty
     * @param isSecuredCon do we need a secure connection, then {@code keyPath} and {@code keyPass} must be given; else 
     *     they are ignored
     * @param keyPass password of the SSL key (optional if {@code isSecureCon} is {@code false})
     * @param keyPath path to the SSL certificate (optional if {@code isSecureCon} is {@code false})
     */
    DeploymentSpec(Endpoint endpoint, String docPath, boolean isSecuredCon, String keyPath,  String keyPass) {
        this.endpoint = endpoint;
        if (isSecuredCon) {
            this.context = new BaSyxContext(endpoint.getEndpoint(), docPath, endpoint.getHost(), endpoint.getPort(), 
                true, keyPath, keyPass);
        } else {
            this.context = new BaSyxContext(endpoint.getEndpoint(), docPath, endpoint.getHost(), endpoint.getPort());
        }
        this.contextConfig = new BaSyxContextConfiguration(
            endpoint.getEndpoint(), "", endpoint.getHost(), endpoint.getPort()) {
            
            @Override
            public BaSyxContext createBaSyxContext() {
                return context;
            }
            
        };
    }
    
    /**
     * Returns the BaSyx context.
     * 
     * @return the BaSyx context
     */
    BaSyxContext getContext() {
        return context;
    }

    /**
     * Returns the endpoint.
     * 
     * @return the endpoint
     */
    Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Returns the server context configuration.
     * 
     * @return the context configuration (may be <b>null</b> depending on constructor use)
     */
    BaSyxContextConfiguration getContextConfiguration() {
        return contextConfig;
    }
    
    /**
     * Adds a descriptor.
     * 
     * @param idShort the short id of the AAS described by the descriptor
     * @param descriptor the descriptor
     */
    void putDescriptor(String idShort, BaSyxAasDescriptor descriptor) {
        descriptors.put(idShort, descriptor);
    }
    
    /**
     * Returns a stored descriptor.
     * 
     * @param idShort the short id of the AAS/descriptor
     * @return the descriptor (may be <b>null</b> if unknown)
     */
    BaSyxAasDescriptor getDescriptor(String idShort) {
        return descriptors.get(idShort);
    }

    /**
     * Returns the BaSyx registry.
     * 
     * @return the registry (may be <b>null</b> depending on constructor use)
     */
    IAASRegistry getRegistry() {
        return registry;
    }

    /**
     * Defines the BaSyx registry.
     * 
     * @param registry the registry (may be <b>null</b> depending on constructor use)
     */
    void setRegistry(IAASRegistry registry) {
        this.registry = registry;
    }
    
}
