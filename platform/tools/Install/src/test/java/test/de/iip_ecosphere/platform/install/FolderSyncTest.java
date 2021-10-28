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

package test.de.iip_ecosphere.platform.install;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.install.FolderSync;
import de.iip_ecosphere.platform.support.FileUtils;

/**
 * Tests {@link FolderSync}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FolderSyncTest {
    
    /**
     * Tests {@link FolderSync}.
     */
    @Test
    public void testFolderSync() {
        File src1 = new File("./src/test/resources/plJars");
        File src2 = new File("./src/test/resources/svcJars");
        File target = FileUtils.createTmpFolder("iip-install");
        FolderSync.main(target.getAbsolutePath(), src1.getAbsolutePath(), src2.getAbsolutePath());

        assertFiles(target, FolderSync.COMMON_FOLDER_NAME, "de.iip-ecosphere.platform.support.aas-0.3.0", 
            "de.iip-ecosphere.platform.support.aas.basxy-0.3.0");
        assertFiles(target, "plJars", "antlr.antlr-2.7.7");
        assertFiles(target, "svcJars", "commons-codec.commons-codec-1.15");

        FileUtils.deleteQuietly(target);
    }
    
    /**
     * Asserts files and folders.
     * 
     * @param baseFolder the base folder
     * @param folderName the folder to assert (existing)
     * @param fileNames optional files within {@code folderName} to be asserted (existing, non-empty)
     */
    private static void assertFiles(File baseFolder, String folderName, String... fileNames) {
        File folder = new File(baseFolder, folderName);
        Assert.assertTrue("Folder " + folder + " does not exist", folder.exists());
        for (String fName : fileNames) {
            File f = new File(folder, fName);
            Assert.assertTrue("File " + f + " does not exist", f.exists());
            Assert.assertTrue("File " + f + " is empty", f.length() > 0);
        }
    }

}
