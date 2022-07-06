/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.spring;

import java.io.InputStream;

import de.iip_ecosphere.platform.support.resources.ResourceResolver;

/**
 * Implements a Spring resource loader for "BOOT-INF/classes" (Spring FAT Jar).
 *  
 * @author Holger Eichelberger, SSE
 */
public class SpringResourceResolver implements ResourceResolver {

    @Override
    public String getName() {
        return "Spring BOOT-INF/classes resolver";
    }
    
    @Override
    public InputStream resolve(ClassLoader loader, String resource) {
        return loader.getResourceAsStream("BOOT-INF/classes/" + resource);
    }

}
