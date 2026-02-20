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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A class loader that takes its indexing from an index file. Requires first running 
 * {@link LoaderIndex#createIndex(List, java.util.function.Consumer)} on the Jars to load and then feeding the index 
 * into this class loader with. We 
 * inherit from {@link URLClassLoader} and override {@link #getURLs()} so that frameworks like 
 * <a href="https://github.com/classgraph/classgraph">ClassGraph</a> take a (transparent) notice of this class loader.
 * 
 * 
 * @author ChatGPT
 * @author Holger Eichelberger, SSE
 */
public class IndexClassloader extends URLClassLoader {
    
    private static final String CLASS_SUFFIX = ".class";
    private final List<String> files;
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
     * @param index the index instance
     * @param parent the parent class loader for delegation
     */
    public IndexClassloader(LoaderIndex index, ClassLoader parent) {
        super(new URL[0], parent);
        this.files = index.getFilesList();
        this.locationIndex = index.getLocationIndex();
        this.classIndex = index.getClassIndex();
        this.resourceIndex = index.getResourceIndex();
    }

    /**
     * Creates an indexed classloader.
     * 
     * @param index the index file
     * @param parent the parent class loader for delegation
     * @throws IOException if loading the index fails
     */
    public IndexClassloader(File index, ClassLoader parent) throws IOException {
        this(LoaderIndex.fromFile(index), parent);
    }

    @Override
    public URL[] getURLs() { // for ClassGraph
        URL[] result = new URL[files.size()];
        for (int f = 0; f < files.size(); f++) {
            try {
                result[f] = new File(files.get(f)).toURI().toURL();
            } catch (MalformedURLException e) {
                System.err.println("Cannot turn file " + files.get(f) + " to URL: " + e.getMessage());
            }
        }
        return result;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String locStr = classIndex.get(name);
        if (null == locStr) {
            throw new ClassNotFoundException(name);
        }
        locStr = locStr.split(LoaderIndex.RESOURCE_SEPARATOR)[0]; // only the first one here
        String jarPathStr = locationIndex.get(locStr);
        Path jarPath = Paths.get(jarPathStr);
        try {
            JarFile jar = jarCache.computeIfAbsent(jarPath, this::openJar);
            String entryName = name.replace('.', '/') + CLASS_SUFFIX;
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
        String locStr = getResourceLocation(name);
        if (null == locStr) {
            return null;
        }
        locStr = locStr.split(LoaderIndex.RESOURCE_SEPARATOR)[0]; // only the first one here
        String jarPathStr = locationIndex.get(locStr);
        Path jarPath = Paths.get(jarPathStr);
        try {
            JarFile jar = jarCache.computeIfAbsent(jarPath, this::openJar);
            JarEntry entry = jar.getJarEntry(name);
            if (null == entry) {
                return null;
            }
            return constructJarFileUrl(jarPath.toAbsolutePath(), name);
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
     * Obtains the resource location, alternatively taking class file names as resources.
     * 
     * @param name the resource/class name
     * @return the resource location(s), may be separated by {@link LoaderIndex#RESOURCE_SEPARATOR}
     */
    private String getResourceLocation(String name) {
        String locStr = resourceIndex.get(name);
        if (null == locStr && name.endsWith(CLASS_SUFFIX)) {
            String modName = name.substring(0, name.length() - CLASS_SUFFIX.length()).replace('/', '.');
            locStr = classIndex.get(modName);
            if (null == locStr) {
                // Spring 2.4 behavior package$Class for non-inner classes
                locStr = classIndex.get(modName.replace('$', '/')); 
            }
        }    
        return locStr;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        String locStr = getResourceLocation(name);
        if (null == locStr) {
            return null;
        }
        String[] locStrs = locStr.split(LoaderIndex.RESOURCE_SEPARATOR);
        List<URL> result = new ArrayList<URL>();
        for (int l = 0; l < locStrs.length; l++) {
            String jarPathStr = locationIndex.get(locStrs[l]);
            Path jarPath = Paths.get(jarPathStr);
            try {
                JarFile jar = jarCache.computeIfAbsent(jarPath, this::openJar);
                JarEntry entry = jar.getJarEntry(name);
                if (null == entry) {
                    return null;
                }
                result.add(constructJarFileUrl(jarPath.toAbsolutePath(), name));
            } catch (IllegalArgumentException | IOException e) {
                return Collections.emptyEnumeration();
            }
        }
        return Collections.enumeration(result);
    }
    
    /**
     * Constructs a Jar-file URL.
     * 
     * @param path the path to the jar file
     * @param entry the path within the Jar file to the JarEntry
     * @return the URL
     * @throws MalformedURLException if the URL cannot be constructed
     */
    private static URL constructJarFileUrl(Path path, String entry) throws MalformedURLException {
        return new URL("jar:file:" + LoaderIndex.normalize(path) + "!/" + entry);
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
