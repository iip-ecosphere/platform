/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to YAML. Requires an implementing plugin of type {@link Yaml} or an active 
 * {@link YamlProviderDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Yaml {
    
    private static Yaml instance; 

    static {
        instance = PluginManager.getPluginInstance(Yaml.class, YamlProviderDescriptor.class);
    }
    
    /**
     * Returns the YAML instance.
     * 
     * @return the instance
     */
    public static Yaml getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param yaml the YAML instance
     */
    public static void setInstance(Yaml yaml) {
        if (null != yaml) {
            instance = yaml;
        }
    }
    
    /**
     * Loads a plain object from an input stream.
     * 
     * @param in the input stream to load from
     * @return the mapping
     * @throws IOException if accessing the input stream fails, if reading the structure fails
     */
    public abstract Object load(InputStream in) throws IOException;

    /**
     * Loads a plain name-object mapping from an input stream.
     * 
     * @param in the input stream to load from
     * @return the mapping
     * @throws IOException if accessing the input stream fails, if reading the structure fails
     */
    public abstract Map<String, Object> loadMapping(InputStream in) throws IOException;
    
    /**
     * Loads an object from YAML given as string expecting all properties.
     * 
     * @param <T> the result type
     * @param in the YAML string
     * @param cls the class/type of the object to read
     * @return the object
     * @throws IOException if reading the structure/object fails
     */
    public abstract <T> T loadAs(String in, Class<T> cls) throws IOException;

    /**
     * Loads an object from YAML given as an input stream expecting all properties.
     * 
     * @param <T> the result type
     * @param in the input stream
     * @param cls the class/type of the object to read
     * @return the object
     * @throws IOException if reading the structure/object fails
     */
    public abstract <T> T loadAs(InputStream in, Class<T> cls) throws IOException;

    /**
     * Loads an object from YAML given as an input stream ignoring missing properties.
     * 
     * @param <T> the result type
     * @param in the input stream
     * @param cls the class/type of the object to read
     * @return the object
     * @throws IOException if reading the structure/object fails
     */
    public abstract <T> T loadTolerantAs(InputStream in, Class<T> cls) throws IOException;
    
    /**
     * Loads all documents from a YAML file, trying to apply {@code cls} as type.
     * 
     * @param in the input stream to load from
     * @param cls the class/type of the object to read; if given, only objects of that type will be returned
     * @return an iterator over all documents
     * @throws IOException if accessing the input stream fails, if reading the structure fails
     */
    public Iterator<Object> loadAll(InputStream in, Class<?> cls) throws IOException {
        return loadAll(in, null, cls);
    }

    /**
     * Loads all documents from a YAML file, trying to apply {@code cls} as type.
     * 
     * @param in the input stream to load from
     * @param path the path within YAML to load from, may be <b>null</b> or empty for none
     * @param cls the class/type of the object to read; if given, only objects of that type will be returned
     * @return an iterator over all documents
     * @throws IOException if accessing the input stream fails, if reading the structure fails
     */
    public abstract Iterator<Object> loadAll(InputStream in, String path, Class<?> cls) throws IOException;

    /**
     * Loads all documents from a YAML file.
     * 
     * @return an iterator over all documents
     * @throws IOException if accessing the input stream fails, if reading the structure fails
     */
    public abstract Iterator<Object> loadAll(InputStream in) throws IOException;

    /**
     * Dumps {@code object} in terms of {@code cls} to output.
     * 
     * @param object the data
     * @param cls the class to be used as schema for writing
     * @param out the output writer
     * @throws IOException if writing fails
     */
    public abstract void dump(Object object, Class<?> cls, Writer out) throws IOException;

    /**
     * Dumps {@code object} to a string.
     * 
     * @param object the data
     * @return the YAML representation of the data
     * @throws IOException if writing fails
     */
    public abstract String dump(Object object) throws IOException;
    
    /**
     * Dumps {@code object} in terms of its class  to output.
     * 
     * @param object the data
     * @param out the output writer
     * @throws IOException if writing fails
     */
    public void dump(Object object, Writer out) throws IOException {
        dump(object, object.getClass(), out);
    }

}
