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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.AbstractServiceManager.TypedDataConnection;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.ProcessSpec;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation.Direction;
import de.iip_ecosphere.platform.services.spring.descriptor.Service;
import de.iip_ecosphere.platform.services.spring.descriptor.TypeResolver;
import de.iip_ecosphere.platform.services.spring.descriptor.TypedData;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
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
     * @see #setClassification(ServiceKind, boolean)
     */
    public SpringCloudServiceDescriptor(Service service, TypeResolver resolver) {
        super(service.getId(), service.getName(), service.getDescription(), service.getVersion());
        setClassification(service.getKind(), service.isDeployable());
        this.service = service;
        
        for (TypedData p : service.getParameters()) {
            addParameter(new SpringCloudServiceTypedData(p.getName(), p.getDescription(), 
                resolver.resolve(p.getType())));
        }
        for (Relation r : service.getRelations()) {
            if (Direction.IN == r.getDirection()) {
                addInputDataConnector(new SpringCloudServiceTypedConnectorData(r.getId(), r.getChannel(), 
                    r.getDescription(), resolver.resolve(r.getType()), r.getService()));
            } else if (Direction.OUT == r.getDirection()) {
                addOutputDataConnector(new SpringCloudServiceTypedConnectorData(r.getId(), r.getChannel(), 
                    r.getDescription(), resolver.resolve(r.getType()), r.getService()));
            }
        }
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
            AppDefinition def = new AppDefinition(getId(), appProps);

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
            adminAddr = registerPort(mgr, Starter.getServiceCommandNetworkMgrKey(getId()));
            serviceProtocol = config.getServiceProtocol();
            cmdLine.addAll(service.getCmdArg(adminAddr.getPort(), serviceProtocol));
            for (Relation r : service.getRelations()) {
                Endpoint endpoint = r.getEndpoint();
                if (r.getChannel().length() == 0) {
                    addEndpointArgs(cmdLine, endpoint, config.getBrokerPort(), config.getBrokerHost());
                } else {
                    ManagedServerAddress adr = registerPort(mgr, r.getChannel());
                    addEndpointArgs(cmdLine, endpoint, adr);
                }
            }
            ProcessSpec pSpec = service.getProcess();
            if (null != pSpec) {
                ManagedServerAddress adr = registerPort(mgr, getStreamingNetmanagerKey());
                addEndpointArgs(cmdLine, pSpec.getServiceStreamEndpoint(), adr);

                List<String> procCmdLine = new ArrayList<String>();
                procCmdLine.addAll(pSpec.getCmdArg());
                addEndpointArgs(cmdLine, pSpec.getStreamEndpoint(), adr);

                ManagedServerAddress adrAas = registerPort(mgr, getAasNetmanagerKey());
                addEndpointArgs(cmdLine, pSpec.getAasEndpoint(), adrAas);
                
                int procPort = startProcess(config, pSpec);
                if (procPort > 0) {
                    cmdLine.add(Starter.composeArgument(Starter.getServicePortName(getId()), procPort));
                }
            }
            if (null != cmdArgs) {
                cmdLine.addAll(cmdArgs);
            }
            // if cmdLine becomes too long, check whether a Yaml file/stream could be a solution 
            getLogger().info("Creates deployment request for " + getName() + " " + cmdLine);
            result = new AppDeploymentRequest(def, res, deployProps, cmdLine);
        }
        return result;
    }
    
    /**
     * Attaches a service stub to directly interact with the service if {@link #adminAddr} has been set by 
     * {@link #createDeploymentRequest(SpringCloudServiceSetup)} before.
     */
    void attachStub() {
        InvocablesCreator iCreator = getInvocablesCreator();
        if (null != iCreator) {
            setStub(new ServiceStub(iCreator, getId()));
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
    private int startProcess(SpringCloudServiceSetup config, ProcessSpec pSpec) throws ExecutionException {
        int result = -1;
        try {
            // take over / create process home dir
            processDir = pSpec.getHomePath();
            if (null == processDir) {
                processDir = new File(SpringInstances.getConfig().getDownloadDir(), 
                    Starter.normalizeServiceId(getId()) + "-" + System.currentTimeMillis());
            }
            if (!pSpec.isStarted()) {
                FileUtils.deleteQuietly(processDir); // unlikely, just to be sure
            }
            processDir.mkdirs();

            // unpack artifacts to home
            for (String artPath : pSpec.getArtifacts()) {
                if (!artPath.startsWith("/")) {
                    artPath = "/" + artPath;
                }
                FileInputStream fis = null;
                InputStream artifact = SpringCloudServiceDescriptor.class.getResourceAsStream(artPath);
                if (null == artifact) { // spring packaging fallback
                    try {
                        fis = new FileInputStream(getArtifact().getJar());
                        artifact = JarUtils.findFile(fis, "BOOT-INF/classes" + artPath);
                    } catch (IOException e) {
                        getLogger().info("Cannot open " + getArtifact().getJar() + ": " + e.getMessage());
                    }
                }
                if (null == artifact) {
                    throw new IOException("Cannot find artifact '" + artPath + "' in actual service JAR");
                }
                JarUtils.extractZip(artifact, processDir.toPath());
                getLogger().info("Extracted process artifact " + artPath + " to " + processDir);
                FileUtils.closeQuietly(artifact);
                FileUtils.closeQuietly(fis);
            }

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
     * Adds commandline args for a given {@code endpoint}.
     * 
     * @param cmdLine the command line arguments to modify as a side effect
     * @param endpoint the endpoint to turn into command line arguments
     * @param addr the address containing port number and host (for substitution in results delivered 
     *     by {@code endpoint})
     */
    private void addEndpointArgs(List<String> cmdLine, Endpoint endpoint, ServerAddress addr) {
        addEndpointArgs(cmdLine, endpoint, addr.getPort(), addr.getHost());
    }

    /**
     * Adds commandline args for a given {@code endpoint}.
     * 
     * @param cmdLine the command line arguments to modify as a side effect
     * @param endpoint the endpoint to turn into command line arguments
     * @param port the port number (for substitution in results delivered by {@code endpoint})
     * @param host the host name (for substitution in results delivered by {@code endpoint})
     */
    private void addEndpointArgs(List<String> cmdLine, Endpoint endpoint, int port, String host) {
        if (null != endpoint) { // endpoints are optional
            CmdLine.parseToArgs(endpoint.getPortArg(port), cmdLine);
            if (endpoint.getHostArg().length() > 0) {
                CmdLine.parseToArgs(endpoint.getHostArg(host), cmdLine);
            }
        }
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
     * Turns a channel name to a function name.
     * 
     * @param channel the channel
     * @return the function name
     */
    public static String channelToFunction(String channel) {
        String result = channel;
        int pos = channel.lastIndexOf('-');
        if (pos > 0) {
            pos = channel.lastIndexOf('-', pos - 1);
            if (pos > 0) {
                result = result.substring(0, pos);
            }
        }
        return result;
    }

    /**
     * Turns typed data connections into a Spring cloud function definition argument.
     * 
     * @param conn the connections
     * @return the composed function definition argument
     */
    public static String toFunctionDefinition(Set<TypedDataConnection> conn) {
        return conn.stream()
            .map(c -> channelToFunction(c.getName()))
            .distinct()
            .collect(Collectors.joining(";"));        
    }
    
    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private Logger getLogger() {
        return LoggerFactory.getLogger(SpringCloudServiceDescriptor.class);
    }
    
}
