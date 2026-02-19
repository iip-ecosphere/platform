/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppInstanceStatus;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactoryDescriptor;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServiceSetup;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.ServicesAasClient;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.spring.Starter;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.Updater;
import de.iip_ecosphere.platform.support.commons.Commons;
import de.iip_ecosphere.platform.support.commons.Tailer;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.NetworkManagerAasClient;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;
import de.iip_ecosphere.platform.support.net.UriResolver;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

import static de.iip_ecosphere.platform.services.spring.SpringInstances.*;

/**
 * Service manager for Spring Cloud Stream. Requires {@link SpringInstances} set correctly before use.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
@Import(SpringCloudServiceSetup.class)
public class SpringCloudServiceManager 
    extends AbstractServiceManager<SpringCloudArtifactDescriptor, SpringCloudServiceDescriptor> {

    public static final String PLUGIN_ID = "services-spring";
    public static final String OPT_SERVICE_PREFIX = "iip.service.";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudServiceManager.class);
    private ServerManager serverManager;
    private Predicate<TypedDataConnectorDescriptor> available = c -> true;
    private Supplier<NetworkManager> networkManagerSupplier = () -> {
        NetworkManager result = null;
        try {
            result = new NetworkManagerAasClient();
        } catch (IOException e) {
            LOGGER.warn("Cannot create network manager AAS client. Using factory-provided network manager, which may "
                + "be local. AAS server running? {}", e.getMessage());
            result = NetworkManagerFactory.getInstance(); 
        }
        return result;
    };
    
    // do not rename this class or the following descriptor class! Java Service Loader
    
    /**
     * Descriptor for creating the service manager instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class SpringCloudServiceFactoryDescriptor extends SingletonPluginDescriptor<ServiceFactoryDescriptor> 
        implements ServiceFactoryDescriptor {

        /**
         * Creates an instance. [JSL]
         */
        public SpringCloudServiceFactoryDescriptor() {
            super("services", List.of(PLUGIN_ID), ServiceFactoryDescriptor.class, null);
            LifecycleHandler.consider(getClass().getClassLoader());
        }

        @Override
        protected PluginSupplier<ServiceFactoryDescriptor> initPluginSupplier(
            PluginSupplier<ServiceFactoryDescriptor> pluginSupplier) {
            return p -> this;
        }

        @Override
        public ServiceManager createInstance() {
            return new SpringCloudServiceManager();
        }
        
        @Override
        public ServiceSetup getSetup() {
            return null != SpringInstances.getConfig() ? SpringInstances.getConfig() : null;
        }

        @Override
        public AasSetup getAasSetup() {
            // may not yet have been initialized
            return null != SpringInstances.getConfig() ? SpringInstances.getConfig().getAas() : null;
        }

        @Override
        public TransportSetup getTransport() {
            return null != SpringInstances.getConfig() ? SpringInstances.getConfig().getTransport() : null;
        }
        
        @Override
        public Class<? extends ServiceSetup> getSetupClass() {
            return SpringCloudServiceSetup.class;
        }
        
    }

    // TODO upon start, scan file-system for containers and add them automatically if applicable

    /**
     * Prevents external creation.
     */
    private SpringCloudServiceManager() {
        serverManager = new ServerManager(() -> networkManagerSupplier.get()); // keep the supplier dynamic
    }
    
    /**
     * Changes the network manager supplier. [testing]
     * 
     * @param supplier the new supplier, ignored if <b>null</b>
     * @return the supplier before applying this function/changing the actual value
     */
    public Supplier<NetworkManager> setNetworkManagerClientSupplier(Supplier<NetworkManager> supplier) {
        Supplier<NetworkManager> old = networkManagerSupplier;
        if (null != supplier) {
            networkManagerSupplier = supplier;
        }
        return old;
    }

    @Override
    protected Predicate<TypedDataConnectorDescriptor> getAvailablePredicate() {
        if (null == available) {
            available = ServicesAas.createAvailabilityPredicate(SpringInstances.getConfig().getWaitingTime(), 
                SpringInstances.getConfig().getAvailabilityRetryDelay(), false);
        }
        return available;
    }
    
    @Override
    public String addArtifact(URI location) throws ExecutionException {
        URI nlocation = location.normalize();
        SpringCloudArtifactDescriptor found = null;
        for (SpringCloudArtifactDescriptor a: getArtifacts()) {
            if (a.getUri().equals(nlocation)) {
                found = a;
                break;
            }
        }
        
        if (null == found) {
            LOGGER.info("Adding {}", location);
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 0, 1, "Adding artifact " + location);
            try {
                File jarFile = UriResolver.resolveToFile(location, SpringInstances.getConfig().getDownloadDir());
                YamlArtifact yamlArtifact = null;
                if (null != jarFile) {
                    yamlArtifact = DescriptorUtils.readFromFile(jarFile);
                    updatePlugins(jarFile);
                } else {
                    DescriptorUtils.throwExecutionException("Adding " + location, 
                        "Cannot load " + location + ". Must be a (resolved) file.");
                }
                SpringCloudArtifactDescriptor artifact = SpringCloudArtifactDescriptor.createInstance(
                    yamlArtifact, location, jarFile);
                String artifactId = super.addArtifact(artifact.getId(), artifact);
                Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 1, 1, 
                    "Added artifact " + location + "  " + artifactId);
                artifact.increaseUsageCount();
                return artifactId;
            } catch (IOException e) {
                DescriptorUtils.throwExecutionException("Adding " + location, e);
                return null;
            }
        } else {
            found.increaseUsageCount();
            LOGGER.info("Found known artifact for {}, registering additional use", location);
            return found.getId();
        }
    }
    
    /**
     * Resolves/updates the plugins required by the application artifact {@code jarFile}.
     * 
     * @param jarFile update
     */
    private void updatePlugins(File jarFile) {
        InputStream resolved = Starter.findArtifact(jarFile, "resolved");
        if (null != resolved) {
            ServiceSetup setup = SpringInstances.getConfig();
            String pluginFolder = setup.getAppPluginsFolder();
            if (null == pluginFolder || pluginFolder.length() == 0) {
                pluginFolder = setup.getPluginsFolder();
            }
            Updater.updatePluginsQuiet(resolved, new File(pluginFolder), setup.getUpdateAppPlugins());
        }
    }

    /**
     * Determines the command line arguments to adjust service connections from "internal" to "external" binder.
     * 
     * @param serviceIds the services to determine the arguments for
     * @return the command line arguments
     * @see #determineExternalConnections(ServiceManager, String...)
     */
    private List<String> determineBindingServiceArgs(String... serviceIds) {
        Set<String> tmp = determineExternalConnections(this, serviceIds)
            .stream()
            .filter(c -> isValidId(c.getName()) && c.isInput()) // isInput is preliminary
            .map(c -> CmdLine.PARAM_PREFIX + Starter.OPT_SPRING_BINDINGS_PREFIX + c.getName() 
                + Starter.OPT_SPRING_BINDER_POSTFIX + CmdLine.PARAM_VALUE_SEP + Starter.EXTERNAL_BINDER_NAME)
            .collect(Collectors.toSet());
        List<String> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }
    
    /**
     * Determines the cloud function argument for the given services.
     * 
     * @param serviceIds the services to determine the arguments for
     * @return the command line arguments
     * @see #determineFunctionalConnections(ServiceManager, String...)
     */
    private String determineCloudFunctionArg(String... serviceIds) {
        String[] sIds = new String[serviceIds.length];
        for (int s = 0; s < sIds.length; s++) {
            sIds[s] = ServiceBase.getServiceId(serviceIds[s]); // this part is static and independent of app id
        }
        return CmdLine.PARAM_PREFIX + Starter.OPT_SPRING_FUNCTION_DEF + CmdLine.PARAM_VALUE_SEP 
            + SpringCloudServiceDescriptor.toFunctionDefinition(determineFunctionalConnections(this, sIds));
    }
    
    /**
     * Determines the command line values for the spring component conditionals. [public, static for testing]
     * 
     * @param mgr the manager instance
     * @param serviceIds the services to determine the arguments for
     * @return the arguments
     */
    public static List<String> determineSpringConditionals(ServiceManager mgr, String... serviceIds) {
        List<String> result = new ArrayList<>();

        Set<ArtifactDescriptor> artifacts = new HashSet<>();
        Set<String> activeServices = new HashSet<>();
        Set<String> activeAppIds = new HashSet<>();
        Set<String> activeAppInstanceIds = new HashSet<>();
        for (String id: serviceIds) {
            ServiceDescriptor service = mgr.getService(id);
            artifacts.add(service.getArtifact());
            activeServices.add(id);
            activeAppIds.add(ServiceBase.getApplicationId(id));
            activeAppInstanceIds.add(ServiceBase.getApplicationInstanceId(id));
        }
        
        Set<String> allServices = new HashSet<>();
        for (ArtifactDescriptor a: artifacts) {
            for (ServiceDescriptor s : a.getServices()) {
                String sId = s.getId();
                String aId = ServiceBase.getApplicationId(sId);
                String iId = ServiceBase.getApplicationInstanceId(sId);
                boolean addAsActive = false;
                if (iId.length() > 0) {
                    addAsActive = activeAppInstanceIds.contains(iId);
                } else if (aId.length() > 0) {
                    addAsActive = activeAppIds.contains(aId);
                } else {
                    addAsActive = activeAppInstanceIds.contains(""); // filled above, better than appId
                }
                if (addAsActive) {
                    allServices.add(s.getId());
                }
            }
        }

        if (activeServices.size() != allServices.size()) {
            for (String id: allServices) {
                String sId = ServiceBase.getServiceId(id); // annotation declaration in code is static
                result.add(CmdLine.PARAM_PREFIX + OPT_SERVICE_PREFIX + sId 
                    + CmdLine.PARAM_VALUE_SEP + activeServices.contains(id));
            }
        }
        
        return result;
    }
    
    /**
     * Returns the lead and ensemble services of service {@code id} (viewing {@code id} as potential 
     * ensemble leader) from a given set of services.
     * 
     * @param id the service to return the lead/ensemble services for
     * @param serviceIds the services to project the result from
     * @return {@code id} and connected ensemble services stated in {@code serviceIds}
     */
    private String[] serviceAndEnsemble(String id, String[] serviceIds) {
        List<String> tmp = new ArrayList<String>();
        tmp.add(id);
        for (String sId: serviceIds) {
            if (!sId.equals(id)) {
                SpringCloudServiceDescriptor service = getService(sId);
                SpringCloudServiceDescriptor leader = service.getEnsembleLeader();
                if (null != leader && leader.getId().equals(id)) {
                    tmp.add(service.getId());
                }
            }
        }
        return tmp.toArray(new String[0]);
    }
    
    @Override
    public void startService(String... serviceIds) throws ExecutionException {
        startService(null, serviceIds);
    }
    
    /**
     * Handles service start options.
     * 
     * @param options the options
     * @param serviceIds the service ids to start
     */
    private void handleOptions(Map<String, String> options, String[] serviceIds) {
        handleOptions(options, this, serviceIds);
    }

    /**
     * Handles service start options. [public, static for testing]
     * 
     * @param options the options
     * @param serviceIds the service ids to start
     * @param mgr the service manager
     */
    public static void handleOptions(Map<String, String> options, ServiceManager mgr, String... serviceIds) {
        if (null != options) {
            String opt = options.get(OPTION_ENSEMBLE);
            if (null != opt) {
                handleOptionEnsemble(opt, serviceIds, mgr);
            }

            opt = options.get(OPTION_ARGS);
            List<String> argList = null;
            if (null != opt) {
                List<?> tmp = JsonUtils.fromJson(opt, List.class);
                argList = new ArrayList<>();
                for (Object t : tmp) {
                    argList.add(t.toString());
                }
            }
            for (String sId : serviceIds) {
                ServiceDescriptor sd = mgr.getService(sId);
                if (sd != null) {
                    sd.setAdditionalArguments(argList); // set/reset
                } else {
                    LoggerFactory.getLogger(SpringCloudServiceManager.class).info(
                        "Cannot set options on service {} as service is null", sId);
                }
            }
        }
    }
    
    /**
     * Replaces all keys/values in {@code optMap} by substitutes given in {@code subst}.
     * 
     * @param optMap the option map
     * @param subst the substitution table (original-substitute)
     * @return the copied, substituted {@code optMap}
     */
    private static Map<Object, Object> replaceAll(Map<?, ?> optMap, Map<String, String> subst) {
        Map<Object, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> ent : optMap.entrySet()) {
            Object key = substitute(ent.getKey(), subst);
            Object val = substitute(ent.getValue(), subst);
            result.put(key, val);
        }
        return result;
    }

    /**
     * Substitutes {@code val} by the corresponding value in {@code subst}.
     * 
     * @param val the value, may be <b>null</b>
     * @param subst the substitution table (original-substitute)
     * @return the substituted value, may be <b>null</b> if {@code val} was <b>null</b>
     */
    private static Object substitute(Object val, Map<String, String> subst) {
        if (null != val && subst.containsKey(val)) {
            val = subst.get(val);
        }
        return val;
    }
    
    /**
     * Creates a default substitution mapping for {@code serviceIds}, i.e., from the simple id to the full id with 
     * application id and application instance id only if both application ids are given.
     * 
     * @param serviceIds the service ids to create the substitution for
     * @return the substitution mapping
     * @see #replaceAll(Map, Map)
     */
    private static Map<String, String> createServiceAppIdSubstitution(String... serviceIds) {
        Map<String, String> substitution = new HashMap<>();
        for (String sId : serviceIds) {
            String simpleId = ServiceBase.getServiceId(sId);
            String appId = ServiceBase.getApplicationId(sId);
            String appInstId = ServiceBase.getApplicationInstanceId(sId);
            if (appId.length() > 0 && appInstId.length() > 0) { // substitute only if we have the additional IDs
                substitution.put(simpleId, sId);
            }
        }
        return substitution;
    }
    
    /**
     * Handles the ensemble option for service start.
     * 
     * @param opt the option as JSON string
     * @param serviceIds the service ids to start
     * @param mgr the service manager
     */
    private static void handleOptionEnsemble(String opt, String[] serviceIds, ServiceManager mgr) {
        Map<?, ?> optMap = JsonUtils.fromJson(opt, Map.class);
        if (null != optMap) {
            Set<String> actServices = CollectionUtils.addAll(new HashSet<>(), serviceIds);
            Map<String, String> substitution = createServiceAppIdSubstitution(serviceIds);
            for (Map.Entry<?, ?> ent: replaceAll(optMap, substitution).entrySet()) {
                String ensemble = ent.getKey().toString();
                String leader = ent.getValue().toString();
                if (actServices.contains(ensemble)) { // leader may be null to reset, must not be there
                    ServiceDescriptor ensDesc = mgr.getService(ensemble);
                    ServiceDescriptor leaderDesc = mgr.getService(leader);
                    if (ensDesc instanceof SpringCloudServiceDescriptor && ensDesc.isTopLevel()
                        && (leaderDesc == null || leaderDesc instanceof SpringCloudServiceDescriptor)) {
                        LOGGER.info("Changing ensemble leader of {} to {}", ensDesc.getId(), 
                            leaderDesc == null ? null : leaderDesc.getId());
                        ((SpringCloudServiceDescriptor) ensDesc).setEnsembleLeader(
                            (SpringCloudServiceDescriptor) leaderDesc);
                    }
                }
            }
        }
    }
    
    /**
     * Turns the application (instance) id into a command line argument.
     * 
     * @param serviceId the service id to take the application (instance) id from
     * @param cmdArgs the command line arguments
     */
    public static void addAppId(String serviceId, List<String> cmdArgs) {
        String aId = ServiceBase.getApplicationId(serviceId);
        String iId = ServiceBase.getApplicationInstanceId(serviceId);
        if (aId.length() > 0 && iId.length() > 0) {
            String appId = aId + ServiceBase.APPLICATION_SEPARATOR + iId;
            cmdArgs.add(CmdLine.PARAM_PREFIX + Starter.PARAM_IIP_APP_ID + CmdLine.PARAM_VALUE_SEP + appId);
        }
    }

    /**
     * Marks the use of a server by a service.
     * 
     * @param started was the service started; if not, ignore call
     * @param service the service to mark
     * @param register register or unregister the server use
     * @param netClient the network client used for marking, may be <b>null</b>
     * @return the network client used for marking, for instance reuse, may be <b>null</b>
     */
    private NetworkManager markServerUse(boolean started, SpringCloudServiceDescriptor service, boolean register,
        NetworkManager netClient) {
        String id = service.getSvc().getNetMgtKey();
        if (id != null) { // prevent warnings if there are no server specs to process
            if (netClient == null) {
                netClient = networkManagerSupplier.get();
            }
            if (null != netClient) { // may be disabled in testing
                if (register) {
                    netClient.registerInstance(id, NetUtils.getOwnHostname());
                } else {
                    netClient.unregisterInstance(id, NetUtils.getOwnHostname());
                }
            }
        }
        return netClient;
    }

    /**
     * Reconfigures {@code service} if {@code started} with {@code params}.
     * 
     * @param service the service to be reconfigured
     * @param started whether it was (just) started
     * @param options the options to reconfigure (serviceId/name/value)
     * @throws ExecutionException if reconfiguration failed
     */
    @SuppressWarnings("unchecked")
    private static void reconfigure(SpringCloudServiceDescriptor service, boolean started, Map<String, String> options) 
        throws ExecutionException {
        if (started && null != options) {
            String txt = options.get(OPTION_PARAMS);
            if (null != txt) {
                Map<Object, Object> allParams = JsonUtils.fromJson(txt, Map.class);
                Object tmp = allParams.get(service.getId());
                if (tmp instanceof Map) {
                    try {
                        Map<String, String> params = new HashMap<>();
                        for (Map.Entry<Object, Object> e : ((Map<Object, Object>) tmp).entrySet()) {
                            params.put(e.getKey().toString(), e.getValue().toString());
                        }
                        ServicesAasClient client = new ServicesAasClient(Id.getDeviceIdAas());
                        client.reconfigureService(service.getId(), params);
                    } catch (IOException e) {
                        throw new ExecutionException(e);
                    }
                }
            }
        }
    }
    
    /**
     * Registers/pre-allocates the network ports for all given services.
     * 
     * @param serviceIds the service ids
     * @return {@code serviceIds}
     * @see SpringCloudServiceDescriptor#registerNetworkPorts()
     */
    private String[] registerNetworkPorts(String[] serviceIds) {
        for (String sId : serviceIds) {
            SpringCloudServiceDescriptor service = getService(sId);
            if (null != service) {
                service.registerNetworkPorts();
            }
        }
        return serviceIds;
    }

    @Override
    public void startService(Map<String, String> options, String... serviceIds) throws ExecutionException {
        LOGGER.info("Received request to start services {} (options {})", Arrays.toString(serviceIds), options);
        serverManager.startServers(options, getArtifacts());
        serviceIds = registerNetworkPorts(pruneServers(this, serviceIds));
        
        List<String> instServiceIds = checkServiceInstances(serviceIds);
        serviceIds = topLevel(this, serviceIds); // avoid accidentally accessing family members
        LOGGER.info("Preparing to start top-level services {} (options {})", Arrays.toString(serviceIds), options);
        handleOptions(options, serviceIds);
        handleInstantiatedServices(instServiceIds); // after handle options, i.e., turning services to ensemble leaders
        AppDeployer deployer = getDeployer();
        // TODO add/check causes for failing, potentially re-sort remaining services iteratively 
        List<String> errors = new ArrayList<>();
        LOGGER.info("Starting services {} (options {})", Arrays.toString(serviceIds), options);
        SpringCloudServiceSetup config = getConfig();
        int step = 0;
        handleFamilyProcesses(serviceIds, true);
        // re-link binders if needed, i.e., subset shall be started locally
        NetworkManager netClient = null;
        List<String> commonServiceArgs = determineBindingServiceArgs(serviceIds);
        commonServiceArgs.addAll(config.getServiceCmdArgs());
        for (String sId : sortByDependency(serviceIds, true)) {
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, step, serviceIds.length + 1, "Starting " + sId);
            SpringCloudServiceDescriptor service = getService(sId);
            if (null == service) {
                errors.add("No service for id '" + sId + "' known.");
            } else {
                LOGGER.info("Preparing {} for start", sId);
                String[] sIdEns = serviceAndEnsemble(sId, serviceIds);
                List<String> externalServiceArgs = new ArrayList<>(commonServiceArgs);
                addAppId(sId, externalServiceArgs);
                // adjust spring function definition from application.yml if subset of services shall be started 
                externalServiceArgs.add(determineCloudFunctionArg(sIdEns));
                externalServiceArgs.addAll(determineSpringConditionals(this, sIdEns));
                AppDeploymentRequest req = service.createDeploymentRequest(config, externalServiceArgs, 
                    getMemLimit(options, sIdEns));
                boolean started = false;
                if (null != req) {
                    setState(service, ServiceState.DEPLOYING);
                    LOGGER.info("Starting " + sId);
                    String dId = deployer.deploy(req);
                    waitFor(dId, null, s -> null == s || s == DeploymentState.deploying);
                    LOGGER.info("Started " + dId + ": " + deployer.status(dId));
                    service.waitForAdminServer(getConfig().getWaitingTime()); // well, full waiting time for now
                    AppStatus status = deployer.status(dId); 
                    service.setDeploymentId(dId);
                    if (DeploymentState.deployed == status.getState()) {
                        service.attachStub();
                        setState(service, ServiceState.STARTING);
                        LOGGER.info("Starting " + sId + " completed");
                        started = true;
                    } else {
                        setState(service, ServiceState.FAILED);
                        errors.add("Starting service id '" + sId + "' failed:\n" + getDeployer().getLog(dId));
                        LOGGER.info("Starting " + dId + " failed");
                    }
                } else {
                    LOGGER.info("Starting ensemble service " + sId);
                    ServiceState ensState = service.getEnsembleLeader().getState();
                    if (ServiceState.RUNNING == ensState) {
                        service.attachStub();
                        setState(service, ServiceState.STARTING);
                        LOGGER.info("Starting ensemble service " + sId + " completed");
                        started = true;
                    } else {
                        setState(service, ServiceState.FAILED);
                        errors.add("Starting ensemble service id '" + sId + "' failed: See " 
                            + service.getEnsembleLeader().getId());
                        LOGGER.info("Starting ensemble service " + sId + " failed");
                    }
                }
                reconfigure(service, started, options);
                netClient = markServerUse(started, service, true, netClient);
            }
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, step++, serviceIds.length + 1, "Started " + sId);
        }
        checkErrors(errors);
        LOGGER.info("Started services " + Arrays.toString(serviceIds));
    }
    
    @Override
    protected SpringCloudServiceDescriptor instantiateFromTemplate(SpringCloudServiceDescriptor template, 
        String serviceId) {
        SpringCloudServiceDescriptor result = template.instantiate(serviceId);
        result.getArtifact().addService(result);
        return result;
    }
    
    @Override
    protected void handleInstantiatedServices(Iterable<String> sId) {
        // must happen after all templates/services are instantiated but before the AAS is modified
        for (String id : sId) {
            SpringCloudServiceDescriptor desc = getService(id);
            if (null != desc) { 
                desc.registerNetworkPorts();
            }
        }
        // modify AAS
        super.handleInstantiatedServices(sId);
    }    
    
    /**
     * Returns the specified memory limit of {@code sIds} given in service descriptors or {@code options}.
     * 
     * @param options the service start options
     * @param sIds the service ids of the service and the (optional) ensemble services
     * @return the memory limit in <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (i.e., "m"), may 
     *     be <b>null</b> for none
     */
    private String getMemLimit(Map<String, String> options, String... sIds) {
        String result = null;
        long mem = 0;
        Map<Object, Object> optMap = null;
        if (null != options) { // overriden value takes precedence
            String opt = options.get(OPTION_MEMLIMITS);
            if (null != opt) {
                Map<String, String> substitution = createServiceAppIdSubstitution(sIds);
                optMap = replaceAll(JsonUtils.fromJson(opt, Map.class), substitution);
            }
        }
        
        for (String sId: sIds) {
            long sMem = 0;
            SpringCloudServiceDescriptor desc = getService(sId);
            if (null != desc) {
                sMem = desc.getMemory();
            }
            if (null != optMap) { // overriden value takes precedence
                Object memLimitOpt = optMap.get(sId);
                if (null != memLimitOpt) {
                    try {
                        sMem = Long.parseLong(sId.toString());
                    } catch (NumberFormatException e) {
                        LoggerFactory.getLogger(SpringCloudServiceManager.class).info(
                            "Memlimit option for {} not a long value: {}", sId, e.getMessage());
                    }
                }
            }
            if (sMem > 0) { // valid only if not negative
                mem += sMem;
            }
        }
        if (mem > 0) {
            result = Utils.formatToMeBi(mem, 2);
        }
        return result;
    }
    
    /**
     * Prepares the processes of the family members.
     * 
     * @param serviceIds the service ids of the top-level services to be started
     * @param start do startup or shutdown of the family services
     * @throws ExecutionException when preparing the service fails for some reason
     */
    private void handleFamilyProcesses(String[] serviceIds, boolean start) throws ExecutionException {
        Set<ArtifactDescriptor> artifacts = new HashSet<>();
        Set<String> activeServices = new HashSet<>();
        for (String id: serviceIds) {
            ServiceDescriptor service = getService(id);
            artifacts.add(service.getArtifact());
            activeServices.add(id);
        }
        for (ArtifactDescriptor a: artifacts) {
            for (ServiceDescriptor s: a.getServices()) {
                if (!s.isTopLevel() && s.getEnsembleLeader() != null 
                    && activeServices.contains(s.getEnsembleLeader().getId())) {
                    SpringCloudServiceDescriptor famMember = getService(s.getId());
                    LoggerFactory.getLogger(SpringCloudServiceManager.class).info(
                        "Preparing processes for non-top-level ensemble service {}", s.getId());
                    if (start) {
                        // TODO if there is a real process, what to do with the port?
                        famMember.startProcess(getConfig(), famMember.getSvc().getProcess());
                    } else {
                        famMember.setState(ServiceState.STOPPING);
                    }
                }
            }
        }
    }
    
    /**
     * Checks the given errors list. If there are errors, composes a message and throws an exception.
     * 
     * @param errors the errors to check for
     * @throws ExecutionException an exception if there are errors
     */
    private void checkErrors(List<String> errors) throws ExecutionException {
        if (errors.size() > 0) {
            String result = "";
            for (String s : errors) {
                if (result.length() > 0) {
                    result += "\n";
                }
                result += s;
            }
            throw new ExecutionException(result, null);
        }
    }
    
    /**
     * Waits for completing a deployer operation.
     * 
     * @param id the service id to wait for
     * @param initState the initial deployment state, may be <b>null</b> for none
     * @param endCond the end condition when to stop waiting, anyway at longest 
     *   {@link SpringCloudServiceSetup#getWaitingTime()}.
     * @return the service state at the end of waiting
     */
    private DeploymentState waitFor(String id, DeploymentState initState, Predicate<DeploymentState> endCond) {
        AppDeployer deployer = getDeployer();
        int waitingTime = getConfig().getWaitingTime();
        long start = System.currentTimeMillis();
        DeploymentState state = null;
        do {
            if (endCond.test(state)) { // preliminary, always synchronous deployment
                state = deployer.status(id).getState();
                TimeUtils.sleep(500);
            }
            if (System.currentTimeMillis() - start > waitingTime) {
                LoggerFactory.getLogger(SpringCloudServiceManager.class).error("While deploying {}: timeout {}", 
                    id, waitingTime);
                break;
            }
        } while (endCond.test(state));
        return state;
    }

    @Override
    public void stopService(String... serviceIds) throws ExecutionException {
        serviceIds = topLevel(this, pruneServers(this, serviceIds)); // avoid accidentally accessing family members
        List<String> errors = new ArrayList<>();
        AppDeployer deployer = getDeployer();
        LOGGER.info("Stopping services " + Arrays.toString(serviceIds));
        int step = 0;
        handleFamilyProcesses(serviceIds, false);
        NetworkManager netClient = null;
        for (String ids : sortByDependency(serviceIds, false)) {
            try {
                Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, step, serviceIds.length + 1, 
                    "Stopping service " + ids);
                SpringCloudServiceDescriptor service = getService(ids);
                String id = service.getDeploymentId();
                if (null != id) {
                    AppStatus status = deployer.status(id);
                    if (null != status) {
                        DeploymentState state = status.getState();
                        if (state != null) { // if it was in a failure, also try to get rid of it, #50
                            setState(service, ServiceState.STOPPING);
                            LOGGER.info("Stopping " + id + "... ");
                            deployer.undeploy(id);
                            state = waitFor(id, state, s -> DeploymentState.deployed == s);
                            LOGGER.info("Stopping " + id + "... ");
                            if (null == state || state == DeploymentState.undeployed) {
                                setState(service, ServiceState.STOPPED); // to be safe, shall be done by service
                            } else if (state == DeploymentState.error || state == DeploymentState.failed) {
                                setState(service, ServiceState.FAILED);
                            }
                        } else {
                            setState(service, ServiceState.STOPPING);
                        }
                    }
                } else {
                    setState(service, ServiceState.STOPPING);
                }
                service.detachStub();
                Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, step++, serviceIds.length + 1, 
                    "Stopped service " + ids);
                netClient = markServerUse(true, service, false, netClient);
            } catch (ExecutionException e) {
                errors.add(e.getMessage());
            }
        }
        checkErrors(errors);
        LOGGER.info("Stopped services " + Arrays.toString(serviceIds));
        serverManager.stopServers(getArtifacts());
    }

    @Override
    public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        super.migrateService(serviceId, resourceId);
        throw new ExecutionException("not implemented", null);  // TODO must change host value in AAS!
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        checkId(artifactId, "artifactId");
        SpringCloudArtifactDescriptor desc = getArtifact(artifactId);
        if (null != desc && desc.decreaseUsageCount() > 0) {
            LOGGER.info("Decreased usage of artifact {}", artifactId);
        } else {
            LOGGER.info("Removing artifact {}", artifactId);
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 0, 1, "Removing artifact " + artifactId);
            super.removeArtifact(artifactId);
            if (null != desc) {
                File jar = desc.getJar();
                File downloadDir = getConfig().getDownloadDir();
                if (null != jar && null != downloadDir && jar.toPath().startsWith(downloadDir.toPath())) {
                    if (getConfig().getDeleteArtifacts()) {
                        FileUtils.deleteQuietly(desc.getJar());
                    }
                }
            }
            LOGGER.info("Removed artifact {}",  artifactId);
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 1, 1, "Removied artifact " + artifactId);
        }
    }

    @Override
    public void updateService(String serviceId, URI location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void switchToService(String serviceId, String target) throws ExecutionException {
        super.switchToService(serviceId, target);
        throw new ExecutionException("not implemented", null); // TODO
    }

    @Override
    public void cloneArtifact(String artifactId, URI location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    protected void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        DescriptorUtils.setState(service, state);
    }

    @Override
    public void clear() {
        AppDeployer deployer = getDeployer();
        for (SpringCloudServiceDescriptor desc: getServices()) {
            String deploymentId = desc.getDeploymentId();
            if (null != deploymentId) {
                AppStatus status = deployer.status(deploymentId);
                if (DeploymentState.deployed == status.getState()) {
                    getDeployer().undeploy(deploymentId);
                }
            }
        }
        super.clear();
    }
    
    /**
     * Returns whether {@code desc} is loggable, i.e., shomehow running and depoyed.
     * 
     * @param desc the descriptor
     * @return {@code true} for loggable, {@code false} else
     */
    private boolean isLoggable(SpringCloudServiceDescriptor desc) {
        boolean result = false;
        if (null != desc) {
            result = desc.getDeploymentId() != null;
            ServiceState state = desc.getState();
            result &= ServiceState.STARTING == state || ServiceState.RUNNING == state || ServiceState.STOPPING == state;
        }
        return result;
    }
    
    @Override
    public String streamLog(String serviceId, StreamLogMode mode) throws ExecutionException {
        String result = "[]";
        SpringCloudServiceDescriptor desc = getService(serviceId);
        if (mode != StreamLogMode.NONE && isLoggable(desc)) {
            AppDeployer deployer = getDeployer();
            String deploymentId = desc.getDeploymentId();
            AppStatus status = deployer.status(deploymentId);
            List<URI> uris = new ArrayList<URI>();
            if (DeploymentState.deployed == status.getState()) {
                if (StreamLogMode.STOP == mode) {
                    desc.closeCloseables(s -> isLogStreamCloseable(s), c -> {
                        return (c instanceof LogTailerListener) && ((LogTailerListener) c).decreaseUsageCount() == 0;
                    });
                    LOGGER.info("Stopping log streaming for {}", serviceId);
                } else {
                    desc.iterClosables(s -> isLogStreamCloseable(s), null, c -> {
                        if (c instanceof LogTailerListener) {
                            LogTailerListener lt = (LogTailerListener) c;
                            uris.add(lt.getURI());
                            lt.increaseUsageCount();
                        }
                    });
                    if (uris.isEmpty()) {
                        for (AppInstanceStatus inst : status.getInstances().values()) {
                            attachTailer(inst, "stdout", mode, desc, uris);
                            attachTailer(inst, "stderr", mode, desc, uris);
                        }
                    }
                    LOGGER.info("Starting/registering log streaming for {} in mode {}: {}", serviceId, mode, uris);
                }
            } else {
                LOGGER.info("Log streaming request for {} not fulfilled: deployer state {}", 
                    serviceId, status.getState());
            }
            result = JsonUtils.toJson(uris);
        } else {
            LOGGER.info("Log streaming request for {} not fulfilled: mode {}, loggable {}, desc {}", serviceId, mode, 
                isLoggable(desc), desc);
        }
        return result;
    }

    /**
     * Returns whether {@code key} is a log stream closeable.
     * 
     * @param key the key
     * @return {@code true} for is a closeable, {@code false} else
     */
    private static boolean isLogStreamCloseable(String key) {
        return key.startsWith("stdout_") || key.startsWith("stderr_");
    }
    
    /**
     * Attaches a tailer to an app instance.
     * 
     * @param inst the instance
     * @param field the field to attach to 
     * @param mode the streaming mode
     * @param desc the service descriptor
     * @param result the result
     */
    private void attachTailer(AppInstanceStatus inst, String field, StreamLogMode mode, 
        SpringCloudServiceDescriptor desc, List<URI> result) {
        Class<?> cls = inst.getClass();
        try {
            Field f = cls.getDeclaredField(field);
            f.setAccessible(true);
            Object tmp = f.get(inst);
            if (tmp instanceof File) {
                File file = (File) f.get(inst);
                SpringCloudServiceSetup setup = SpringInstances.getConfig();
                LogTailerListener listener = new LogTailerListener(setup.getAas(), setup.getTransport(), 
                    desc.getId().replace(ServiceBase.APPLICATION_SEPARATOR, "/") + "/" + field);
                Tailer tailer = Commons.getInstance().createTailer(file, listener, Duration.ofMillis(300), 
                    mode == StreamLogMode.TAIL);
                listener.attachTailer(tailer);
                desc.attachCloseable(field + "_" + inst.getId(), listener);
                URI uri = listener.getURI();
                if (null != uri) {
                    result.add(uri);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LoggerFactory.getLogger(getClass()).error("Attaching logging {}: {}", field, e.getMessage());
        }
    }

}
