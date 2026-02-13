package de.iip_ecosphere.platform.support.aas;

/**
 * Optional server (instance) factory if server implementation is in a separately loadable plugin. Plugin-id must 
 * resolve to the AAS plugin ID ending with {@link AasFactory#POSTFIX_ID_SERVER}.
 *  
 * @author Holger Eichelberger, SSE
 */
public interface AasServerFactoryDescriptor {
    
    /**
     * Creates a deployment recipe for unencrypted deployment.
     * 
     * @param spec the setup specification
     * @return the deployment recipe instance (may be <b>null</b> if no AAS implementation is registered)
     */
    public DeploymentRecipe createDeploymentRecipe(SetupSpec spec);
    
    /**
     * Creates a server recipe. 
     * 
     * @return the server recipe (may be <b>null</b> for none)
     */
    public ServerRecipe createServerRecipe();

}