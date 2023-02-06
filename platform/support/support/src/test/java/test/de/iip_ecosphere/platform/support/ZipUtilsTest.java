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

package test.de.iip_ecosphere.platform.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.ZipUtils;

/**
 * Tests {@link ZipUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ZipUtilsTest {
    
    /**
     * Tests {@link ZipUtils#extractZip(java.io.InputStream, java.nio.file.Path)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void extractFullZip() throws IOException {
        File f = FileUtils.createTmpFolder("support.jar");
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        Assert.assertNotNull(in);
        ZipUtils.extractZip(in, f.toPath());
        assertFileExists(f, "text.txt", false);
        assertFileExists(f, "folder1", true);
        assertFileExists(f, "folder1/text11.txt", false);
        assertFileExists(f, "folder1/text12.txt", false);
        assertFileExists(f, "folder2", true);
        assertFileExists(f, "folder2/text21.txt", false);
        assertFileExists(f, "folder2/text22.txt", false);
        FileUtils.deleteQuietly(f);
    }

    /**
     * Asserts that a file/directory exists.
     * 
     * @param base the base directory
     * @param name the name/path in {@code base} to be asserted
     * @param isDirectory {@code true} if the specified shall be a directory, {@code false} for a file 
     */
    private static void assertFileExists(File base, String name, boolean isDirectory) {
        File f = new File(base, name);
        Assert.assertTrue(f.toString() + " does not exist", f.exists());
        if (isDirectory) {
            Assert.assertTrue(f.isDirectory());
        } else {
            Assert.assertTrue(f.isFile());
        }
    }
    
    /**
     * Asserts that a file/directory does not exists.
     * 
     * @param base the base directory
     * @param name the name/path in {@code base} to be asserted
     */
    private static void assertFileNotExists(File base, String name) {
        File f = new File(base, name);
        Assert.assertFalse(f.exists());
    }

    /**
     * Tests {@link ZipUtils#extractZip(java.io.InputStream, java.nio.file.Path, java.util.function.Predicate)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void extractPartialZip() throws IOException {
        File f = FileUtils.createTmpFolder("support.jar");
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        Assert.assertNotNull(in);
        ZipUtils.extractZip(in, f.toPath(), ZipUtils.inFolder("folder1"));
        assertFileNotExists(f, "text.txt");
        assertFileExists(f, "folder1", true);
        assertFileExists(f, "folder1/text11.txt", false);
        assertFileExists(f, "folder1/text12.txt", false);
        assertFileNotExists(f, "folder2");
        assertFileNotExists(f, "folder2/text21.txt");
        assertFileNotExists(f, "folder2/text22.txt");
        FileUtils.deleteQuietly(f);
    }

    /**
     * Tests {@link ZipUtils#findFile(java.io.InputStream, String)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void extactFile() throws IOException {
        File f = FileUtils.createTmpFolder("support.jar");
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        InputStream fileStream = ZipUtils.findFile(in, "folder2/text21.txt");
        Assert.assertNotNull(fileStream);
        String test = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
        fileStream.close();
        Assert.assertEquals("text21.txt", test);
        FileUtils.closeQuietly(in);

        in = getClass().getClassLoader().getResourceAsStream("test.zip");
        fileStream = ZipUtils.findFile(in, "folder2/text23.txt");
        Assert.assertNull(fileStream);
        FileUtils.closeQuietly(in);

        FileUtils.deleteQuietly(f);
    }
    
    /**
     * Tests {@link ZipUtils#listFiles(InputStream, java.util.function.Predicate, java.util.function.Consumer)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void testListFiles() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        AtomicInteger fileCount = new AtomicInteger();
        AtomicInteger dirCount = new AtomicInteger();
        
        ZipUtils.listFiles(in, z -> true, z -> {
            if (z.isDirectory()) {
                dirCount.incrementAndGet();
            } else {
                fileCount.incrementAndGet();
            }
        });
        FileUtils.closeQuietly(in);
        Assert.assertEquals(5, fileCount.get());
        Assert.assertEquals(2, dirCount.get());
        
        fileCount.set(0);
        dirCount.set(0);
        
        
        in = getClass().getClassLoader().getResourceAsStream("test.zip");
        ZipUtils.listFiles(in, z -> !z.isDirectory(), z -> {
            if (z.isDirectory()) {
                dirCount.incrementAndGet();
            } else {
                fileCount.incrementAndGet();
            }
        });
        FileUtils.closeQuietly(in);
        Assert.assertEquals(5, fileCount.get());
        Assert.assertEquals(0, dirCount.get());
    }

}
