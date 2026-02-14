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

package de.oktoflow.platform.tools.lib.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        this(index.getLocationIndex(), index.getClassIndex(), index.getResourceIndex(), parent);
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
