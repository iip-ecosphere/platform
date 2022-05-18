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

package de.iip_ecosphere.platform.services.environment;

import java.io.InputStream;

/**
 * Support for class loading also in fat jars.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResourceLoader {

    /**
     * Returns a resource as string taking the class loader of this class.
     * 
     * @param name the name of the resource to load
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(String name) {
        return getResourceAsStream(ResourceLoader.class, name);
    }

    /**
     * Returns a resource as string.
     * 
     * @param cls the class to take the class loader from 
     * @param name the name of the resource to load
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(Class<?> cls, String name) {
        return getResourceAsStream(cls.getClassLoader(), name);
    }

    /**
     * Returns a resource as string.
     * 
     * @param loader the class loader to use
     * @param name the name of the resource to load (shall not start with "/", tested as fallback alternative)
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String name) {
        InputStream result = loader.getResourceAsStream(name);
        if (null == result && !name.startsWith("/")) {
            result = loader.getResourceAsStream("/" + name);
        }
        // TODO fixed integration of alternative spring...
        if (null == result) {
            while (name.startsWith("/")) {
                name = name.substring(1);
            }
            result = loader.getResourceAsStream("BOOT-INF/classes/" + name);
            if (null == result) {
                result = loader.getResourceAsStream("/BOOT-INF/classes/" + name);
            }
        }
        return result;
    }

}
