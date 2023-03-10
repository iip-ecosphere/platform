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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServerWrapper;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactoryDescriptor;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.ServicesAasClient;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.spring.Starter;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.services.spring.descriptor.Server;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.NetworkManagerAasClient;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;
import de.iip_ecosphere.platform.support.net.UriResolver;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

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

    public static final String OPT_SERVICE_PREFIX = "iip.service.";
    private static final String PROGRESS_COMPONENT_ID = "Spring Cloud Service Manager";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudServiceManager.class);
    private Predicate<TypedDataConnectorDescriptor> available = c -> true;
    private Map<SpringCloudServiceDescriptor, de.iip_ecosphere.platform.support.Server> 
        runningServers = new HashMap<>();
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
    public static class SpringCloudServiceFactoryDescriptor implements ServiceFactoryDescriptor {

        @Override
        public ServiceManager createInstance() {
            return new SpringCloudServiceManager();
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
        
    }

    // TODO upon start, scan file-system for containers and add them automatically if applicable

    /**
     * Prevents external creation.
     */
    private SpringCloudServiceManager() {
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
        LOGGER.info("Adding " + location);
        Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 0, 1, "Adding artifact " + location);
        try {
            File jarFile = UriResolver.resolveToFile(location, SpringInstances.getConfig().getDownloadDir());
            YamlArtifact yamlArtifact = null;
            if (null != jarFile) {
                yamlArtifact = DescriptorUtils.readFromFile(jarFile);
            } else {
                DescriptorUtils.throwExecutionException("Adding " + location, 
                    "Cannot load " + location + ". Must be a (resolved) file.");
            }
            SpringCloudArtifactDescriptor artifact = SpringCloudArtifactDescriptor.createInstance(
                yamlArtifact, location, jarFile);
            String artifactId = super.addArtifact(artifact.getId(), artifact);
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 1, 1, "Added artifact " + location + "  " + artifactId);
            return artifactId;
        } catch (IOException e) {
            DescriptorUtils.throwExecutionException("Adding " + location, e);
            return null;
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
        return CmdLine.PARAM_PREFIX + Starter.OPT_SPRING_FUNCTION_DEF + CmdLine.PARAM_VALUE_SEP 
            + SpringCloudServiceDescriptor.toFunctionDefinition(determineFunctionalConnections(this, serviceIds));
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
        for (String id: serviceIds) {
            ServiceDescriptor service = mgr.getService(id);
            artifacts.add(service.getArtifact());
            activeServices.add(id);
        }
        
        Set<String> allServices = new HashSet<>();
        for (ArtifactDescriptor a: artifacts) {
            allServices.addAll(a.getServiceIds());
        }

        if (activeServices.size() != allServices.size()) {
            for (String id: allServices) {
                result.add(CmdLine.PARAM_PREFIX + OPT_SERVICE_PREFIX + id 
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
                mgr.getService(sId).setAdditionalArguments(argList); // set/reset
            }
        }
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
        Set<String> actServices = new HashSet<>();
        CollectionUtils.addAll(actServices, serviceIds);
        if (null != optMap) {
            for (Map.Entry<?, ?> ent: optMap.entrySet()) {
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
        String appId = ServiceBase.getApplicationId(serviceId) + ServiceBase.getApplicationInstanceId(serviceId);
        if (appId.length() > 0) {
            cmdArgs.add(CmdLine.PARAM_PREFIX + Starter.PARAM_IIP_APP_ID + CmdLine.PARAM_VALUE_SEP + appId);
        }
    }
    /**
     * String returns the host names/ids to be considered equal for this (the executing) device.
     *  
     * @return the ids
     */
    private static Set<String> getThisDeviceHostIds() {
        Set<String> thisDevice = new HashSet<>();
        thisDevice.add(ServerAddress.LOCALHOST);
        thisDevice.add("127.0.0.1");
        thisDevice.add(NetUtils.getOwnHostname()); // may require netmask
        thisDevice.add(NetUtils.getOwnIP()); // may require netmask
        thisDevice.add(Id.getDeviceId());
        thisDevice.add(Id.getDeviceIdAas());
        return thisDevice;
    }

    /**
     * Returns the servers to be started.
     * 
     * @param options optional map of optional options to be passed to the service manager, 
     *     {@see #startService(Map, String...)}
     * @return the id-descriptor mapping of servers to be started, may be empty
     */
    private Map<String, SpringCloudServiceDescriptor> getServers(Map<String, String> options) {
        Map<String, SpringCloudServiceDescriptor> servers = new HashMap<>();
        Map<String, String> hostMap = new HashMap<>();
        if (null != options) {
            String opt = options.get(OPTION_SERVERS);
            if (null != opt) {
                Map<?, ?> optMap = JsonUtils.fromJson(opt, Map.class);
                for (Map.Entry<?, ?> ent : optMap.entrySet()) {
                    hostMap.put(ent.getKey().toString(), ent.getValue().toString());
                }
            }
        }
        Set<String> thisDevice = getThisDeviceHostIds();
        Set<String> knownServers = new HashSet<>();
        for (SpringCloudArtifactDescriptor desc : getArtifacts()) {
            for (SpringCloudServiceDescriptor s: desc.getServers()) {
                String id = s.getId();
                knownServers.add(id);
                String host = hostMap.get(id);
                if (null == host) {
                    host = s.getServer().getHost();
                }
                if (thisDevice.contains(host)) {
                    servers.put(id, s);
                }
            }
        }
        if (knownServers.size() > 0) {
            LOGGER.info("Preparing server start: Of known servers {} starting {} on this host ({})", knownServers, 
                servers, thisDevice);
        }
        return servers;
    }

    /**
     * Starting server instances.
     * 
     * @param options optional map of optional options to be passed to the service manager, 
     *     {@see #startService(Map, String...)}
     * @see #getThisDeviceHostIds()
     */
    private void startServers(Map<String, String> options) {
        String myHost = NetUtils.getOwnHostname();
        Map<String, SpringCloudServiceDescriptor> servers = getServers(options);
        if (servers.size() > 0) { // prevent warnings if there are no server specs to process
            NetworkManager netClient = networkManagerSupplier.get();
            if (null != netClient) {
                Map<SpringCloudArtifactDescriptor, ClassLoader> loaders = new HashMap<>();
                for (SpringCloudServiceDescriptor s: servers.values()) {
                    String id = s.getId();
                    if (null == netClient.getPort(id)) {
                        try {
                            // current assumption: process comes with valid path, temporary path -> cmdLine
                            Starter.extractProcessArtifacts(id, s.getServer(), s.getArtifact().getJar(), 
                                SpringInstances.getConfig().getDownloadDir());
                            Server ser = s.getServer();
                            ClassLoader loader = loaders.get(s.getArtifact());
                            if (null == loader) {
                                loader = DescriptorUtils.determineArtifactClassLoader(s);
                                loaders.put(s.getArtifact(), loader);
                            }
                            Class<?> cls = Class.forName(ser.getCls(), true, loader); 
                            Object o;
                            try {
                                List<String> cmdLine = s.collectCmdArguments(getConfig(), ser.getPort(), "");
                                o = cls.getConstructor(String[].class).newInstance(
                                    (Object) cmdLine.toArray(new String[0]));
                            } catch (NoSuchMethodException e) {
                                o = cls.getConstructor().newInstance();
                            }
                            if (o instanceof de.iip_ecosphere.platform.support.Server) {
                                ServerWrapper sv = new ServerWrapper((de.iip_ecosphere.platform.support.Server) o);
                                sv.start();
                                setStateSafe(s, ServiceState.STARTING);
                                LOGGER.info("Starting server {} ", id);
                                ServerAddress adr = new ServerAddress(Schema.IGNORE, myHost, ser.getPort());
                                adr = netClient.reservePort(id, adr);
                                runningServers.put(s, sv);
                                setStateSafe(s, ServiceState.RUNNING);
                                LOGGER.info("Started server {} ", id);
                            } else {
                                LOGGER.error("Starting server {}. Specified class does not implement support.Server. "
                                    + "Cannot start.", id);
                            }
                        } catch (ClassNotFoundException  e) {
                            LOGGER.error("Starting server {}: Cannot find class {}", id, e.getMessage());
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException 
                            | NoSuchMethodException e) {
                            LOGGER.error("Starting server {}, cannot invoke constructor: {}", id, e.getMessage());
                        } catch (IOException e) {
                            LOGGER.error("Starting server {}, unpacking artfiact: {}", id, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Stopping server instances.
     */
    private void stopServers() {
        List<SpringCloudServiceDescriptor> servers = new ArrayList<>();
        for (SpringCloudArtifactDescriptor desc : getArtifacts()) {
            for (SpringCloudServiceDescriptor s: desc.getServers()) {
                if (runningServers.containsKey(s)) {
                    servers.add(s);
                }
            }
        }
        if (servers.size() > 0) { // prevent warnings if there are no server specs to process
            NetworkManager netClient = networkManagerSupplier.get();
            if (null != netClient) {
                for (SpringCloudServiceDescriptor s: servers) {
                    String id = s.getId();
                    if (netClient.getRegisteredInstances(id) == 0) {
                        setStateSafe(s, ServiceState.STOPPING);
                        de.iip_ecosphere.platform.support.Server sv = runningServers.remove(s);
                        sv.stop(true);
                        netClient.releasePort(id);
                        setStateSafe(s, ServiceState.STOPPED);
                        LOGGER.info("Stopped server {} ", id);
                    }
                }
            }
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
     * Reconfigures {@code service} if {@code started} with {@params}.
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

    @Override
    public void startService(Map<String, String> options, String... serviceIds) throws ExecutionException {
        startServers(options);
        serviceIds = pruneServers(this, serviceIds);
        checkServiceInstances(serviceIds);
        serviceIds = topLevel(this, serviceIds); // avoid accidentally accessing family members
        handleOptions(options, serviceIds);
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
                AppDeploymentRequest req = service.createDeploymentRequest(config, externalServiceArgs);
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
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, step, serviceIds.length + 1, "Stopping service " + ids);
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
            Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, step++, serviceIds.length + 1, "Stopped service " + ids);
            netClient = markServerUse(true, service, false, netClient);
        }
        checkErrors(errors);
        LOGGER.info("Stopped services " + Arrays.toString(serviceIds));
        stopServers();
    }

    @Override
    public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        super.migrateService(serviceId, resourceId);
        throw new ExecutionException("not implemented", null);  // TODO must change host value in AAS!
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        LOGGER.info("Removing artifact " + artifactId);
        Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 0, 1, "Removing artifact " + artifactId);
        checkId(artifactId, "artifactId");
        SpringCloudArtifactDescriptor desc = getArtifact(artifactId);
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
        LOGGER.info("Removed artifact " + artifactId);
        Transport.sendProcessStatus(PROGRESS_COMPONENT_ID, 1, 1, "Removied artifact " + artifactId);
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

    /**
     * Calls {@link #setState(ServiceDescriptor, ServiceState)} logging exceptions.
     * 
     * @param service the service to change
     * @param state the new state
     */
    protected void setStateSafe(ServiceDescriptor service, ServiceState state) {
        try {
            setState(service, state);
        } catch (ExecutionException e) {
            LOGGER.warn("While setting service {} state: {}", service.getId(), e.getMessage());
        }
    }

    @Override
    protected void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        ServiceState old = service.getState();
        // must be done before setState (via stub), synchronous for now required on Jenkins/Linux
        ServicesAas.notifyServiceStateChanged(old, state, service, NotificationMode.SYNCHRONOUS); 
        service.setState(state);
        // if service made an implicit transition, take up and notify
        ServiceState further = service.getState();
        if (further != state) {
            ServicesAas.notifyServiceStateChanged(state, further, service, NotificationMode.SYNCHRONOUS); 
        }
    }

    @Override
    public void clear() {
        AppDeployer deployer = getDeployer();
        for (SpringCloudServiceDescriptor desc: getServices()) {
            String deploymentId = desc.getDeploymentId();
            AppStatus status = deployer.status(deploymentId);
            if (DeploymentState.deployed == status.getState()) {
                getDeployer().undeploy(deploymentId);
            }
        }
        super.clear();
    }

}
