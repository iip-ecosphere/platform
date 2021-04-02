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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation.Direction;
import de.iip_ecosphere.platform.services.spring.descriptor.Service;
import de.iip_ecosphere.platform.services.spring.descriptor.TypeResolver;
import de.iip_ecosphere.platform.services.spring.descriptor.TypedData;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * Specific descriptor implementation for spring cloud streams. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceDescriptor extends AbstractServiceDescriptor<SpringCloudArtifactDescriptor> {
    
    private Service service;
    private SpringCloudServiceDescriptor ensembleLeader;
    private String deploymentId;
    
    /**
     * Creates an instance.
     * 
     * @param service the service deployment specification object
     * @param ensembleLeader optional ensemble leader some information shall be taken from/synchronized with
     * @param resolver the (artifact) type resolver
     * @see #setClassification(ServiceKind, boolean)
     */
    public SpringCloudServiceDescriptor(Service service, SpringCloudServiceDescriptor ensembleLeader, 
        TypeResolver resolver) {
        super(service.getId(), service.getName(), service.getDescription(), new Version(service.getVersion()));
        setClassification(service.getKind(), service.isDeployable());
        this.service = service;
        this.ensembleLeader = ensembleLeader;
        
        for (TypedData p : service.getParameters()) {
            addParameter(new SpringCloudServiceTypedData(p.getName(), p.getDescription(), 
                resolver.resolve(p.getType())));
        }
        for (Relation r : service.getRelations()) {
            if (Direction.IN == r.getDirection()) {
                addInputDataConnector(new SpringCloudServiceTypedConnectorData(r.getChannel(), 
                    r.getDescription(), resolver.resolve(r.getType())));
            } else if (Direction.OUT == r.getDirection()) {
                addOutputDataConnector(new SpringCloudServiceTypedConnectorData(r.getChannel(), 
                    r.getDescription(), resolver.resolve(r.getType())));
            }
        }
    }
    
    /**
     * Returns the deployment group.
     * 
     * @return the deployment group
     */
    public String getGroup() {
        return getArtifact().getId(); // for now, just the artifact ID
    }

    /**
     * Returns the ensemble leader service, i.e., if multiple services are packaged together and shall be executed
     * in the same process, it is important to synchronize aspects (via the ensemble leader service).
     * 
     * @return the ensemble leader, may be <b>null</b> if there is none
     */
    SpringCloudServiceDescriptor getEnsembleLeader() {
        return ensembleLeader;
    }
    
    /**
     * Creates the deployment request for the Spring deployer.
     * 
     * @param config the service manager configuration instance
     * @return the deployment request, may be <b>null</b> if this service is an ensemble follower and should not be 
     * started individually
     */
    AppDeploymentRequest createDeploymentRequest(SpringCloudServiceConfiguration config) {
        AppDeploymentRequest result = null;
        if (null == ensembleLeader) {
            NetworkManager mgr = NetworkManagerFactory.getInstance();
            Map<String, String> appProps = new HashMap<String, String>();
            AppDefinition def = new AppDefinition(getId(), appProps);

            Map<String, String> deployProps = new HashMap<String, String>();
            Resource res = new FileSystemResource(getArtifact().getJar());
            deployProps.put(AppDeployer.GROUP_PROPERTY_KEY, getGroup());
            Utils.addPropertyIfPositiveToInt(deployProps, AppDeployer.COUNT_PROPERTY_KEY, service.getInstances(),  "1");
            deployProps.put(AppDeployer.INDEXED_PROPERTY_KEY, "false"); // index the instances?
            Utils.addPropertyIfPositiveToMeBi(deployProps, AppDeployer.MEMORY_PROPERTY_KEY, service.getMemory(), null);
            Utils.addPropertyIfPositiveToMeBi(deployProps, AppDeployer.DISK_PROPERTY_KEY, service.getDisk(), null);
            Utils.addPropertyIfPositiveToInt(deployProps, AppDeployer.CPU_PROPERTY_KEY, service.getCpus(), "1");

            List<String> cmdLine = new ArrayList<String>();
            cmdLine.addAll(service.getCmdArg());
            for (Relation r : service.getRelations()) {
                Endpoint endpoint = r.getEndpoint();
                if (r.getChannel().length() == 0) {
                    addEndpointArgs(cmdLine, endpoint, config.getBrokerPort(), config.getBrokerHost());
                } else {
                    ManagedServerAddress adr = mgr.obtainPort(r.getChannel());
                    addEndpointArgs(cmdLine, endpoint, adr);
                }
            }
            de.iip_ecosphere.platform.services.spring.descriptor.Process proc = service.getProcess();
            if (null != proc) {
                // TODO consider isGeneric
                ManagedServerAddress adr = mgr.obtainPort(getStreamingNetmanagerKey()); // TODO release on stop
                addEndpointArgs(cmdLine, proc.getServiceStreamEndpoint(), adr);

                List<String> procCmdLine = new ArrayList<String>();
                procCmdLine.addAll(proc.getCmdArg());
                addEndpointArgs(cmdLine, proc.getStreamEndpoint(), adr);

                ManagedServerAddress adrAas = mgr.obtainPort(getAasNetmanagerKey()); // TODO release on stop??
                addEndpointArgs(cmdLine, proc.getAasEndpoint(), adrAas);

                // TODO start process before, consider started
                // TODO store processid in descriptor
                // TODO consider for stopping
            }
            result = new AppDeploymentRequest(def, res, deployProps, cmdLine);
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
        if (null != endpoint) { // endpoints may be optional
            cmdLine.add(endpoint.getPortArg(port));
            if (endpoint.getHostArg().length() > 0) {
                cmdLine.add(endpoint.getHostArg(host));
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

}
