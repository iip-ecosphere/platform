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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.iip_ecosphere.platform.services.AbstractArtifactDescriptor;
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
     * @param id the artifact id
     * @param name the (file) name
     * @param jar the underlying jar artifact 
     * @param services the associated services
     */
    SpringCloudArtifactDescriptor(String id, String name, File jar, 
        List<SpringCloudServiceDescriptor> services) {
        super(id, name, services);
        this.jar = jar;
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
     * @param jarFile the JAR file containing the artifact
     * @return the spring-specific descriptor
     */
    public static SpringCloudArtifactDescriptor createInstance(YamlArtifact artifact, File jarFile) {
        List<SpringCloudServiceDescriptor> services = new ArrayList<>();
        Map<String, String> ensembleLeaderIds = new HashMap<>();
        for (YamlService s : artifact.getServices()) {
            for (String id : s.getEnsembleWith()) {
                ensembleLeaderIds.put(id, s.getId());
            }
        }
        
        TypeResolver resolver = new TypeResolver(artifact.getTypes());
        Map<String, SpringCloudServiceDescriptor> descriptors = new HashMap<String, SpringCloudServiceDescriptor>();
        for (YamlService s : artifact.getServices()) {
            SpringCloudServiceDescriptor ensembleLeader = null;
            for (String ens: s.getEnsembleWith()) {
                ensembleLeader = descriptors.get(ens);
                if (null != ensembleLeader) {
                    break;
                }
            }
            
            SpringCloudServiceDescriptor desc = new SpringCloudServiceDescriptor(s, ensembleLeader, resolver);
            descriptors.put(desc.getId(), desc);
            services.add(desc);
        }
        return new SpringCloudArtifactDescriptor(artifact.getId(), artifact.getName(), jarFile, services);        
    }
    
}
