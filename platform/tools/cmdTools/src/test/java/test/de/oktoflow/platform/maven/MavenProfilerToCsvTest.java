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

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.cmdTools.MavenProfilerToCsv;

/**
 * Tests {@link MavenProfilerToCsv}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenProfilerToCsvTest {

    /**
     * Tests merging and converting profiler JSON to CSV.
     * 
     * @throws IOException if execution fails
     */
    @Test
    public void testMergeAndConvert() throws IOException {
        String result = System.getProperty("java.io.tmpdir") + "/merge.csv";
        MavenProfilerToCsv.main(new String[] {"src/test/resources/profiler", result});
        File resultFile = new File(result);
        Assert.assertTrue(resultFile.exists());
        List<String> lines = Files.readAllLines(resultFile.toPath());
        int found = 0;        
        for (String l : lines) {
            System.out.println(l);
            if (l.contains("org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent") && l.endsWith(";101")) {
                found++;
            }
            if (l.contains("org.apache.maven.plugins:maven-compiler-plugin:3.7.0:compile") && l.endsWith(";102")) {
                found++;
            }
            if (l.contains("org.apache.maven.plugins:maven-resources-plugin:3.3.1:testResources") && l.endsWith(";7")) {
                found++;
            }
        }
        Assert.assertEquals(3, found);
        resultFile.delete();
    }

}
