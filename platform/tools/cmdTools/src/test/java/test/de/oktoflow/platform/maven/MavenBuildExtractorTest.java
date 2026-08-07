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

package test.de.oktoflow.platform.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import de.iip_ecosphere.platform.support.FileUtils;
import de.oktoflow.platform.cmdTools.MavenBuildExtractor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link MavenBuildExtractor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenBuildExtractorTest {

    /**
     * Tests {@link MavenBuildExtractor#appendSuffix(File, String)}.
     */
    @Test
    public void testSuffix() {
        File f = new File("folder/mvn.csv");
        File o = MavenBuildExtractor.appendSuffix(f, "-01");
        Assert.assertEquals(new File("folder/mvn-01.csv").getPath(), o.getPath());

        f = new File("folder/mvn");
        o = MavenBuildExtractor.appendSuffix(f, "-01");
        Assert.assertEquals(new File("folder/mvn-01").getPath(), o.getPath());

        f = new File("folder/mvn");
        o = MavenBuildExtractor.appendSuffix(f, "");
        Assert.assertEquals(f.getPath(), o.getPath());

        f = new File("folder/mvn");
        o = MavenBuildExtractor.appendSuffix(f, null);
        Assert.assertEquals(f.getPath(), o.getPath());
    }
    
    /**
     * Tests {@link MavenBuildExtractor}.
     * 
     * @throws IOException in case of I/O issues
     */
    @Test
    public void testExtractor() throws IOException {
        File mvnFolder = new File("src/test/resources/plugins");
        File outFolder = FileUtils.getTempDirectory();
        File expectedLogCsv = new File(outFolder, "mvn.csv");
        File expectedPluginCsv = new File(outFolder, "plugins.csv");
        
        expectedLogCsv.delete();
        expectedPluginCsv.delete();
        
        MavenBuildExtractor.main(new String[] {mvnFolder.getPath(), outFolder.getPath()});
        
        Assert.assertTrue("Log CSV does not exist: " + expectedLogCsv.getPath(), expectedLogCsv.exists());
        Assert.assertTrue("Log CSV appears to be empty: " + expectedLogCsv.getPath(), 
            expectedLogCsv.isFile() && expectedLogCsv.length() > 0);
        List<String> extracted = Files.readAllLines(expectedLogCsv.toPath());
        Assert.assertTrue("Log CSV does not contain header: ", extracted.contains("testClass,elapsedTime"));

        Assert.assertTrue("Plugin CSV does not exist: " + expectedPluginCsv.getPath(), expectedPluginCsv.exists());
        Assert.assertTrue("Plugin CSV appears to be empty: " + expectedPluginCsv.getPath(), 
            expectedPluginCsv.isFile() && expectedPluginCsv.length() > 0);
        extracted = Files.readAllLines(expectedPluginCsv.toPath());
        Assert.assertTrue("Plugin CSV does not contain header: ", extracted.contains("plugin,time"));

        expectedLogCsv.delete();
        expectedPluginCsv.delete();
    }

}
