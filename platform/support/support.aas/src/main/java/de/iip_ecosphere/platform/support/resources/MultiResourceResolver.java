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

package de.iip_ecosphere.platform.support.resources;

import java.io.InputStream;
import java.util.List;

/**
 * A delegating multi resource resolver.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiResourceResolver implements ResourceResolver {

    private ResourceResolver[] resolvers;

    /**
     * Creates a resolver from individual resolvers or an array of resolvers.
     * 
     * @param resolvers the resolvers
     */
    public MultiResourceResolver(ResourceResolver... resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Creates a resolver from a list resolvers. [convenience]
     * 
     * @param resolvers the resolvers
     */
    public MultiResourceResolver(List<ResourceResolver> resolvers) {
        this(resolvers.toArray(new ResourceResolver[0]));
    }
    
    @Override
    public String getName() {
        String name = "Multi resource resolver: ";
        int count = 0;
        for (ResourceResolver r : resolvers) {
            if (count > 0) {
                name += ", ";
            }
            name += r.getName();
            count++;
        }
        return name;
    }
    
    @Override
    public InputStream resolve(ClassLoader loader, String resource) {
        InputStream result = null;
        for (int r = 0; null == result && r < resolvers.length; r++) {
            result = resolvers[r].resolve(loader, resource);
        }
        return result;
    }

}
