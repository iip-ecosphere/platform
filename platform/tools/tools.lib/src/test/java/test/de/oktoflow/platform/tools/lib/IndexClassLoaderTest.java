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

package test.de.oktoflow.platform.tools.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import org.junit.Test;

import de.oktoflow.platform.tools.lib.HashUtils;
import de.oktoflow.platform.tools.lib.loader.IndexClassloader;
import de.oktoflow.platform.tools.lib.loader.LoaderIndex;
import de.oktoflow.platform.tools.lib.loader.LoaderIndex.IncrementalListener;

import org.junit.Assert;

/**
 * Tests {@link IndexClassloader}.
 * 
 * @author Holger Eichelberger, SSE
 * @author Gemini
 */
public class IndexClassLoaderTest {

    /**
     * Extends for null returns.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyIndexClassloader extends IndexClassloader {
        
        /**
         * Creates an indexed classloader.
         */
        protected MyIndexClassloader(LoaderIndex index) {
            super(index, true);
        }

    }
    
    /**
     * Tests {@link IndexClassloader}.
     * 
     * @throws IOException if files are not found
     * @throws ClassNotFoundException the the expected class is not found
     */
    @Test
    public void testLoader() throws IOException, ClassNotFoundException {
        final File indexFile = new File("src/test/resources/index.idx");
        final String clsName = "indexLoaderTest.ClassToLoad";
        final String resName = "testResource.txt";
        final String location = "src/test/resources/index.jar";
        final String location2 = "src/test/resources/index2.jar";

        LoaderIndex index = new LoaderIndex();
        LoaderIndex.addToIndex(index, true, clsName, location);
        LoaderIndex.addToIndex(index, true, clsName, location2);
        LoaderIndex.addToIndex(index, false, resName, location);
        LoaderIndex.addToIndex(index, false, resName, location2);
        LoaderIndex.toFile(index, indexFile);
        System.out.println("index.idx written");
        
        Assert.assertTrue(index.getClassesCount() > 0);
        Assert.assertTrue(index.getResourcesCount() > 0);
        Assert.assertTrue(index.getLocationsCount() > 0);
        Assert.assertNotNull(index.getClassLocation(clsName));
        Assert.assertNull(index.getClassLocation("abc"));
        Assert.assertNotNull(index.getResourceLocations(resName));
        Assert.assertNull(index.getResourceLocations("abc"));

        IndexClassloader loader = new IndexClassloader(indexFile, null);
        Class<?> cls = loader.loadClass(clsName);
        Assert.assertNotNull(cls);
        Assert.assertEquals(clsName, cls.getName());
        Assert.assertNotNull(loader.findResource(resName));
        Assert.assertNotNull(loader.getResource(resName));
        Assert.assertNotNull(loader.getResourceAsStream(resName));
        List<URL> res = Collections.list(loader.getResources(resName));
        Assert.assertEquals(2, res.size());
        Assert.assertNotNull(loader.getURLs()); // the construction here does not add URLs
        loader.close();
        
        loader = new MyIndexClassloader(index);
        cls = loader.loadClass(clsName);
        Assert.assertNotNull(cls);
        Assert.assertEquals(clsName, cls.getName());
        cls = loader.loadClass("de.unknown.UnknownClass");
        Assert.assertNull(cls);
        loader.close();
    }
    
    /**
     * Collects index update operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class CollectingIncrementalListener implements IncrementalListener {
        
        private int updated = 0;
        private int transferred = 0;

        @Override
        public void notify(String location, boolean update) {
            if (update) {
                updated++;
            } else {
                transferred++;
            }
        }
        
        /**
         * Clears the collected data.
         */
        private void clear() {
            updated = 0;
            transferred = 0;
        }
        
    }
    
    /**
     * Tests incremental indexing.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIncrementalIndex() throws IOException {
        File oldIndexFile = new File(System.getProperty(System.getProperty("java.io.tmpdir")), "oldIndex.idx");

        LoaderIndex oldIndex = new LoaderIndex();
        List<Path> jars = new ArrayList<>();
        jars.add(new File("src/test/resources/index.jar").toPath());
        jars.add(new File("src/test/resources/index2.jar").toPath());
        Properties hashes = new Properties();
        LoaderIndex.addToIndex(oldIndex, jars, e -> System.out.println(HashUtils.toMessage(e)), hashes, null);

        CollectingIncrementalListener listener = new CollectingIncrementalListener();
        LoaderIndex newIndex = new LoaderIndex();
        newIndex.setIncrementalListener(listener);
        LoaderIndex.addToIndex(newIndex, jars, e -> System.out.println(HashUtils.toMessage(e)), hashes, null);
        // newIndex is the same version, no change
        Assert.assertEquals(oldIndex.getFilesCount(), newIndex.getFilesCount());
        Assert.assertEquals(oldIndex.getClassesCount(), newIndex.getClassesCount());
        Assert.assertEquals(oldIndex.getResourcesCount(), newIndex.getResourcesCount());
        Assert.assertEquals(oldIndex.getLocationsCount(), newIndex.getLocationsCount());
        Assert.assertEquals(0, listener.transferred); // addToIndex just for hashes, no incremental update
        Assert.assertEquals(2, listener.updated);
        listener.clear();
        
        final String tmp = System.getProperty("java.io.tmpdir");
        final File additionalFile = new File("src/test/resources/additionalFile.txt");
        File modJar = new File(tmp, "iip-tools/src/test/resources");
        modJar.mkdirs();
        File tmpFolder = new File(tmp + "/iip-tools");
        modJar = new File(tmpFolder, "src/test/resources/index.jar"); // to path
        modJar.delete();
        // modify existing jar file by copying entries to temp (modified) jar file
        try (JarFile jar = new JarFile(new File("src/test/resources/index.jar"));
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(modJar))) {
            jar.stream().forEach(e -> {
                try {
                    jos.putNextEntry(new JarEntry(e.getName()));
                    transferStream(jar, e, jos);
                    jos.closeEntry();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }); // just throw

            // Add the new file
            jos.putNextEntry(new JarEntry(additionalFile.getName()));
            Files.copy(additionalFile.toPath(), jos);
            jos.closeEntry();
        } // just throw
        
        // modify one jar in temp, adjust list
        jars.clear();
        jars.add(modJar.toPath());
        jars.add(new File("src/test/resources/index2.jar").toPath());

        // incrementally updated index
        
        newIndex = new LoaderIndex();
        newIndex.setIncrementalListener(listener);
        LoaderIndex.addToIndex(newIndex, jars, e -> System.out.println(HashUtils.toMessage(e)), hashes, oldIndex);
        
        modJar.delete();
        try (Stream<Path> pathStream = Files.walk(tmpFolder.toPath())) {
            pathStream.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        }        
        oldIndexFile.delete();

        // newIndex is the updated version
        Assert.assertEquals(oldIndex.getFilesCount(), newIndex.getFilesCount());
        Assert.assertEquals(oldIndex.getClassesCount(), newIndex.getClassesCount());
        Assert.assertEquals(oldIndex.getResourcesCount() + 1, newIndex.getResourcesCount());
        Assert.assertEquals(oldIndex.getLocationsCount(), newIndex.getLocationsCount());
        listener.clear();
    }
    
    /**
     * Transfers the contents of {@code ent} in {@code jar} to {@code os}.
     * 
     * @param jar the jar file to transfer from
     * @param ent the entry within {@code jar} to transfer from
     * @param os the output stream to transfer to
     * @throws IOException if exceptions occur
     */
    private static void transferStream(JarFile jar, JarEntry ent, OutputStream os) throws IOException {
        try (InputStream is = jar.getInputStream(ent)) {
            copyStream(is, os);
        }                
    }
    
    /**
     * Copies the contents of stream {@code in} to stream {@code out}.
     * 
     * @param in the input stream
     * @param out the output stream
     * @throws IOException if exceptions occur
     */
    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
    
}
