/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A priority (child first) class loader that takes its indexing from an index file.
 * 
 * @author ChatGPT
 * @author Holger Eichelberger, SSE
 */
public class ChildFirstIndexedClassloader extends ClassLoader {
    
    private final Map<String, String> locationIndex;
    private final Map<String, String> classIndex;
    private final Map<String, String> resourceIndex;
    private final Map<Path, JarFile> jarCache = new ConcurrentHashMap<>();

    static {
        registerAsParallelCapable();
    }
    
    /**
     * Represents the class loader index.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class LoaderIndex implements Serializable {
        
        private static final long serialVersionUID = -3350988607004003802L; // keep in sync with resource plugin

        private Map<String, String> locationIndex = new HashMap<>();
        private Map<String, String> classIndex = new HashMap<>();
        private Map<String, String> resourceIndex = new HashMap<>();

        /**
         * Reads an index file, basically via serialization.
         * 
         * @param indexFile the index file
         * @return the index object
         * @throws IOException if the file cannot be read
         */
        public static LoaderIndex fromFile(File indexFile) throws IOException {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(indexFile)))) {
                return (LoaderIndex) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        
        /**
         * Saves {@code index} to {@code file}.
         * 
         * @param index the index to write
         * @param file the file to write to
         * @throws IOException if the index cannot be written
         */
        public static void toFile(LoaderIndex index, File file) throws IOException {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)))) {
                oos.writeObject(index);
            }
        }
        
        /**
         * Adds the resource {@code name} in {@code location} in {@code index}. [testing]
         * 
         * @param index the index instance
         * @param isClass whether we shall register a class or a resource
         * @param name the resource name-key
         * @param location the resource file location-value
         */
        public static void addToIndex(LoaderIndex index, boolean isClass, String name, String location) {
            String loc = index.locationIndex.get(location);
            if (null == loc) {
                loc = String.valueOf(index.locationIndex.size());
                index.locationIndex.put(loc, location);
            }
            Map<String, String> map = isClass ? index.classIndex : index.resourceIndex;
            map.put(name, loc);
        }

    }
    
    /**
     * Creates an indexed classloader.
     * 
     * @param classIndex the class index (class-to-jar mapping)
     * @param resourceIndex the resource index (class-to-jar mapping)
     * @param parent the parent class loader
     */
    public ChildFirstIndexedClassloader(Map<String, String> locationIndex, Map<String, String> classIndex,
        Map<String, String> resourceIndex, ClassLoader parent) {
        super(parent);
        this.locationIndex = locationIndex;
        this.classIndex = classIndex;
        this.resourceIndex = resourceIndex;
    }

    /**
     * Creates an indexed classloader.
     * 
     * @param index the 
     */
    public ChildFirstIndexedClassloader(LoaderIndex index, ClassLoader parent) {
        this(index.locationIndex, index.classIndex, index.resourceIndex, parent);
    }

    /**
     * Creates an indexed classloader.
     * 
     * @param index the 
     */
    public ChildFirstIndexedClassloader(File index, ClassLoader parent) throws IOException {
        this(LoaderIndex.fromFile(index), parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String locStr = classIndex.get(name);
        if (null == locStr) {
            throw new ClassNotFoundException(name);
        }
        String jarPathStr = locationIndex.get(locStr);
        Path jarPath = Paths.get(jarPathStr);
        try {
            JarFile jar = jarCache.computeIfAbsent(jarPath, this::openJar);
            String entryName = name.replace('.', '/') + ".class";
            JarEntry entry = jar.getJarEntry(entryName);
            if (null == entry) {
                throw new ClassNotFoundException(name);
            }
            try (InputStream in = jar.getInputStream(entry)) {
                byte[] bytes = readAllBytes(in);
                return defineClass(name, bytes, 0, bytes.length);
            }
        } catch (IllegalArgumentException | IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
    
    /**
     * As long as readAllBytes is not available on InputStream. Do not use IOUtils
     * here as plugin may not be loaded.
     * 
     * @param in the input stream
     * @return the read bytes
     * @throws IOException if reading fails
     */
    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    @Override
    public URL findResource(String name) {
        String locStr = resourceIndex.get(name);
        if (null == locStr) {
            return null;
        }
        String jarPathStr = locationIndex.get(locStr);
        Path jarPath = Paths.get(jarPathStr);
        try {
            JarFile jar = jarCache.computeIfAbsent(jarPath, this::openJar);
            JarEntry entry = jar.getJarEntry(name);
            if (null == entry) {
                return null;
            }
            return new URL("jar:file:" + jarPath.toAbsolutePath() + "!/" + name);
        } catch (IllegalArgumentException | IOException e) {
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = findResource(name);
        if (null == url) {
            return null;
        }
        try {
            return url.openStream();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Opens a Jar from a {@code path}.
     * 
     * @param path the path
     * @return the {@link JarFile}
     * @throws IllegalArgumentException if the file cannot be opened
     */
    private JarFile openJar(Path path) throws IllegalArgumentException {
        try {
            return new JarFile(path.toFile(), false);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to open jar: " + path, e);
        }
    }

    /**
     * Close all cached jars when unloading plugin.
     */
    public void close() throws IOException {
        for (JarFile jar : jarCache.values()) {
            jar.close();
        }
        jarCache.clear();
    }

}
