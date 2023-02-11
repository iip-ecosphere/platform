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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.iip_ecosphere.platform.services.AbstractArtifactDescriptor;
import de.iip_ecosphere.platform.services.spring.descriptor.Server;
import de.iip_ecosphere.platform.services.spring.descriptor.TypeResolver;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.services.spring.yaml.YamlService;

/**
 * A specific artifact descriptor for spring cloud services. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudArtifactDescriptor extends AbstractArtifactDescriptor<SpringCloudServiceDescriptor> {

    private File jar;
    
    /**
     * Creates an artifact descriptor.
     * 
     * @param artifact the Yaml artifact
     * @param uri the URI the descriptor was loaded from ({@code jar} may be a local file instead)
     * @param jar the underlying jar artifact 
     * @param services the associated services
     */
    SpringCloudArtifactDescriptor(YamlArtifact artifact, URI uri, File jar, 
        List<SpringCloudServiceDescriptor> services) {
        super(artifact.getId(), artifact.getName(), uri, services, createServersList(artifact));
        this.jar = jar;
    }
    
    /**
     * Creates a list of service descriptors for the servers in {@code artifact}.
     * 
     * @param artifact the artifact
     * @return the service descriptors
     */
    private static List<SpringCloudServiceDescriptor> createServersList(YamlArtifact artifact) {
        ArrayList<SpringCloudServiceDescriptor> result = new ArrayList<>();
        for (Server s : artifact.getServers()) {
            result.add(SpringCloudServiceDescriptor.createFor(s));
        }
        return result;
    }
    
    /**
     * Returns the underlying JAR file.
     * 
     * @return the jar file
     */
    public File getJar() {
        return jar;
    }
    
    /**
     * Creates a descriptor instance for a given YAML {@code artifact} and containing {@code jarFile}.
     * 
     * @param artifact the artifact parsed from a YAML descriptor
     * @param uri the URI the {@code jarFile} was loaded from ({@code jarFile} may be a local file)
     * @param jarFile the JAR file containing the artifact
     * @return the spring-specific descriptor
     */
    public static SpringCloudArtifactDescriptor createInstance(YamlArtifact artifact, URI uri, File jarFile) {
        List<SpringCloudServiceDescriptor> services = new ArrayList<>();
        
        TypeResolver resolver = new TypeResolver(artifact.getTypes());
        Map<String, SpringCloudServiceDescriptor> descriptors = new HashMap<String, SpringCloudServiceDescriptor>();
        for (YamlService s : artifact.getServices()) {
            SpringCloudServiceDescriptor desc = new SpringCloudServiceDescriptor(s, resolver);
            descriptors.put(desc.getId(), desc);
            services.add(desc);
        }
        
        for (YamlService s : artifact.getServices()) {
            if (null != s.getEnsembleWith()) {
                SpringCloudServiceDescriptor service = descriptors.get(s.getId());
                SpringCloudServiceDescriptor ensembleLeader = descriptors.get(s.getEnsembleWith());
                if (null != ensembleLeader && null != service) { // must be local and defined
                    service.setEnsembleLeader(ensembleLeader);
                }
            }
        }
        
        return new SpringCloudArtifactDescriptor(artifact, uri, jarFile, services);        
    }
    
    // for package access
    @Override
    protected void addService(SpringCloudServiceDescriptor service) {
        super.addService(service);
    }
    
}
