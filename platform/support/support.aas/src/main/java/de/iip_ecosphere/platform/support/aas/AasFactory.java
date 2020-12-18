/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * A customizable factory for creating AAS instances independent of the underlying implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AasFactory {

    public static final String DEFAULT_PROTOCOL = "";
    
    /**
     * A dummy AAS factory instance that intentionally does nothing. This is the default implementation,
     * but it will never be effective if there is an implementation available via the service loader.
     */
    public static final AasFactory DUMMY = new AasFactory() {

        @Override
        public String getName() {
            return "Dummy";
        }

        @Override
        public AasBuilder createAasBuilder(String idShort, String urn) {
            return null;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort) {
            return null;
        }

        @Override
        public Aas retrieveAas(String host, int port, String endpointPath, String urn) throws IOException {
            return null;
        }

        @Override
        public DeploymentRecipe createDeploymentRecipe(String host, int port) {
            return null;
        }

        @Override
        public DeploymentRecipe createDeploymentRecipe(String contextPath, String host, int port) {
            return null;
        }

        @Override
        public PersistenceRecipe createPersistenceRecipe() {
            return null;
        }

        @Override
        public String[] getProtocols() {
            return new String[]{DEFAULT_PROTOCOL};
        }

        @Override
        public InvocablesCreator createInvocablesCreator(String protocol, String host, int port) {
            return null;
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(String protocol, int port) {
            return null;
        }
        
    };

    private static final Logger LOGGER = Logger.getLogger(AasFactory.class.getName());
    // instance-based to allow later dependency injection
    private static AasFactory instance = DUMMY;
    
    /**
     * Returns the actual instance.
     * 
     * @return the actual instance
     */
    public static AasFactory getInstance() {
        if (DUMMY == instance) {
            ServiceLoader<AasFactoryDescriptor> loader = ServiceLoader.load(AasFactoryDescriptor.class);
            Optional<AasFactoryDescriptor> first = loader.findFirst();
            if (first.isPresent()) {
                instance = first.get().createInstance();
                if (null != instance) {
                    LOGGER.fine("AAS factory implementation registered: " + instance.getClass().getName());
                }
            } else {
                LOGGER.severe("No AAS factory implementation known.");
            }
        }
        return instance;
    }
    
    /**
     * Defines the actual instance. This shall be used when there is intentionally no AAS implementation, e.g., 
     * in test situations.
     * 
     * @param newInstance the new instance (may be <b>null</b> but then the call is without effect)
     * @return AAS instance before setting, may be <b>null</b>
     */
    public static AasFactory setInstance(AasFactory newInstance) {
        AasFactory oldInstance = instance;
        if (null != newInstance) {
            instance = newInstance;
        }
        return oldInstance;
    }
    
    /**
     * Returns the (descriptive) name of the factory.
     * 
     * @return the name of the factory
     */
    public abstract String getName();

    /**
     * Creates an AAS builder instance.
     * 
     * @param idShort the shortId of the AAS
     * @param urn the uniform resource name of the AAS
     * 
     * @return the AAS builder instance (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
     */
    public abstract AasBuilder createAasBuilder(String idShort, String urn);

    /**
     * Creates a standalone sub-model without parent AAS.
     * 
     * @param idShort the short id of the sub-model
     * @return the sub-model builder (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty, or if this operation is not 
     *   supported
     */
    public abstract SubmodelBuilder createSubmodelBuilder(String idShort);
    
    /**
     * Retrieves an AAS.
     * 
     * @param host the host name of the AAS repository
     * @param port the TCP port number of the AAS repository
     * @param registryPath the registry path on {@code host}
     * @param urn the URN of the AAS
     * @return the AAS (may be <b>null</b> if the AAS does not exist)
     * @throws IOException if accessing the AAS fails for some reason
     */
    public abstract Aas retrieveAas(String host, int port, String registryPath, String urn) throws IOException;
    
    /**
     * Creates a deployment recipe.
     * 
     * @param host the target host
     * @param port the target IP port
     * @return the deployment recipe instance (may be <b>null</b> if no AAS implementation is registered)
     */
    public abstract DeploymentRecipe createDeploymentRecipe(String host, int port);

    /**
     * Creates a deployment recipe.
     * 
     * @param contextPath the context base path (may be empty, otherwise shall start with a "/")
     * @param host the target host
     * @param port the target IP port
     * @return the deployment recipe instance (may be <b>null</b> if no AAS implementation is registered)
     */
    public abstract DeploymentRecipe createDeploymentRecipe(String contextPath, String host, int port);
    
    /**
     * Creates a persistence recipe.
     * 
     * @return the recipe (may be <b>null</b> if no AAS implementation is registered)
     */
    public abstract PersistenceRecipe createPersistenceRecipe();
    
    /**
     * Returns the supported protocols.
     * 
     * @return the protocol names, shall include {@link #DEFAULT_PROTOCOL}
     * @see #createInvocablesCreator(String, String, int)
     */
    public abstract String[] getProtocols();
    
    /**
     * Creates an invocables creator for a certain protocol.
     * 
     * @param protocol the protocol (shall be one from {@link #getProtocols()}, may be {@link #DEFAULT_PROTOCOL} for 
     *   the default protocol}
     * @param host the host name to communicate with
     * @param port the port number to communicate on
     * @return the invocables creator
     * @throws IllegalArgumentException if the protocol is not supported, the host name or the port is not valid
     * @see #createProtocolBuilder(String, int)
     */
    public abstract InvocablesCreator createInvocablesCreator(String protocol, String host, int port);

    /**
     * Creates a protocol server builder for a certain protocol. The server is supposed to run on localhost
     * and to be accessible
     * 
     * @param protocol the protocol (shall be one from {@link #getProtocols()}, may be {@link #DEFAULT_PROTOCOL} for 
     *   the default protocol}
     * @param port the port number to communicate on
     * @return the builder instance
     * @throws IllegalArgumentException if the protocol is not supported or the port is not valid
     * @see #createInvocablesCreator(String, String, int)
     */
    public abstract ProtocolServerBuilder createProtocolServerBuilder(String protocol, int port);
    
}
