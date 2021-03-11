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
import java.util.List;

import de.iip_ecosphere.platform.services.AbstractArtifactDescriptor;

/**
 * A specific artifact descriptor for spring cloud services.
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

}
