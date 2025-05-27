/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * SPDX-License-Identifier: MIT
 ******************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx.basyx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.basyx.aas.aggregator.AASAggregatorAPIHelper;
import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.aas.aggregator.restapi.AASAggregatorProvider;
import org.eclipse.basyx.aas.bundle.AASBundle;
import org.eclipse.basyx.aas.bundle.AASBundleHelper;
import org.eclipse.basyx.aas.factory.aasx.AASXToMetamodelConverter;
import org.eclipse.basyx.aas.factory.aasx.FileLoaderHelper;
import org.eclipse.basyx.aas.factory.aasx.SubmodelFileEndpointLoader;
import org.eclipse.basyx.aas.factory.json.JSONAASBundleFactory;
import org.eclipse.basyx.aas.factory.xml.XMLAASBundleFactory;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.components.IComponent;
import org.eclipse.basyx.components.aas.aascomponent.IAASServerDecorator;
import org.eclipse.basyx.components.aas.aascomponent.IAASServerFeature;
import org.eclipse.basyx.components.aas.aascomponent.InMemoryAASServerComponentFactory;
import org.eclipse.basyx.components.aas.aascomponent.MongoDBAASServerComponentFactory;
import org.eclipse.basyx.components.aas.aasx.AASXPackageManager;
import org.eclipse.basyx.components.aas.authorization.AuthorizedAASServerFeature;
import org.eclipse.basyx.components.aas.authorization.internal.AuthorizedAASServerFeatureFactory;
import org.eclipse.basyx.components.aas.authorization.internal.AuthorizedDefaultServlet;
import org.eclipse.basyx.components.aas.authorization.internal.AuthorizedDefaultServletParams;
import org.eclipse.basyx.components.aas.configuration.AASEventBackend;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.aas.delegation.DelegationAASServerFeature;
import org.eclipse.basyx.components.aas.mqtt.MqttAASServerFeature;
import org.eclipse.basyx.components.aas.mqtt.MqttV2AASServerFeature;
import org.eclipse.basyx.components.aas.servlet.AASAggregatorAASXUploadServlet;
import org.eclipse.basyx.components.aas.servlet.AASAggregatorServlet;
import org.eclipse.basyx.components.configuration.BaSyxConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxMongoDBConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxMqttConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxSecurityConfiguration;
import org.eclipse.basyx.extensions.aas.aggregator.aasxupload.AASAggregatorAASXUpload;
import org.eclipse.basyx.extensions.aas.registration.authorization.AuthorizedAASRegistryProxy;
import org.eclipse.basyx.extensions.shared.authorization.internal.ElevatedCodeAuthentication;
import org.eclipse.basyx.extensions.shared.encoding.Base64URLEncoder;
import org.eclipse.basyx.extensions.shared.encoding.URLEncoder;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.basyx.vab.modelprovider.VABPathTools;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.basyx.security.Helper;

/**
 * Component providing an empty AAS server that is able to receive AAS/SMs from
 * remote. It uses the Aggregator API, i.e. AAS should be pushed to
 * ${URL}/shells
 * 
 * Taken over from BaSyx for customization.
 *
 * @author schnicke, espen, fried, fischer, danish, wege
 */
@SuppressWarnings("deprecation")
public class AASServerComponent implements IComponent {

    private static final String PREFIX_SUBMODEL_PATH = "/aas/submodels/";
    private static Logger logger = LoggerFactory.getLogger(AASServerComponent.class);

    // Initial AASBundle
    protected Collection<AASBundle> aasBundles;

    // The server with the servlet that will be created
    private BaSyxHTTPServer server;
    private IAASRegistry registry;

    // Configurations
    private BaSyxContextConfiguration contextConfig;
    private BaSyxAASServerConfiguration aasConfig;
    private BaSyxMongoDBConfiguration mongoDBConfig;
    private BaSyxSecurityConfiguration securityConfig;

    private List<IAASServerFeature> aasServerFeatureList = new ArrayList<IAASServerFeature>();

    private IAASAggregator aggregator;
    // Watcher for AAS Aggregator functionality
    private boolean isAASXUploadEnabled = false;

    private SetupSpec spec;
    private AasComponent component;

    /**
     * Constructs an empty AAS server using the passed context.
     * 
     * @param contextConfig the AAS context configuration
     * @param spec the setup specification
     * @param component the component being configured
     */
    public AASServerComponent(BaSyxContextConfiguration contextConfig, SetupSpec spec, AasComponent component) {
        this.contextConfig = contextConfig;
        this.aasConfig = new BaSyxAASServerConfiguration();
        this.spec = spec;
        this.component = component;
    }

    /**
     * Constructs an empty AAS server using the passed configuration.
     * 
     * @param contextConfig the AAS context configuration
     * @param spec the setup specification
     * @param component the component being configured
     * @param aasConfig the aAS server configuration
     */
    public AASServerComponent(BaSyxContextConfiguration contextConfig, SetupSpec spec, AasComponent component, 
        BaSyxAASServerConfiguration aasConfig) {
        this.contextConfig = contextConfig;
        this.aasConfig = aasConfig;
        this.spec = spec;
        this.component = component;
    }

    /**
     * Constructs an empty AAS server using the passed configuration.
     * 
     * @param contextConfig the AAS context configuration
     * @param spec the setup specification
     * @param component the component being configured
     * @param aasConfig the aAS server configuration
     * @param mongoDBConfig the mongo DB configuration
     */
    public AASServerComponent(BaSyxContextConfiguration contextConfig, SetupSpec spec, AasComponent component, 
        BaSyxAASServerConfiguration aasConfig, BaSyxMongoDBConfiguration mongoDBConfig) {
        this.contextConfig = contextConfig;
        this.aasConfig = aasConfig;
        this.aasConfig.setAASBackend(AASServerBackend.MONGODB);
        this.mongoDBConfig = mongoDBConfig;
        this.spec = spec;
        this.component = component;
    }

    /**
     * Sets and enables mqtt connection configuration for this component. Has to be
     * called before the component is started. Currently only works for InMemory
     * backend.
     *
     * @param configuration
     * 
     * @deprecated Add MQTT via {@link MqttAASServerFeature} instead.
     */
    @Deprecated
    public void enableMQTT(BaSyxMqttConfiguration configuration) {
        aasServerFeatureList.add(new MqttAASServerFeature(configuration, getMqttSubmodelClientId()));
    }

    /**
     * Disables mqtt configuration. Has to be called before the component is
     * started.
     * 
     * @deprecated remove MQTT from the feature list instead.
     */
    @Deprecated
    public void disableMQTT() {
        aasServerFeatureList.forEach(f -> {
            if (f instanceof MqttAASServerFeature) {
                aasServerFeatureList.remove(f);
            }
        });
    }

    /**
     * Enables AASX upload functionality.
     */
    public void enableAASXUpload() {
        this.isAASXUploadEnabled = true;
    }

    /**
     * Sets a registry service for registering AAS that are created during startup.
     *
     * @param registry the registry
     */
    public void setRegistry(IAASRegistry registry) {
        this.registry = registry;
    }

    /**
     * Explicitly sets AAS bundles that should be loaded during startup.
     * 
     * @param aasBundles The bundles that will be loaded during startup
     */
    public void setAASBundles(Collection<AASBundle> aasBundles) {
        this.aasBundles = aasBundles;
    }

    /**
     * Explicitly sets an AAS bundle that should be loaded during startup.
     * 
     * @param aasBundle The bundle that will be loaded during startup
     */
    public void setAASBundle(AASBundle aasBundle) {
        this.aasBundles = Collections.singleton(aasBundle);
    }

    /**
     * Sets the security configuration for this component. Has to be called before
     * the component is started.
     *
     * @param configuration the configuration to be set
     */
    public void setSecurityConfiguration(final BaSyxSecurityConfiguration configuration) {
        securityConfig = configuration;
    }

    /**
     * Starts the AASX component at http://${hostName}:${port}/${path}.
     */
    @Override
    public void startComponent() {
        logger.info("Create the server...");
        registry = createRegistryFromConfig(aasConfig);

        loadAASServerFeaturesFromConfig();
        initializeAASServerFeatures();

        BaSyxContext context = contextConfig.createBaSyxContext();
        context.addServletMapping("/*", createAggregatorServlet());
        addAASServerFeaturesToContext(context);

        // An initial AAS has been loaded from the drive?
        if (aasBundles != null) {
            // 1. Also provide the files
            context.addServletMapping("/files/*", createDefaultServlet());

            // 2. Fix the file paths according to the servlet configuration
            modifyFilePaths(contextConfig.getHostname(), contextConfig.getPort(), contextConfig.getContextPath());

            // 3. Register the initial AAS
            registerEnvironment();
        }

        logger.info("Start the server");
        server = new BaSyxHTTPServer(context, spec, component);
        server.start();

        registerPreexistingAASAndSMIfPossible();
    }

    /**
     * Creates the default servlet.
     * 
     * @return the default servlet
     */
    private DefaultServlet createDefaultServlet() {
        if (aasConfig.isAuthorizationEnabled()) {
            final AuthorizedDefaultServletParams<?> params = getAuthorizedDefaultServletParams();
            if (params != null) {
                return new AuthorizedDefaultServlet<>(params);
            }
        }
        return new DefaultServlet();
    }

    /**
     * Gets authorized servlet params.
     * 
     * @return the params
     */
    private AuthorizedDefaultServletParams<?> getAuthorizedDefaultServletParams() {
        final AuthorizedAASServerFeature<?> authorizedAASServerFeature = new AuthorizedAASServerFeatureFactory(
                securityConfig).create();

        return authorizedAASServerFeature.getFilesAuthorizerParams();
    }

    /**
     * Registers pre-existing AAS and SM if possible.
     */
    private void registerPreexistingAASAndSMIfPossible() {
        if (!shouldRegisterPreexistingAASAndSM()) {
            return;
        }

        aggregator.getAASList().stream().forEach(this::registerAASAndSubmodels);
    }

    /**
     * Returns whether it should register pre-existing AAS and SM if possible.
     */
    private boolean shouldRegisterPreexistingAASAndSM() {
        return isMongoDBBackend() && registry != null;
    }

    /**
     * Registers AAS and submodels.
     * 
     * @param aas the AAS to register
     */
    private void registerAASAndSubmodels(IAssetAdministrationShell aas) {
        registerAAS(aas);

        registerSubmodels(aas);
    }
    
    // checkstyle: stop exception type check

    /**
     * Registers AAS.
     * 
     * @param aas the AAS to register
     */
    private void registerAAS(IAssetAdministrationShell aas) {
        try {
            String combinedEndpoint = getAASAccessPath(aas);
            registry.register(new AASDescriptor(aas, combinedEndpoint));
            logger.info("The AAS " + aas.getIdShort() + " is Successfully Registered from DB");
        } catch (Exception e) {
            logger.info("The AAS " + aas.getIdShort() + " could not be Registered from DB" + e);
        }
    }

    // checkstyle: resume exception type check

    /**
     * Returns the AAS access path.
     * 
     * @param aas the AAS
     * @return the access path
     */
    private String getAASAccessPath(IAssetAdministrationShell aas) {
        return VABPathTools.concatenatePaths(getURL(),
                AASAggregatorAPIHelper.getAASAccessPath(aas.getIdentification()));
    }
    
    // checkstyle: stop exception type check

    /**
     * Registers the submodels of {@code aas}.
     * 
     * @param aas the AAS
     */
    private void registerSubmodels(IAssetAdministrationShell aas) {
        List<ISubmodel> submodels = getSubmodelFromAggregator(aggregator, aas.getIdentification());
        try {
            submodels.stream().forEach(submodel -> registerSubmodel(aas, submodel));
            logger.info("The submodels from AAS " + aas.getIdShort() + " are Successfully Registered from DB");
        } catch (Exception e) {
            logger.info("The submodel from AAS " + aas.getIdShort() + " could not be Registered from DB " + e);
        }
    }

    // checkstyle: resume exception type check

    /**
     * Registers the given submodel.
     * 
     * @param aas the AAS
     * @param submodel the submodel
     */
    private void registerSubmodel(IAssetAdministrationShell aas, ISubmodel submodel) {
        String smEndpoint = VABPathTools.concatenatePaths(getAASAccessPath(aas), AssetAdministrationShell.SUBMODELS,
                submodel.getIdShort(), SubmodelProvider.SUBMODEL);
        registry.register(aas.getIdentification(), new SubmodelDescriptor(submodel, smEndpoint));
    }
    
    /**
     * Gets submodels from aggregator.
     * 
     * @param aggregator the aggregator
     * @param iIdentifier the AAS identifier
     * @return the submodels
     */
    private List<ISubmodel> getSubmodelFromAggregator(IAASAggregator aggregator, IIdentifier iIdentifier) {
        MultiSubmodelProvider aasProvider = (MultiSubmodelProvider) aggregator.getAASProvider(iIdentifier);

        @SuppressWarnings("unchecked")
        List<Object> submodelObject = (List<Object>) aasProvider.getValue(PREFIX_SUBMODEL_PATH);

        List<ISubmodel> persistentSubmodelList = new ArrayList<>();

        submodelObject.stream().map(this::getSubmodel).forEach(persistentSubmodelList::add);

        return persistentSubmodelList;
    }

    /**
     * Casts the submodel in {@code submodelObject}.
     * 
     * @param submodelObject the submodel object
     * @return the submodel
     */
    @SuppressWarnings("unchecked")
    private ISubmodel getSubmodel(Object submodelObject) {
        return Submodel.createAsFacade((Map<String, Object>) submodelObject);
    }

    /**
     * Loads AAS server features from config.
     */
    private void loadAASServerFeaturesFromConfig() {
        if (aasConfig.isPropertyDelegationEnabled()) {
            addAASServerFeature(new DelegationAASServerFeature());
        }

        if (isEventingEnabled()) {
            configureMqttFeature();
        }

        configureSecurity();

        if (aasConfig.isAASXUploadEnabled()) {
            enableAASXUpload();
        }
    }

    /**
     * Configures security.
     */
    private void configureSecurity() {
        if (!aasConfig.isAuthorizationEnabled()) {
            return;
        }

        if (securityConfig == null) {
            securityConfig = new BaSyxSecurityConfiguration();
            securityConfig.loadFromDefaultSource();
        }

        addAASServerFeature(new AuthorizedAASServerFeatureFactory(securityConfig).create());
    }

    /**
     * Returns whether AAS eventing is enabled.
     * 
     * @return {@code true} for eventing, {@code false} else
     */
    private boolean isEventingEnabled() {
        return !aasConfig.getAASEvents().equals(AASEventBackend.NONE);
    }

    /**
     * Configures the MQTT feature.
     */
    private void configureMqttFeature() {
        BaSyxMqttConfiguration mqttConfig = new BaSyxMqttConfiguration();
        mqttConfig.loadFromDefaultSource();
        if (aasConfig.getAASEvents().equals(AASEventBackend.MQTT)) {
            addAASServerFeature(new MqttAASServerFeature(mqttConfig, mqttConfig.getClientId()));
        } else if (aasConfig.getAASEvents().equals(AASEventBackend.MQTTV2)) {
            addAASServerFeature(new MqttV2AASServerFeature(mqttConfig, mqttConfig.getClientId(), aasConfig.getAASId(),
                new Base64URLEncoder()));
        } else if (aasConfig.getAASEvents().equals(AASEventBackend.MQTTV2_SIMPLE_ENCODING)) {
            addAASServerFeature(new MqttV2AASServerFeature(mqttConfig, mqttConfig.getClientId(), aasConfig.getAASId(),
                new URLEncoder()));
        }
    }

    /**
     * Retrieves the URL on which the component is providing its HTTP server.
     *
     * @return
     */
    public String getURL() {
        String basePath = aasConfig.getHostpath();
        if (basePath.isEmpty()) {
            return contextConfig.getUrl();
        }
        return basePath;
    }

    @Override
    public void stopComponent() {
        deregisterAASAndSmAddedDuringRuntime();

        cleanUpAASServerFeatures();

        server.shutdown();
    }

    // checkstyle: stop exception type check

    /**
     * Deregisters AAS and SSM during runtime.
     */
    private void deregisterAASAndSmAddedDuringRuntime() {
        if (registry == null) {
            return;
        }

        try {
            aggregator.getAASList().stream().forEach(this::deregisterAASAndAccompanyingSM);
        } catch (RuntimeException e) {
            logger.info("The resource could not be found in the aggregator " + e);
        }

    }

    // checkstyle: resume exception type check

    /**
     * Deregisters AAS and SM.
     * 
     * @param aas the AAS
     */
    private void deregisterAASAndAccompanyingSM(IAssetAdministrationShell aas) {
        getSubmodelDescriptors(aas.getIdentification()).stream()
            .forEach(submodelDescriptor -> deregisterSubmodel(aas.getIdentification(), submodelDescriptor));

        deregisterAAS(aas.getIdentification());
    }

    /**
     * Returns the submodel descriptors for the given AAS.
     * 
     * @param aasIdentifier the AAS identifier
     * @return the submodel descriptors
     */
    private List<SubmodelDescriptor> getSubmodelDescriptors(IIdentifier aasIdentifier) {
        try {
            return registry.lookupSubmodels(aasIdentifier);
        } catch (ResourceNotFoundException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Deregisters a submodel.
     * 
     * @param aasIdentifier the identifier to deregister
     * @param submodelDescriptor the submodel to deregister
     */
    private void deregisterSubmodel(IIdentifier aasIdentifier, SubmodelDescriptor submodelDescriptor) {
        try {
            registry.delete(aasIdentifier, submodelDescriptor.getIdentifier());
            logger.info("The SM '" + submodelDescriptor.getIdShort() + "' successfully deregistered.");
        } catch (ProviderException e) {
            logger.info("The SM '" + submodelDescriptor.getIdShort()
                    + "' can't be deregistered. It was not found in registry.");
        }
    }

    /**
     * Deregisters an AAS.
     * 
     * @param aasIdentifier the identifier to deregister
     */
    private void deregisterAAS(IIdentifier aasIdentifier) {
        try {
            registry.delete(aasIdentifier);
            logger.info("The AAS '" + aasIdentifier.getId() + "' successfully deregistered.");
        } catch (ProviderException e) {
            logger.info("The AAS '" + aasIdentifier.getId() + "' can't be deregistered. It was not found in registry.");
        }
    }

    /**
     * Adds an AAS server feature.
     * 
     * @param aasServerFeature the feature
     */
    public void addAASServerFeature(IAASServerFeature aasServerFeature) {
        aasServerFeatureList.add(aasServerFeature);
    }

    /**
     * Initializes up AAS server features.
     */
    private void initializeAASServerFeatures() {
        for (IAASServerFeature aasServerFeature : aasServerFeatureList) {
            aasServerFeature.initialize();
        }
    }

    /**
     * Cleans up AAS server features.
     */
    private void cleanUpAASServerFeatures() {
        for (IAASServerFeature aasServerFeature : aasServerFeatureList) {
            aasServerFeature.cleanUp();
        }
    }

    /**
     * Loads a bundle as string.
     * 
     * @param filePath the file path
     * @return the bundle as string
     * @throws IOException if I/O fails
     */
    private String loadBundleString(String filePath) throws IOException {
        String content;
        try {
            content = IOUtils.toString(FileLoaderHelper.getInputStream(filePath), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            logger.info("Could not find a corresponding file. Loading from default resource.");
            content = BaSyxConfiguration.getResourceString(filePath);
        }

        return content;
    }

    /**
     * Loads a bundle from XML.
     * 
     * @param xmlPath the XML path
     * @return the bundle
     * @throws IOException if I/O fails
     * @throws ParserConfigurationException if parser is not configured correcty
     * @throws SAXException if XML reading fails
     */
    private Set<AASBundle> loadBundleFromXML(String xmlPath)
            throws IOException, ParserConfigurationException, SAXException {
        logger.info("Loading aas from xml \"" + xmlPath + "\"");
        String xmlContent = loadBundleString(xmlPath);

        return new XMLAASBundleFactory(xmlContent).create();
    }

    /**
     * Loads a bundle from JSON.
     * 
     * @param jsonPath the JSON path
     * @return the bundle
     * @throws IOException if I/O fails
     */
    private Set<AASBundle> loadBundleFromJSON(String jsonPath) throws IOException {
        logger.info("Loading aas from json \"" + jsonPath + "\"");
        String jsonContent = loadBundleString(jsonPath);

        return new JSONAASBundleFactory(jsonContent).create();
    }

    /**
     * Loads a bundle from AASX.
     * 
     * @param aasxPath the AASX path
     * @return the bundle
     * @throws IOException if I/O fails
     * @throws ParserConfigurationException if parser is not configured correcty
     * @throws SAXException if XML reading fails
     * @throws InvalidFormatException if a format is invalid
     * @throws URISyntaxException if a URI is invalid
     */
    private static Set<AASBundle> loadBundleFromAASX(String aasxPath)
            throws IOException, ParserConfigurationException, SAXException, InvalidFormatException, URISyntaxException {
        logger.info("Loading aas from aasx \"" + aasxPath + "\"");

        // Instantiate the aasx package manager
        AASXToMetamodelConverter packageManager = new AASXPackageManager(aasxPath);

        // Unpack the files referenced by the aas
        packageManager.unzipRelatedFiles();

        // Retrieve the aas from the package
        return packageManager.retrieveAASBundles();
    }

    /**
     * Adds AAS server features to {@code context}.
     * 
     * @param context the context to modify
     */
    private void addAASServerFeaturesToContext(BaSyxContext context) {
        for (IAASServerFeature aasServerFeature : aasServerFeatureList) {
            aasServerFeature.addToContext(context);
        }
    }

    /**
     * Creates the aggregator servlet.
     * 
     * @return the aggregator servlet
     */
    private VABHTTPInterface<?> createAggregatorServlet() {
        aggregator = createAASAggregator();
        loadAASBundles();

        if (aasBundles != null) {
            try (final var ignored = ElevatedCodeAuthentication.enterElevatedCodeAuthenticationArea()) {
                AASBundleHelper.integrate(aggregator, aasBundles);
            }
        }

        if (isAASXUploadEnabled) {
            return new AASAggregatorAASXUploadServlet(new AASAggregatorAASXUpload(aggregator));
        } else {
            return new AASAggregatorServlet(aggregator);
        }
    }

    /**
     * Creates the AAS aggregator.
     * 
     * @return the aggregator instance
     */
    private IAASAggregator createAASAggregator() {
        IAASAggregator result;
        if (isMongoDBBackend()) {
            result = new MongoDBAASServerComponentFactory(createMongoDbConfiguration(), createAASServerDecoratorList(),
                    registry).create();
        }
        result = new InMemoryAASServerComponentFactory(createAASServerDecoratorList(), registry).create();
        return Helper.addAuthorization(result, spec, component);
    }

    /**
     * Are we running a mongo DB backend?
     * 
     * @return {@code true} for mongo, {@code false} else
     */
    private boolean isMongoDBBackend() {
        return aasConfig.getAASBackend().equals(AASServerBackend.MONGODB);
    }

    /**
     * Creates a mongo DB configuration.
     * 
     * @return the configuration
     */
    private BaSyxMongoDBConfiguration createMongoDbConfiguration() {
        BaSyxMongoDBConfiguration config;
        if (this.mongoDBConfig == null) {
            config = new BaSyxMongoDBConfiguration();
            config.loadFromDefaultSource();
        } else {
            config = this.mongoDBConfig;
        }
        return config;
    }

    /**
     * Creates an AAS server decorator list.
     * 
     * @return the decorator list
     */
    private List<IAASServerDecorator> createAASServerDecoratorList() {
        List<IAASServerDecorator> aasServerDecoratorList = new ArrayList<>();

        for (IAASServerFeature aasServerFeature : aasServerFeatureList) {
            aasServerDecoratorList.add(aasServerFeature.getDecorator());
        }

        return aasServerDecoratorList;
    }

    /**
     * Loads the AAS bundles.
     */
    private void loadAASBundles() {
        if (aasBundles != null) {
            return;
        }

        List<String> aasSources = aasConfig.getAASSourceAsList();
        aasBundles = loadAASFromSource(aasSources);
    }

    /**
     * Loads AAS from source.
     * 
     * @param aasSources the sources
     * @return the AAS bundle
     */
    private Set<AASBundle> loadAASFromSource(List<String> aasSources) {
        if (aasSources.isEmpty()) {
            return Collections.emptySet();
        }

        Set<AASBundle> aasBundlesSet = new HashSet<>();

        aasSources.stream().map(this::loadBundleFromFile).forEach(aasBundlesSet::addAll);

        return aasBundlesSet;
    }

    /**
     * Loads an AAS source as bundle from a file.
     * 
     * @param aasSource the AAS source
     * @return the bundle
     */
    private Set<AASBundle> loadBundleFromFile(String aasSource) {
        try {
            if (aasSource.endsWith(".aasx")) {
                return loadBundleFromAASX(aasSource);
            } else if (aasSource.endsWith(".json")) {
                return loadBundleFromJSON(aasSource);
            } else if (aasSource.endsWith(".xml")) {
                return loadBundleFromXML(aasSource);
            }
        } catch (IOException | ParserConfigurationException | SAXException | URISyntaxException
                | InvalidFormatException e) {
            logger.error("Could not load initial AAS from source '" + aasSource + "'");
        }

        return Collections.emptySet();
    }

    /**
     * Only creates the registry, if it hasn't been set explicitly before.
     * 
     * @param aasConfig the server configuration
     * @return the registry
     */
    private IAASRegistry createRegistryFromConfig(BaSyxAASServerConfiguration aasConfig) {
        if (this.registry != null) {
            // Do not overwrite an explicitly set registry
            return this.registry;
        }
        String registryUrl = aasConfig.getRegistry();
        if (registryUrl == null || registryUrl.isEmpty()) {
            return null;
        }

        // Load registry url from config
        logger.info("Registry loaded at \"" + registryUrl + "\"");

        if (shouldUseSecuredRegistryConnection(aasConfig)) {
            return new AuthorizedAASRegistryProxy(registryUrl, aasConfig.configureAndGetAuthorizationSupplier());
        } else {
            return new AASRegistryProxy(registryUrl);
        }
    }

    /**
     * Returns whether we should use a secured registry connection.
     * 
     * @param aasConfig the AAS server configuration
     * @return whether we should use a secured registry connection
     */
    private boolean shouldUseSecuredRegistryConnection(BaSyxAASServerConfiguration aasConfig) {
        return aasConfig.isAuthorizationCredentialsForSecuredRegistryConfigured();
    }

    /**
     * Registers an environment.
     */
    private void registerEnvironment() {
        if (aasConfig.getSubmodels().isEmpty()) {
            registerFullAAS();
        } else {
            registerSubmodelsFromWhitelist();
        }
    }
    
    /**
     * Registers submodels from a whitelist.
     */
    private void registerSubmodelsFromWhitelist() {
        logger.info("Register from whitelist");
        List<AASDescriptor> descriptors = registry.lookupAll();
        List<String> smWhitelist = aasConfig.getSubmodels();
        for (String s : smWhitelist) {
            updateSMEndpoint(s, descriptors);
        }
    }

    /**
     * Registers a full AAS.
     */
    private void registerFullAAS() {
        if (registry == null) {
            logger.info("No registry specified, skipped registration");
            return;
        }

        String baseUrl = getURL();
        String aggregatorPath = VABPathTools.concatenatePaths(baseUrl, AASAggregatorProvider.PREFIX);
        AASBundleHelper.register(registry, aasBundles, aggregatorPath);
    }

    /**
     * Updates a SM endpoint.
     * 
     * @param smId the sm identifier
     * @param descriptors the related AAS descriptors
     */
    private void updateSMEndpoint(String smId, List<AASDescriptor> descriptors) {
        descriptors.forEach(desc -> {
            Collection<SubmodelDescriptor> smDescriptors = desc.getSubmodelDescriptors();
            SubmodelDescriptor smDescriptor = findSMDescriptor(smId, smDescriptors);
            updateSMEndpoint(smDescriptor);
            registry.register(desc.getIdentifier(), smDescriptor);
        });
    }

    /**
     * Updates a SM endpoint.
     * 
     * @param smDescriptor the submodel descriptor
     */
    private void updateSMEndpoint(SubmodelDescriptor smDescriptor) {
        String smEndpoint = getSMEndpoint(smDescriptor.getIdentifier());
        String firstEndpoint = smDescriptor.getFirstEndpoint();
        if (firstEndpoint.isEmpty()) {
            smDescriptor.removeEndpoint("");
        } else if (firstEndpoint.equals("/submodel")) {
            smDescriptor.removeEndpoint("/submodel");
        }
        smDescriptor.addEndpoint(smEndpoint);
    }

    /**
     * Finds a SM descriptor.
     * 
     * @param smId the SM id
     * @param smDescriptors the SM descriptors to search
     * @return the descriptor or <b>null</b>
     */
    private SubmodelDescriptor findSMDescriptor(String smId, Collection<SubmodelDescriptor> smDescriptors) {
        for (SubmodelDescriptor smDesc : smDescriptors) {
            if (smDesc.getIdentifier().getId().equals(smId)) {
                return smDesc;
            }
        }
        return null;
    }

    /**
     * Returns the SM endpoint for the given SM id.
     * 
     * @param smId the submodel id
     * @return the endpoint
     */
    private String getSMEndpoint(IIdentifier smId) {
        String aasId = getAASIdFromSMId(smId);
        String encodedAASId = VABPathTools.encodePathElement(aasId);
        String aasBasePath = VABPathTools.concatenatePaths(getURL(), encodedAASId, "aas");
        String smIdShort = getSMIdShortFromSMId(smId);
        return VABPathTools.concatenatePaths(aasBasePath, "submodels", smIdShort, "submodel");
    }

    /**
     * Returns an SM idShort from an sm identifier.
     * 
     * @param smId the submodel id
     * @return the idShort
     * @throws ResourceNotFoundException if the submodel cannot be found
     */
    private String getSMIdShortFromSMId(IIdentifier smId) {
        for (AASBundle bundle : aasBundles) {
            for (ISubmodel sm : bundle.getSubmodels()) {
                if (smId.getId().equals(sm.getIdentification().getId())) {
                    return sm.getIdShort();
                }
            }
        }
        throw new ResourceNotFoundException("Submodel in registry whitelist not found in AASBundle");
    }

    /**
     * Returns the AAS id from a submodel.
     * 
     * @param smId the submodel id
     * @return the AAS id
     * @throws ResourceNotFoundException if the submodel cannot be found
     */
    private String getAASIdFromSMId(IIdentifier smId) {
        for (AASBundle bundle : aasBundles) {
            for (ISubmodel sm : bundle.getSubmodels()) {
                if (smId.getId().equals(sm.getIdentification().getId())) {
                    return bundle.getAAS().getIdentification().getId();
                }
            }
        }
        throw new ResourceNotFoundException("Submodel in registry whitelist does not belong to any AAS in AASBundle");
    }

    /**
     * Fixes the File submodel element value paths according to the given endpoint
     * configuration.
     * 
     * @param hostName the hostname
     * @param port the port
     * @param rootPath the root path
     */
    private void modifyFilePaths(String hostName, int port, String rootPath) {
        rootPath = rootPath + "/files";
        for (AASBundle bundle : aasBundles) {
            Set<ISubmodel> submodels = bundle.getSubmodels();
            for (ISubmodel sm : submodels) {
                SubmodelFileEndpointLoader.setRelativeFileEndpoints(sm, hostName, port, rootPath);
            }
        }
    }

    /**
     * Returns the MQTT AAS client id.
     * 
     * @return the ID
     */
    private String getMqttAASClientId() {
        if (aasBundles == null || aasBundles.isEmpty()) {
            return "defaultNoShellId";
        }
        return aasBundles.stream().findFirst().get().getAAS().getIdShort();
    }

    /**
     * Returns the MQTT submodel client id.
     * 
     * @return the ID
     */
    private String getMqttSubmodelClientId() {
        return getMqttAASClientId() + "/submodelAggregator";
    }
}
