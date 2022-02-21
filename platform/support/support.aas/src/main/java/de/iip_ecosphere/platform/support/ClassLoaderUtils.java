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

package de.iip_ecosphere.platform.support;

/**
 * Class loader helpers.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassLoaderUtils {

    /**
     * Turns the class loader hierarchy to a descriptive string. [debugging]
     * 
     * @param loader the loader to start with
     * @return the descriptive string
     */
    public static String hierarchyToString(ClassLoader loader) {
        String loaders = "";
        ClassLoader l = loader;
        while (null != l) {
            if (loaders.length() > 0) {
                loaders += " -> ";
            }
            loaders += l.getClass().getSimpleName();
            l = l.getParent();
        }
        return loaders;
    }
    
}
