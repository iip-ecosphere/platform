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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.SingletonPlugin;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * A customizable factory for creating AAS instances independent of the underlying implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AasFactory {

    /**
     * Default communication/implementation protocol for properties and operations. An implementation must
     * fill this with some protocol.
     */
    public static final String DEFAULT_PROTOCOL = "";
    
    /**
     * A protocol involving only local calls, no network. Host/port shall be ignored. Created 
     * {@link InvocablesCreator} and {@link ProtocolServerBuilder} shall refer to the same {@link OperationsProvider} 
     * instance. {@link #createInvocablesCreator(SetupSpec)} creates then the provider instance that is used
     * for the next {@link #createProtocolServerBuilder(SetupSpec)} calls until the next invocables creator
     * is requested. Clients may also create the instances directly to take control over the 
     * {@link OperationsProvider}. No other protocol shall have this name. The default protocol shall not be 
     * a local protocol.
     */
    public static final String LOCAL_PROTOCOL = "local";
    
    /**
     * The plugin ID of the default AAS implementation.
     */
    public static final String DEFAULT_PLUGIN_ID = "aas" + PluginManager.POSTFIX_ID_DEFAULT;
    
    public static final String PROPERTY_PLUGIN_ID = "okto.aasFactoryId";

    /**
     * Factory descriptor for Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract static class AbstractDescriptor implements AasFactoryDescriptor, PluginDescriptor<AasFactory> {
        
        @Override
        public Class<AasFactory> getType() {
            return AasFactory.class;
        }
        
        @Override
        public Plugin<AasFactory> createPlugin(File installDir) {
            return new SingletonPlugin<AasFactory>(getId(), getFurtherIds(), AasFactory.class, p -> createInstance(), 
                installDir);
        }
        
    }
    
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
        public Registry obtainRegistry(SetupSpec spec) throws IOException {
            return new Registry() { // some tests rely on an instance here

                @Override
                public Aas retrieveAas(String aasUrn) throws IOException {
                    return null;
                }

                @Override
                public Aas retrieveAas(String identifier, boolean populate) throws IOException {
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

                @Override
                public String getEndpoint(Aas aas) {
                    return null;
                }

                @Override
                public String getEndpoint(Aas aas, Submodel submodel) {
                    return null;
                }

                @Override
                public String getEndpoint(String aasIdShort) {
                    return null;
                }

                @Override
                public List<String> getAasIdShorts() {
                    return new ArrayList<String>();
                }

                @Override
                public List<String> getAasIdentifiers() {
                    return new ArrayList<String>();
                }

            };
        }
        
        @Override
        public Registry obtainRegistry(SetupSpec spec, Schema aasSchema) throws IOException {
            return obtainRegistry(spec);
        }

        @Override
        public DeploymentRecipe createDeploymentRecipe(SetupSpec spec) {
            return null;
        }

        @Override
        public PersistenceRecipe createPersistenceRecipe() {
            return null;
        }
        
        @Override
        protected boolean accept(ProtocolDescriptor creator) {
            return true; // allow the fake test protocol creator for testing
        }

        @Override
        public String getMetaModelVersion() {
            return "v0";
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
         * @param spec the setup specification
         * @return the invocables creator
         * @throws IllegalArgumentException if the protocol is not supported, the host name or the port is not valid
         * @see #createProtocolServerBuilder(SetupSpec)
         */
        public InvocablesCreator createInvocablesCreator(SetupSpec spec);

        /**
         * Creates a protocol server builder for a certain protocol. The server is supposed to run on localhost
         * and to be accessible. Depending on the AAS implementation, access to the protocol service may be 
         * required to deploy an AAS.
         * 
         * @param spec the setup specification
         * @return the builder instance
         * @throws IllegalArgumentException if the protocol is not supported or the port is not valid
         * @see #createInvocablesCreator(SetupSpec)
         */
        public ProtocolServerBuilder createProtocolServerBuilder(SetupSpec spec);
        
    }    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AasFactory.class);
    // instance-based to allow later dependency injection
    private static AasFactory instance = DUMMY;
    private static String pluginId = OsUtils.getPropertyOrEnv("okto.aasFactoryId", DEFAULT_PLUGIN_ID);
    private static boolean noInstanceWarningEmitted = false;
    
    private Map<String, ProtocolCreator> protocolCreators = new HashMap<>();
    private String[] protocols;
    private Map<AasComponent, Function<ComponentSetup, Boolean>> available = new HashMap<>();

    /**
     * Creates the factory instance.
     * 
     * @see ProtocolDescriptor
     */
    protected AasFactory() {
        // load specified first so that refined classes can overwrite protocols on demand later in their constructor
        ServiceLoader<ProtocolDescriptor> loader = ServiceLoader.load(ProtocolDescriptor.class);
        Iterator<ProtocolDescriptor> iter = loader.iterator();
        while (iter.hasNext()) {
            ProtocolDescriptor desc = iter.next();
            if (accept(desc)) {
                ProtocolCreator creator = desc.createInstance();
                registerProtocolCreator(desc.getName(), creator);
                if (1 == protocolCreators.size()) {
                    registerProtocolCreator(DEFAULT_PROTOCOL, creator);
                }
            }
        }
        registerProtocolCreator(LOCAL_PROTOCOL, new SimpleLocalProtocolCreator());
    }
    
    /**
     * Returns whether a protocol is considered acceptable for this factory. By default, we exclude all 
     * {@link ExcludeFirst} annotated creators.
     * 
     * @param creator the creator to check
     * @return {@code true} for acceptable, {@code false} else
     */
    protected boolean accept(ProtocolDescriptor creator) {
        return !creator.getClass().isAnnotationPresent(ExcludeFirst.class);
    }
    
    /**
     * Returns the supported protocols.
     * 
     * @return the protocol names, shall include {@link #DEFAULT_PROTOCOL}
     * @see #createInvocablesCreator(SetupSpec)
     */
    public String[] getProtocols() {
        if (null == protocols) {
            protocols = new String[protocolCreators.size()];
            protocolCreators.keySet().toArray(protocols);
        }
        return protocols;
    }
    
    /**
     * Sets the id of the plugin to load as AAS implementation. By default, {@link #DEFAULT_PLUGIN_ID}.
     * 
     * @param id the id, ignored if <b>null</b> or empty
     */
    public static void setPluginId(String id) {
        if (id != null && id.length() > 0 && !id.equals(pluginId)) {
            pluginId = id;
            instance = DUMMY; // reset for getInstance()
        }
    }
    
    /**
     * Returns the plugin id used to load the AAS implementation. By default, {@link #DEFAULT_PLUGIN_ID}.
     * 
     * @return the id
     */
    public static String getPluginId() {
        return pluginId;
    }
    
    /**
     * Returns the actual instance.
     * 
     * @return the actual instance
     */
    public static AasFactory getInstance() {
        if (DUMMY == instance) {
            Plugin<AasFactory> plugin = PluginManager.getPlugin(pluginId, AasFactory.class);
            if (null != plugin) {
                instance = plugin.getInstance();
                emitFactoryInstanceNotice();
            } 
            if (DUMMY == instance || null == instance) {
                Optional<AasFactoryDescriptor> first = ServiceLoaderUtils.filterExcluded(AasFactoryDescriptor.class);
                if (first.isPresent()) {
                    instance = first.get().createInstance();
                    emitFactoryInstanceNotice();
                } else {
                    if (!noInstanceWarningEmitted) {
                        noInstanceWarningEmitted = true;
                        LOGGER.warn("No AAS factory implementation known. This may be intended in a simple testing "
                            + "setup where AAS operations are optional, but also a severe misconfiguration if this "
                            + "occurs in the context of a full platform instance where AAS operations are mandatory.");
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Logs a notice which factory is being used.
     */
    private static void emitFactoryInstanceNotice() {
        if (null != instance) {
            LOGGER.info("Using AAS factory implementation: {}", instance.getClass().getName());
        }
    }
    
    /**
     * Returns whether a no instance warning was already emitted, i.e., the factory is not configured for production 
     * use. The (some) subsequent errors/warning may be omitted.
     * 
     * @return {@code true} for "no instance warning" emitted, {@code false} if no such warning was emitted 
     */
    public static boolean isNoInstanceWarningEmitted() {
        return noInstanceWarningEmitted;
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
     * Creates an AAS builder instance for a <b>new AAS</b> (not for adding further sub-models).
     * 
     * @param idShort the shortId of the AAS
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} for others)
     * 
     * @return the AAS builder instance (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
     */
    public abstract AasBuilder createAasBuilder(String idShort, String identifier);

    /**
     * Creates a standalone sub-model without parent AAS for a <b>new sub-model</b> (not for adding elements to an 
     * existing sub-model).
     * 
     * @param idShort the short id of the sub-model
     * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} for others)
     * @return the sub-model builder (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty, or if this operation is not 
     *   supported
     */
    public abstract SubmodelBuilder createSubmodelBuilder(String idShort, String identifier);

    /**
     * Creates a standalone sub-model without parent AAS for a <b>new sub-model</b> (not for adding elements to an 
     * existing sub-model). Calls {@link #createSubmodelBuilder(String, String)} by default.
     * 
     * @param idShort the short id of the sub-model
     * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} for others)
     * @param spec setup specification needed for encrypted/authentication notification of property changes/execution of
     *    operation in purely local instances, not needed if registered to/retrieved from repository
     * @return the sub-model builder (may be <b>null</b> if no AAS implementation is registered)
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty, or if this operation is not 
     *   supported
     */
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier, SetupSpec spec) {
        return createSubmodelBuilder(idShort, identifier);
    }

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
     * Obtains access to a registry for unencrypted AAS via HTTP.
     * 
     * @param spec the setup endpoints for AAS
     * @return the registry access for the given connection information
     * @throws IOException in case that the recipe/connection cannot be created
     */
    public abstract Registry obtainRegistry(SetupSpec spec) throws IOException;

    /**
     * Obtains access to a registry.
     * 
     * @param spec the setup endpoints for AAS
     * @param aasSchema the schema to access the AAS server with, must be consistent with encryption settings
     * @return the registry access for the given connection information
     * @throws IOException in case that the recipe/connection cannot be created
     */
    public abstract Registry obtainRegistry(SetupSpec spec, Schema aasSchema) throws IOException;
    
    /**
     * Creates a deployment recipe for unencrypted deployment.
     * 
     * @param spec the setup specification
     * @return the deployment recipe instance (may be <b>null</b> if no AAS implementation is registered)
     */
    public abstract DeploymentRecipe createDeploymentRecipe(SetupSpec spec);
    
    /**
     * Creates a persistence recipe.
     * 
     * @return the recipe (may be <b>null</b> if no AAS implementation is registered)
     */
    public abstract PersistenceRecipe createPersistenceRecipe();
    
    /**
     * Creates an invocables creator for a certain protocol. <b>null</b> is turned into {@link #DEFAULT_PROTOCOL}
     * 
     * @param spec the setup specification with protocol from {@link #getProtocols()}, may be {@link #DEFAULT_PROTOCOL}
     * @return the invocables creator (may be <b>null</b> if the protocol does not exist)
     * @throws IllegalArgumentException if the protocol is not supported, the host name or the port is not valid
     * @see #createProtocolServerBuilder(SetupSpec)
     */
    public InvocablesCreator createInvocablesCreator(SetupSpec spec) {
        String protocol = spec.getAssetServerProtocol();
        if (null == protocol) {
            protocol = DEFAULT_PROTOCOL;
        }
        ProtocolCreator creator = protocolCreators.get(protocol);
        if (null == creator) {
            throw new IllegalArgumentException("Unknown/unregistered protocol: " + protocol);
        }
        return creator.createInvocablesCreator(spec);
    }
        
    /**
     * Creates a protocol server builder for a certain protocol. The server is supposed to run on localhost
     * and to be accessible. Depending on the AAS implementation, access to the protocol service may be 
     * required to deploy an AAS, i.e., it is advisable to start the protocol server before 
     * {@link #createDeploymentRecipe(SetupSpec)}.
     * 
     * @param spec the setup specification with protocol (shall be one from {@link #getProtocols()}, may be 
     *     {@link #DEFAULT_PROTOCOL} for the default protocol}
     * @return the builder instance (may be <b>null</b> if the protocol does not exist)
     * @throws IllegalArgumentException if the protocol is not supported or the port is not valid
     * @see #createInvocablesCreator(SetupSpec)
     */
    public ProtocolServerBuilder createProtocolServerBuilder(SetupSpec spec) {
        ProtocolCreator creator = protocolCreators.get(spec.getAssetServerProtocol());
        if (null == creator) {
            throw new IllegalArgumentException("Unknown/unregistered protocol: " + spec.getAssetServerProtocol());
        }
        return creator.createProtocolServerBuilder(spec);
    }

    /**
     * Returns whether this specific factory/implementation requires a short id fix in addition
     * to the fixes required by the AAS specification.
     * 
     * @param id the id short to test
     * @return {@code true} if it needs a fix, {@code false} else
     */
    protected boolean needsIdFix(String id) {
        return false;
    }
    
    /**
     * Modifies a given {@code id} so that it fits the needs of the implementation.
     * 
     * @param id the id
     * @return the fixed id
     * @see #needsIdFix(String)
     */
    public String fixId(String id) { // generic code for AAS Spec, may be overridden/extended
        String result = id;
        if (id != null && id.length() > 0) {
            if (!Character.isAlphabetic(id.charAt(0)) || needsIdFix(id)) {
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
    
    /**
     * Returns the implemented metamodel version.
     * 
     * @return the metamodel version (v2, v3)
     */
    public abstract String getMetaModelVersion();

    /**
     * Returns whether the implementation allows for server instances running on the same port.
     * 
     * @return {@code true} if supported, {@code false}
     */
    public boolean supportsSamePorts() {
        return false;
    }
    
    /**
     * Returns whether the implementation allows for dedicated server URL paths.
     * 
     * @return {@code true} of server URL paths are considered and taken up, {@code false} if paths are ignored 
     */
    public boolean supportsUrlPaths() {
        return false;
    }

    /**
     * Returns whether the implementation allows for user-defined functions assigned to
     * AAS properties.
     * 
     * @return {@code true} if supported, {@code false}
     */
    public boolean supportsPropertyFunctions() {
        return false;
    }

    /**
     * Returns whether the implementation supports authentication.
     * 
     * @return {@code true} if supported, {@code false}
     */
    public boolean supportsAuthentication() {
        return true;
    }
    
    /**
     * Returns whether the implementation supports authorization of operation execution level.
     * 
     * @return {@code true} if supported, {@code false}
     */
    public boolean supportsOperationExecutionAuthorization() {
        return true;
    }
    
    /**
     * Returns whether creating structures more statically/early is better supported by the implementation than
     * later/dynamic. Typically, later creation requires a faster/more agile implementation.
     * 
     * @return {@code true} if late creation is suggested, {@code false} if early creation
     */
    public boolean createPropertiesEarly() {
        return false;
    }
    
    /**
     * Composes an idShort from multiple names.
     * 
     * @param names the names to compose
     * @return the composed name
     */
    public static String composeIdShort(String... names) {
        String result = "";
        for (String s : names) {
            /*String tmp = s;
            if (result.length() > 0) {
                if (tmp.length() > 0) {
                    tmp = Character.toUpperCase(tmp.charAt(0)) + tmp.substring(1);
                } // further cleanup?
            }
            result += "_" + tmp;*/
            if (s.length() > 0) {
                if (result.length() > 0) {
                    result += "_";
                }
                result += s;
            }
        }
        return result;
    }

    /**
     * Returns whether a specific component from {@code setup} is available.
     * 
     * @param spec the specification
     * @param component the component
     * @return {@code true} for available, {@code false} else
     */
    public boolean isAvailable(SetupSpec spec, AasComponent component) {
        boolean result;
        Function<ComponentSetup, Boolean> func = available.get(component);
        if (null == func) {
            result = true;
        } else {
            result = func.apply(spec.getSetup(component));
        }
        return result;
    }

    /**
     * Register a function to determine the (network) availability of the specified components.
     * 
     * @param func the function, may be <b>null</b> for replacing the actual function by the default 
     *     (constant {@code true} function)
     * @param components the component(s) to register the function for
     */
    protected void registerAvailabilityFunction(Function<ComponentSetup, Boolean> func, AasComponent... components) {
        for (AasComponent c : components) {
            available.put(c, func);
        }
    }

}
