package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.haskind.ModelingKind;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;

public class VersionAdjustment {

    private static Map<Class<?>, SetPropertyKind> setPropertyKind = new HashMap<>();
    private static Map<Class<?>, OperationInvoke> invokeOperation = new HashMap<>();
    private static Map<Class<?>, SetBearerTokenAuthenticationConfiguration> setBearerAuthenticationTokenConf 
        = new HashMap<>();
    private static Map<Class<?>, SetupBaSyxAASServerConfiguration> setupBaSyxAASServerConfiguration
        = new HashMap<>();
    
    /**
     * Function interface to set the modeling kind of an operation.
     *  
     * @author Holger Eichelberger, SSE
     */
    public interface SetPropertyKind {
        
        /**
         * Sets {@code kind} on {@code property}.
         * 
         * @param property the property
         * @param kind the kind
         */
        public void set(Property property, ModelingKind kind);
        
    }
    
    /**
     * Function interface to invoke an operation.
     *  
     * @author Holger Eichelberger, SSE
     */
    public interface OperationInvoke {
        
        /**
         * invokes {@code operation} with {@code args} and returns value of the invokation.
         * 
         * @param operation the operation
         * @param args the arguments
         * @return the operation value
         */
        public Object invoke(IOperation operation, Object[] args);
        
    }
    
    /**
     * Function interface to set the bearer token authentication configuration.
     *  
     * @author Holger Eichelberger, SSE
     */
    public interface SetBearerTokenAuthenticationConfiguration {
        
        /**
         * Sets the bearer authentication configuration.
         * 
         * @param context the target context
         * @param issuerUri the URI of the issuer
         * @param jwkSetUri unclear
         * @param requiredAud unclear (may be <b>null</b>)
         * @throws IllegalArgumentException if the passed in information is invalid
         */
        public void set(BaSyxContext context, String issuerUri, String jwkSetUri, String requiredAud);
        
    }
    
    /**
     * Sets up a server configuration.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SetupBaSyxAASServerConfiguration {

        /**
         * Sets up the server configuration.
         * 
         * @param cfg the configuration instance
         */
        public void setup(BaSyxAASServerConfiguration cfg);
        
    }
    
    /**
     * Registers the {@code setter}.
     * 
     * @param property the BaSyx property class to register the setter for
     * @param setter the setter instance
     */
    public static void registerSetPropertyKind(Class<? extends Property> property, SetPropertyKind setter) {
        setPropertyKind.put(property, setter);
    }

    /**
     * Sets {@code kind} on {@code property}.
     * 
     * @param property the property
     * @param kind the kind
     */
    public static void setPropertyKind(Property property, ModelingKind kind) {
        setPropertyKind.get(property.getClass()).set(property, kind);
    }

    /**
     * Registers the {@code invoker}.
     * 
     * @param operation the BaSyx operation class
     * @param invoker the operation invoker
     */
    public static void registerOperationInvoke(Class<? extends IOperation> operation, OperationInvoke invoker) {
        invokeOperation.put(operation, invoker);
    }
    
    /**
     * Invokes {@code operation} with the given {@code args}.
     * 
     * @param operation the operation
     * @param args the arguments
     * @return the operation return value
     */
    public static Object operationInvoke(IOperation operation, Object[] args) {
        return invokeOperation.get(operation.getClass()).invoke(operation, args);
    }

    /**
     * Registers the {@code setter}.
     * 
     * @param context the context class to register the setter for
     * @param setter the authentication setter
     */
    public static void registerSetBearerTokenAuthenticationConfiguration(Class<? extends BaSyxContext> context, 
        SetBearerTokenAuthenticationConfiguration setter) {
        setBearerAuthenticationTokenConf.put(context, setter);
    }
    
    /**
     * Sets the bearer authentication configuration.
     * 
     * @param context the target context
     * @param issuerUri the URI of the issuer
     * @param jwkSetUri unclear
     * @param requiredAud unclear (may be <b>null</b>)
     * @throws IllegalArgumentException if the passed in information is invalid
     */
    public static void setBearerTokenAuthenticationConfiguration(BaSyxContext context, String issuerUri, 
        String jwkSetUri, String requiredAud) {
        setBearerAuthenticationTokenConf.get(context.getClass()).set(context, issuerUri, jwkSetUri, requiredAud);
    }

    /**
     * Registers the {@code initializer}.
     * 
     * @param config the configuration class to register the initializer for
     * @param initializer the initializer
     */
    public static void registerSetupBaSyxAASServerConfiguration(
        Class<? extends BaSyxAASServerConfiguration> config, SetupBaSyxAASServerConfiguration initializer) {
        setupBaSyxAASServerConfiguration.put(config, initializer);
    }
    
    /**
     * Initializes the server configuration.
     * 
     * @param cfg the configuration
     */
    public static void setupBaSyxAASServerConfiguration(BaSyxAASServerConfiguration cfg) {
        setupBaSyxAASServerConfiguration.get(cfg.getClass()).setup(cfg);
    }

}
