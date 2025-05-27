/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.haskind.ModelingKind;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;

/**
 * Variation points for version adjustments among BaSyx versions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VersionAdjustment {

    /**
     * The default server creator directly utilizing the underlying {@link BaSyxHTTPServer}. The default is to use the 
     * {@link BaSyxHTTPServer} as provided, without adjustments, TLS setup if defined and with limited/if at all 
     * authentication/RBAC setup.
     */
    public static final ServerCreator DEFAULT_SERVER_CREATOR = new ServerCreator() {

        @Override
        public Server createServer(DeploymentSpec spec, SetupSpec sSpec, AasComponent component) {
            BaSyxHTTPServer server = new BaSyxHTTPServer(spec.getContext());
            Server result = new Server() {

                @Override
                public Server start() {
                    server.start();
                    return this;
                }

                @Override
                public void stop(boolean dispose) {
                    server.shutdown();
                }

            };
            return result;
        }
        
    };

    public static final RegistryDeploymentServerCreator DEFAULT_DEPLOYMENT_SERVER_CREATOR 
        = new RegistryDeploymentServerCreator() {

            // checkstyle: stop parameter number check

            @Override
            public BaSyxAbstractAasServer createRegistryDeploymentServer(DeploymentSpec deploymentSpec, SetupSpec spec,
                    AasComponent component, String regUrl, AASServerBackend backend, String... options) {
                return new BaSyxRegistryDeploymentAasServer(deploymentSpec, spec, component, regUrl, backend, options);
            }
        
            // checkstyle: resume parameter number check

        };

    private static Map<Class<?>, SetPropertyKind> setPropertyKind = new HashMap<>();
    private static Map<Class<?>, OperationInvoke> invokeOperation = new HashMap<>();
    private static Map<Class<?>, SetBearerTokenAuthenticationConfiguration> setBearerAuthenticationTokenConf 
        = new HashMap<>();
    private static Map<Class<?>, SetupBaSyxAASServerConfiguration> setupBaSyxAASServerConfiguration
        = new HashMap<>();
    private static ServerCreator serverCreator = DEFAULT_SERVER_CREATOR;
    private static RegistryDeploymentServerCreator deploymentServerCreator = DEFAULT_DEPLOYMENT_SERVER_CREATOR;
    
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
    
    /**
     * Creates server instances.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ServerCreator {

        /**
         * Creates a BaSyx server instance.
         * 
         * @param dSpec the deployment specification
         * @param sSpec the setup specification
         * @param component the component to create the server for
         * @return the server instance
         */
        public Server createServer(DeploymentSpec dSpec, SetupSpec sSpec, AasComponent component);
        
    }
    
    /**
     * Creates a BaSyx Server.
     * 
     * @param dSpec the deployment specification
     * @param sSpec the setup specification
     * @param component the component to create the server for
     * @return the server instance
     */
    public static Server createBaSyxServer(DeploymentSpec dSpec, SetupSpec sSpec, AasComponent component) {
        return serverCreator.createServer(dSpec, sSpec, component);
    }

    /**
     * Sets up the server creator. 
     * 
     * @param creator the creator (default {@link #DEFAULT_SERVER_CREATOR})
     */
    public static void setupBaSyxServerCreator(ServerCreator creator) {
        if (null != creator) {
            serverCreator = creator;
        }
    }

    /**
     * Creates a registry deployment server.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface RegistryDeploymentServerCreator {

        // checkstyle: stop parameter number check

        /**
         * Creates a new BaSyx registry deployment server.
         * 
         * @param deploymentSpec the deployment set instance for runtime deployments
         * @param spec the setup specification
         * @param component the component being configured
         * @param regUrl the registryUR
         * @param backend the AAS server backend to use
         * @param options for server creation
         */
        public BaSyxAbstractAasServer createRegistryDeploymentServer(DeploymentSpec deploymentSpec, SetupSpec spec, 
            AasComponent component, String regUrl, AASServerBackend backend, String... options);

        // checkstyle: resume parameter number check

    }

    // checkstyle: stop parameter number check

    /**
     * Creates a new BaSyx registry deployment server.
     * 
     * @param deploymentSpec the deployment set instance for runtime deployments
     * @param spec the setup specification
     * @param component the component being configured
     * @param regUrl the registryUR
     * @param backend the AAS server backend to use
     * @param options for server creation
     */
    public static BaSyxAbstractAasServer createRegistryDeploymentServer(DeploymentSpec deploymentSpec, SetupSpec spec, 
        AasComponent component, String regUrl, AASServerBackend backend, String... options) {
        return deploymentServerCreator.createRegistryDeploymentServer(deploymentSpec, spec, component, regUrl, 
            backend, options);
    }

    // checkstyle: resume parameter number check

    /**
     * Sets up the deployment server creator. 
     * 
     * @param creator the creator (default {@link #DEFAULT_DEPLOYMENT_SERVER_CREATOR})
     */
    public static void setupRegistryDeploymentServerCreator(RegistryDeploymentServerCreator creator) {
        if (null != creator) {
            deploymentServerCreator = creator;
        }
    }

}
