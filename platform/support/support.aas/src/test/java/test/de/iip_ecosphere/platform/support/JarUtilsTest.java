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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;

/**
 * Tests {@link JarUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JarUtilsTest {
    
    /**
     * Tests {@link JarUtils#extractZip(java.io.InputStream, java.nio.file.Path)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void extractFullZip() throws IOException {
        File f = FileUtils.createTmpFolder("support.jar");
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        Assert.assertNotNull(in);
        JarUtils.extractZip(in, f.toPath());
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
     * Tests {@link JarUtils#extractZip(java.io.InputStream, java.nio.file.Path, java.util.function.Predicate)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void extractPartialZip() throws IOException {
        File f = FileUtils.createTmpFolder("support.jar");
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        Assert.assertNotNull(in);
        JarUtils.extractZip(in, f.toPath(), JarUtils.inFolder("folder1"));
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
     * Tests {@link JarUtils#findFile(java.io.InputStream, String)}.
     * 
     * @throws IOException if reading/writing fails
     */
    @Test
    public void extactFile() throws IOException {
        File f = FileUtils.createTmpFolder("support.jar");
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.zip");
        InputStream fileStream = JarUtils.findFile(in, "folder2/text21.txt");
        Assert.assertNotNull(fileStream);
        String test = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
        fileStream.close();
        Assert.assertEquals("text21.txt", test);

        in = getClass().getClassLoader().getResourceAsStream("test.zip");
        fileStream = JarUtils.findFile(in, "folder2/text23.txt");
        Assert.assertNull(fileStream);
        in.close();

        FileUtils.deleteQuietly(f);
    }

}
