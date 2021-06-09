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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

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
        public SubmodelBuilder createSubmodelBuilder(String idShort, String urn) {
            return null;
        }

        @Override
        protected ServerRecipe createDefaultServerRecipe() {
            return null;
        }

        @Override
        public Registry obtainRegistry(Endpoint regEndpoint) throws IOException {
            return new Registry() { // some tests rely on an instance here

                @Override
                public Aas retrieveAas(String aasUrn) throws IOException {
                    return null;
                }

                @Override
                public Submodel retrieveSubmodel(String aasUrn, String submodelUrn) throws IOException {
                    return null;
                }

                @Override
                public void createAas(Aas aas, String endpointURL) {
                }

                @Override
                public void createSubmodel(Aas aas, Submodel submodel) {
                }

                @Override
                public void register(Aas aas, Submodel submodel, String endpointUrl) {
                }
            };
        }

        @Override
        public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint) {
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
    
    /**
     * Functions needed to create an implementation protocol.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ProtocolCreator {

        /**
         * Creates an invocables creator for a certain protocol.
         * 
         * @param host the host name to communicate with
         * @param port the port number to communicate on
         * @return the invocables creator
         * @throws IllegalArgumentException if the protocol is not supported, the host name or the port is not valid
         * @see #createProtocolServerBuilder(String, int)
         */
        public InvocablesCreator createInvocablesCreator(String host, int port);

        /**
         * Creates a protocol server builder for a certain protocol. The server is supposed to run on localhost
         * and to be accessible. Depending on the AAS implementation, access to the protocol service may be 
         * required to deploy an AAS, i.e., it is advisable to start the protocol server before 
         * {@link #createDeploymentRecipe(Endpoint)}.
         * 
         * @param port the port number to communicate on
         * @return the builder instance
         * @throws IllegalArgumentException if the protocol is not supported or the port is not valid
         * @see #createInvocablesCreator(String, String, int)
         */
        public ProtocolServerBuilder createProtocolServerBuilder(int port);
        
    }    
    
    private static final Logger LOGGER = Logger.getLogger(AasFactory.class.getName());
    // instance-based to allow later dependency injection
    private static AasFactory instance = DUMMY;
    
    private Map<String, ProtocolCreator> protocolCreators = new HashMap<>();
    
    /**
     * Returns the actual instance.
     * 
     * @return the actual instance
     */
    public static AasFactory getInstance() {
        if (DUMMY == instance) {
            Optional<AasFactoryDescriptor> first = ServiceLoaderUtils.filterExcluded(AasFactoryDescriptor.class);
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
     * Returns whether {@link #getInstance()} is a full factory instance.
     * 
     * @return {@code true} if {@link #getInstance()} is a full factory instance, {@code false} else
     */
    public static boolean isFullInstance() {
        return isFullInstance(getInstance());
    }
    
    /**
     * Returns whether {@code factory} is a full factory instance.
     * 
     * @param factory the factory instance
     * @return {@code true} if {@code factory} is a full factory instance, {@code false} else
     */
    public static boolean isFullInstance(AasFactory factory) {
        return !(factory == AasFactory.DUMMY || ServiceLoaderUtils.hasExcludeFirst(factory));
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
     * Registers a protocol creator.
     * 
     * @param protocol the protocol name
     * @param creator the creator
     */
    protected void registerProtocolCreator(String protocol, ProtocolCreator creator) {
        protocolCreators.put(protocol, creator);
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
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * 
     * @return the AAS builder instance (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
     */
    public abstract AasBuilder createAasBuilder(String idShort, String identifier);

    /**
     * Creates a standalone sub-model without parent AAS.
     * 
     * @param idShort the short id of the sub-model
     * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * @return the sub-model builder (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty, or if this operation is not 
     *   supported
     */
    public abstract SubmodelBuilder createSubmodelBuilder(String idShort, String identifier);

    /**
     * Creates a server recipe. Utilizes JLS via {@link AasServerRecipeDescriptor} to determine a specific recipe.
     * If none is found, {@link #createDefaultServerRecipe()} is called.
     * 
     * @return the registry server recipe (may be <b>null</b> for none)
     */
    public final ServerRecipe createServerRecipe() {
        ServerRecipe result = null;
        Optional<AasServerRecipeDescriptor> first = ServiceLoaderUtils.filterExcluded(AasServerRecipeDescriptor.class);
        if (first.isPresent()) {
            result  = first.get().createInstance();
        } 
        if (null == result) {
            result = createDefaultServerRecipe();
        }
        return result;
    }
    
    /**
     * Creates the default server recipe.
     * 
     * @return the default server recipe (may be <b>null</b> for none)
     */
    protected abstract ServerRecipe createDefaultServerRecipe();
    
    /**
     * Obtains access to a registry.
     * 
     * @param regEndpoint the registry endpoint
     * @return the registry access for the given connection information
     * @throws IOException in case that the recipe/connection cannot be created
     */
    public abstract Registry obtainRegistry(Endpoint regEndpoint) throws IOException;

    /**
     * Creates a deployment recipe.
     * 
     * @param endpoint the target host (hostname in particular used for endpoint urls)
     * @return the deployment recipe instance (may be <b>null</b> if no AAS implementation is registered)
     */
    public abstract DeploymentRecipe createDeploymentRecipe(Endpoint endpoint);
    
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
     * @see #createProtocolServerBuilder(String, int)
     */
    public abstract InvocablesCreator createInvocablesCreator(String protocol, String host, int port);

    /**
     * Creates a protocol server builder for a certain protocol. The server is supposed to run on localhost
     * and to be accessible. Depending on the AAS implementation, access to the protocol service may be 
     * required to deploy an AAS, i.e., it is advisable to start the protocol server before 
     * {@link #createDeploymentRecipe(Endpoint)}.
     * 
     * @param protocol the protocol (shall be one from {@link #getProtocols()}, may be {@link #DEFAULT_PROTOCOL} for 
     *   the default protocol}
     * @param port the port number to communicate on
     * @return the builder instance
     * @throws IllegalArgumentException if the protocol is not supported or the port is not valid
     * @see #createInvocablesCreator(String, String, int)
     */
    public abstract ProtocolServerBuilder createProtocolServerBuilder(String protocol, int port);
    
    /**
     * Modifies a given {@code id} so that it fits the needs of the implementation.
     * 
     * @param id the id
     * @return the fixed id
     */
    public String fixId(String id) { // generic code for AAS Spec, may be overridden
        String result = id;
        if (id != null && id.length() > 0) {
            if (!Character.isAlphabetic(id.charAt(0))) {
                id = "a" + id;
            }
            result = "";
            for (int i = 0; i < id.length(); i++) {
                char c = id.charAt(i);
                if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                    result += c;
                } else {
                    result += "_";
                }
            }
        }
        return result;
    }
    
}
