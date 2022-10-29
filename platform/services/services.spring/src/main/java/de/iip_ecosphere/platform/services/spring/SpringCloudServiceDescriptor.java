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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.AbstractServiceManager.TypedDataConnection;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.ProcessSpec;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation.Direction;
import de.iip_ecosphere.platform.services.spring.descriptor.Service;
import de.iip_ecosphere.platform.services.spring.descriptor.TypeResolver;
import de.iip_ecosphere.platform.services.spring.descriptor.TypedData;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * Specific descriptor implementation for spring cloud streams. [public for testing]
 * Relies on parameter functions from {@link Starter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceDescriptor extends AbstractServiceDescriptor<SpringCloudArtifactDescriptor> {
    
    private Service service;
    private SpringCloudServiceDescriptor ensembleLeader;
    private String deploymentId;
    private List<String> portKeys = new ArrayList<String>();
    private Process process;
    private File processDir;
    private String serviceProtocol;
    private ManagedServerAddress adminAddr;
    
    /**
     * Creates an instance.
     * 
     * @param service the service deployment specification object
     * @param resolver the (artifact) type resolver
     * @see #setClassification(de.iip_ecosphere.platform.services.environment.ServiceKind, boolean, boolean)
     */
    public SpringCloudServiceDescriptor(Service service, TypeResolver resolver) {
        super(service.getId(), service.getApplicationId(), service.getName(), service.getDescription(), 
            service.getVersion());
        setClassification(service.getKind(), service.isDeployable(), service.isTopLevel());
        this.service = service;
        
        for (TypedData p : service.getParameters()) {
            addParameter(new SpringCloudServiceTypedData(p.getName(), p.getDescription(), 
                resolver.resolve(p.getType())));
        }
        for (Relation r : service.getRelations()) {
            if (Direction.IN == r.getDirection()) {
                addInputDataConnector(new SpringCloudServiceTypedConnectorData(r.getId(), r.getChannel(), 
                    r.getDescription(), resolver.resolve(r.getType()), r.getService(), r.getFunction()));
            } else if (Direction.OUT == r.getDirection()) {
                addOutputDataConnector(new SpringCloudServiceTypedConnectorData(r.getId(), r.getChannel(), 
                    r.getDescription(), resolver.resolve(r.getType()), r.getService(), r.getFunction()));
            }
        }
    }
    
    /**
     * Creates an instance. Call {@link #setClassification(ServiceKind, boolean, boolean)} afterwards.
     * 
     * @param id the service id
     * @param applicationId the application id, may be empty for default application/legacy
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    protected SpringCloudServiceDescriptor(String id, String applicationId, String name, String description, 
        Version version) {
        super(id, applicationId, name, description, version);
    }
    
    /**
     * Instantiates this service as a template to represent an instance service with id {@code serviceId}.
     * Typically, the application instance id changes compared to existing service descriptors.
     * 
     * @param sId the service id
     * @return the instantiated service
     */
    SpringCloudServiceDescriptor instantiate(String sId) {
        SpringCloudServiceDescriptor result = new SpringCloudServiceDescriptor(sId, getApplicationId(), 
            getName(), getDescription(), getVersion());
        result.instantiateFrom(this);
        result.service = this.service;
        if (null != this.ensembleLeader) {
            String ensId = this.ensembleLeader.getServiceId();
            String appInstanceId = ServiceBase.getApplicationInstanceId(sId);
            String appId = ServiceBase.getApplicationId(sId);
            ensId = ServiceBase.composeId(ensId, appId, appInstanceId);
            result.ensembleLeader = this.getArtifact().getService(ensId);
            if (null == result.ensembleLeader) {
                result = this.ensembleLeader.instantiate(ensId);
                this.getArtifact().addService(result);
            }
        }
        //deploymentId, portId, process, processDir, serviceProtocol, adminAddr come at runtime
        return result;
    }

    @Override
    protected Class<SpringCloudArtifactDescriptor> getArtifactDescriptorClass() {
        return SpringCloudArtifactDescriptor.class;
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        super.setState(state);
        if (ServiceState.STOPPING == state) {
            release();
        }
    }
    
    /**
     * Releases the {@link #process} as well as the ports in {@link #portKeys}.
     */
    private void release() {
        if (null != process) {
            process.destroy();
            process = null;
        }
        if (null != processDir) {
            FileUtils.deleteQuietly(processDir);
        }
        NetworkManager mgr = NetworkManagerFactory.getInstance();
        for (String key : portKeys) {
            mgr.releasePort(key);
        }
        portKeys.clear();
        adminAddr = null;
    }

    /**
     * Obtains a network port and registers it if necessary for release in {@link #setState(ServiceState)}.
     * 
     * @param mgr the network manager instance
     * @param key the key to obtain the network address
     * @return the obtained address
     */
    private ManagedServerAddress registerPort(NetworkManager mgr, String key) {
        ManagedServerAddress result = mgr.obtainPort(key);
        if (result.isNew()) {
            portKeys.add(key);
        }
        return result;
    }
    
    /**
     * Defines the ensemble leader.
     * 
     * @param ensembleLeader optional ensemble leader some information shall be taken from/synchronized with
     */
    void setEnsembleLeader(SpringCloudServiceDescriptor ensembleLeader) {
        this.ensembleLeader = ensembleLeader;
    }
    
    /**
     * Returns the deployment group.
     * 
     * @return the deployment group
     */
    public String getGroup() {
        return getArtifact().getId(); // for now, just the artifact ID
    }

    @Override
    public SpringCloudServiceDescriptor getEnsembleLeader() {
        return ensembleLeader;
    }
    
    /**
     * Creates the deployment request for the Spring deployer.
     * 
     * @param config the service manager configuration instance
     * @param cmdArgs further command line arguments to be considered, may be <b>null</b> or empty for none
     * @return the deployment request, may be <b>null</b> if this service is an ensemble follower and should not be 
     * started individually
     * @throws ExecutionException when preparing the service fails for some reason
     */
    AppDeploymentRequest createDeploymentRequest(SpringCloudServiceSetup config, List<String> cmdArgs) 
        throws ExecutionException {
        AppDeploymentRequest result = null;
        if (null == ensembleLeader) {
            NetworkManager mgr = NetworkManagerFactory.getInstance();
            Map<String, String> appProps = new HashMap<String, String>();

            Map<String, String> deployProps = new HashMap<String, String>();
            Resource res = new FileSystemResource(getArtifact().getJar());
            deployProps.put(AppDeployer.GROUP_PROPERTY_KEY, getGroup());
            //deployProps.put("spring.cloud.deployer.local.deleteFilesOnExit ", "false"); // does not work
            Utils.addPropertyIfPositiveToInt(deployProps, AppDeployer.COUNT_PROPERTY_KEY, service.getInstances(),  "1");
            deployProps.put(AppDeployer.INDEXED_PROPERTY_KEY, "false"); // index the instances?
            Utils.addPropertyIfPositiveToMeBi(deployProps, AppDeployer.MEMORY_PROPERTY_KEY, service.getMemory(), null);
            Utils.addPropertyIfPositiveToMeBi(deployProps, AppDeployer.DISK_PROPERTY_KEY, service.getDisk(), null);
            Utils.addPropertyIfPositiveToInt(deployProps, AppDeployer.CPU_PROPERTY_KEY, service.getCpus(), "1");

            List<String> cmdLine = new ArrayList<String>();
            if (null != config.getJavaOpts()) {
                cmdLine.addAll(config.getJavaOpts());
            }
            ManagedServerAddress springAddr = registerPort(mgr, "spring_" + getId());
            appProps.put("server.port", String.valueOf(springAddr.getPort())); // shall work, not another cmd arg
            adminAddr = registerPort(mgr, Starter.getServiceCommandNetworkMgrKey(getId()));
            serviceProtocol = config.getServiceProtocol();
            cmdLine.addAll(service.getCmdArg(adminAddr.getPort(), serviceProtocol));
            for (Relation r : service.getRelations()) {
                Endpoint endpoint = r.getEndpoint();
                if (r.getChannel().length() == 0) {
                    DescriptorUtils.addEndpointArgs(cmdLine, endpoint, getTransportPort(config), 
                        getTransportHost(config));
                } else {
                    ManagedServerAddress adr = registerPort(mgr, r.getChannel());
                    DescriptorUtils.addEndpointArgs(cmdLine, endpoint, adr);
                }
            }
            ProcessSpec pSpec = service.getProcess();
            if (null != pSpec) {
                ManagedServerAddress adr = registerPort(mgr, getStreamingNetmanagerKey());
                DescriptorUtils.addEndpointArgs(cmdLine, pSpec.getServiceStreamEndpoint(), adr);

                List<String> procCmdLine = new ArrayList<String>();
                procCmdLine.addAll(pSpec.getCmdArg());
                DescriptorUtils.addEndpointArgs(cmdLine, pSpec.getStreamEndpoint(), adr);

                ManagedServerAddress adrAas = registerPort(mgr, getAasNetmanagerKey());
                DescriptorUtils.addEndpointArgs(cmdLine, pSpec.getAasEndpoint(), adrAas);
                
                int procPort = startProcess(config, pSpec);
                if (procPort > 0) {
                    cmdLine.add(Starter.composeArgument(Starter.getServicePortName(getId()), procPort));
                }
            }
            if (null != cmdArgs) {
                cmdLine.addAll(cmdArgs);
            }
            Starter.addAppEnvironment(cmdLine);
            // if cmdLine becomes too long, check whether a Yaml file/stream could be a solution 
            AppDefinition def = new AppDefinition(getId(), appProps);
            result = new AppDeploymentRequest(def, res, deployProps, cmdLine);
        }
        return result;
    }
    
    /**
     * Returns the actual transport host. [legacy fallback, due to testing]
     * 
     * @param setup the instance to take the information from
     * @return the host
     */
    private String getTransportHost(SpringCloudServiceSetup setup) {
        String result;
        if (setup.getTransport().getHost() == null) {
            result = setup.getBrokerHost();
        } else {
            result = setup.getTransport().getHost();
        }
        return result;
    }

    /**
     * Returns the actual transport port. [legacy fallback, due to testing]
     * 
     * @param setup the instance to take the information from
     * @return the port
     */
    private int getTransportPort(SpringCloudServiceSetup setup) {
        int result;
        if (setup.getTransport().getHost() == null) { // yes, check host
            result = setup.getBrokerPort();
        } else {
            result = setup.getTransport().getPort();
        }
        return result;
    }

    /**
     * Attaches a service stub to directly interact with the service if {@link #adminAddr} has been set by 
     * {@link #createDeploymentRequest(SpringCloudServiceSetup, List)} before.
     */
    void attachStub() {
        InvocablesCreator iCreator = getInvocablesCreator();
        if (null != iCreator) {
            setStub(new ServiceStub(iCreator, getId()));
        }
    }
    
    /**
     * Waits that the server on {@link #adminAddr} becomes available.
     *   
     * @param waitingTime maximum waiting time in ms
     */
    void waitForAdminServer(int waitingTime) {
        if (null != adminAddr) {
            ProtocolServerBuilder psb = AasFactory.getInstance()
                .createProtocolServerBuilder(serviceProtocol, adminAddr.getPort());
            psb.isAvailable(adminAddr.getHost(), waitingTime);
        }
    }
    
    /**
     * Detaches an attached service stub.
     */
    void detachStub() {
        // force detach on instance?
        setStub(null);
    }

    @Override
    public InvocablesCreator getInvocablesCreator() {
        InvocablesCreator iCreator = null;
        ManagedServerAddress addr;
        String proto;
        SpringCloudServiceDescriptor leader = getEnsembleLeader();
        if (null != leader) {
            addr = leader.adminAddr;
            proto = leader.serviceProtocol;
        } else {
            addr = adminAddr;
            proto = serviceProtocol;
        }
        if (null != addr) {
            iCreator = AasFactory.getInstance().createInvocablesCreator(proto, 
                addr.getHost(), addr.getPort());
        }
        return iCreator;
    }
    
    /**
     * Starts the non-Java execution process if needed.
     * 
     * @param config the service manager configuration instance
     * @param pSpec the specification of the service to start
     * @return the process port, valid if positive
     * @throws ExecutionException when preparing the service fails for some reason
     */
    int startProcess(SpringCloudServiceSetup config, ProcessSpec pSpec) throws ExecutionException {
        int result = -1;
        try {
            processDir = DescriptorUtils.extractProcessArtifacts(getId(), pSpec, getArtifact().getJar(), 
                SpringInstances.getConfig().getDownloadDir());

            if (!pSpec.isStarted()) {
                // compose start arguments and start service implementation
                NetworkManager mgr = NetworkManagerFactory.getInstance();
                String serviceProtocol = config.getServiceProtocol();
                ManagedServerAddress procAdr = registerPort(mgr, Starter.getServiceProcessNetworkMgrKey(getId()));
                List<String> args = new ArrayList<String>();
                args.add(config.getExecutable(pSpec.getExecutable()));
                args.addAll(pSpec.getCmdArg(procAdr.getPort(), serviceProtocol));
                ProcessBuilder processBuilder = new ProcessBuilder(args);
                processBuilder.directory(processDir);
                processBuilder.inheritIO();
                process = processBuilder.start();
                if (pSpec.getWaitTime() > 0) {
                    TimeUtils.sleep(pSpec.getWaitTime());
                }
                result = procAdr.getPort();
            }
        } catch (IOException e) {
            release();
            throw new ExecutionException(e);
        }
        return result;
    }

    /**
     * Returns the {@link NetworkManager} key for the streaming connection to an external process.
     * 
     * @return the key
     */
    private String getStreamingNetmanagerKey() {
        return getId() + "_stream";
    }
    
    /**
     * Returns the {@link NetworkManager} key for the AAS command connection to an external process.
     * 
     * @return the key
     */
    String getAasNetmanagerKey() {
        return getId() + "_aas"; // preliminary
    }
    
    /**
     * Sets the Spring cloud deployer deployment id.
     * 
     * @param id the deployment id
     */
    void setDeploymentId(String id) {
        this.deploymentId = id;
    }

    /**
     * Returns the Spring cloud deployer deployment id.
     * 
     * @return the deployment id, may be <b>null</b> for not deployed
     */
    String getDeploymentId() {
        return deploymentId;
    }
    
    /**
     * Returns the YAML descriptor. [for testing]
     * 
     * @return the YAML descriptor
     */
    public Service getSvc() {
        return service;
    }

    /**
     * Turns typed data connections into a Spring cloud function definition argument.
     * 
     * @param conn the connections
     * @return the composed function definition argument
     */
    public static String toFunctionDefinition(Set<TypedDataConnection> conn) {
        return conn.stream()
            .filter(c -> c.getFunction() != null && c.getFunction().length() > 0)
            .map(c -> c.getFunction()) 
            .distinct() // make entries unique
            .collect(Collectors.joining(";"));        
    }
    
}
