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

package test.de.iip_ecosphere.platform.services;

import java.util.List;

import de.iip_ecosphere.platform.services.AbstractArtifactDescriptor;

/**
 * A test artifact descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MyArtifactDescriptor extends AbstractArtifactDescriptor<MyServiceDescriptor> {

    /**
     * Creates an artifact descriptor.
     * 
     * @param id the artifact id
     * @param name the (file) name
     * @param services the contained services
     */
    MyArtifactDescriptor(String id, String name, List<MyServiceDescriptor> services) {
        super(id, name, services);
    }

}
