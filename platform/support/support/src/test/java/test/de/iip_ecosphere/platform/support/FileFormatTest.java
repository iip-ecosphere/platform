/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support;

import org.junit.Test;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;

import java.io.File;

import org.junit.Assert;

/**
 * Tests {@link FileFormat} and {@link ExtensionBasedFileFormat}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileFormatTest {

    /**
     * Tests {@link FileFormat} and {@link ExtensionBasedFileFormat}.
     */
    @Test
    public void testFileFormat() {
        ExtensionBasedFileFormat ff = new ExtensionBasedFileFormat("exe", "Executable", "Windows Executable");
        Assert.assertEquals("exe", ff.getExtension());
        Assert.assertEquals("Executable", ff.getName());
        Assert.assertEquals("Windows Executable", ff.getDescription());
        Assert.assertEquals("Executable (*.exe, Windows Executable)", ff.toString());
        Assert.assertFalse(ff.matches(new File("readme.txt")));
        Assert.assertTrue(ff.matches(new File("cmd.exe")));
        
        try {
            ff = new ExtensionBasedFileFormat(".exe", null, "Windows Executable");
            Assert.fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            ff = new ExtensionBasedFileFormat(".exe", "", "Windows Executable");
            Assert.fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        }
        
        ff = new ExtensionBasedFileFormat(".exe", "Executable", "");
        Assert.assertEquals("exe", ff.getExtension());
        Assert.assertEquals("Executable", ff.getName());
        Assert.assertEquals("", ff.getDescription());
        Assert.assertEquals("Executable (*.exe)", ff.toString());
        Assert.assertFalse(ff.matches(new File("ls")));
        Assert.assertFalse(ff.matches(new File("readme.txt")));
        Assert.assertTrue(ff.matches(new File("cmd.exe")));

        ff = new ExtensionBasedFileFormat(".exe", "Executable", null);
        Assert.assertEquals("exe", ff.getExtension());
        Assert.assertEquals("Executable", ff.getName());
        Assert.assertEquals("", ff.getDescription());
        Assert.assertEquals("Executable (*.exe)", ff.toString());
        Assert.assertFalse(ff.matches(new File("ls")));
        Assert.assertFalse(ff.matches(new File("readme.txt")));
        Assert.assertTrue(ff.matches(new File("cmd.exe")));

        ff = new ExtensionBasedFileFormat("", "All", "All file formats");
        Assert.assertEquals("", ff.getExtension());
        Assert.assertEquals("All", ff.getName());
        Assert.assertEquals("All file formats", ff.getDescription());
        Assert.assertEquals("All (All file formats)", ff.toString());
        Assert.assertTrue(ff.matches(new File("ls")));
        Assert.assertTrue(ff.matches(new File("readme.txt")));
        Assert.assertTrue(ff.matches(new File("cmd.exe")));
    }

}
