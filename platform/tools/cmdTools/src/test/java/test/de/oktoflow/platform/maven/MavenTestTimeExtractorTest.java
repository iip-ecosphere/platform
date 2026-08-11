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

import de.iip_ecosphere.platform.support.FileUtils;
import de.oktoflow.platform.cmdTools.MavenTestTimeExtractor;

/**
 * Tests {@link MavenTestTimeExtractor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenTestTimeExtractorTest {

    /**
     * Tests {@link MavenTestTimeExtractor}.
     * 
     * @throws IOException if IO operations fail
     */
    @Test
    public void testExtractor() throws IOException {
        File base = new File("src/test/resources/plugins");
        File input = new File(base, "mvn.log");
        File output = new File(FileUtils.getTempDirectory(), "surefire-time-extraction.csv");
        output.delete();
        MavenTestTimeExtractor.main(new String[] {input.getPath(), output.getPath()});
        Assert.assertTrue(output.exists());
        
        List<String> expected = Files.readAllLines(new File(base, "testTimesExpected.txt").toPath());        
        List<String> extracted = Files.readAllLines(output.toPath());
        extracted.forEach(l -> expected.remove(l));
        Assert.assertTrue("Not matched expected lines: " + expected, expected.size() == 0);
        
        output.delete();
    }

    /**
     * Tests {@link MavenTestTimeExtractor}.
     * 
     * @throws IOException if IO operations fail
     */
    @Test
    public void testEnvExtractor() throws IOException {
        File base = new File("src/test/resources/pluginEnv");
        File input = new File(base, "env.log");
        File output = new File(FileUtils.getTempDirectory(), "surefire-time-extraction.csv");
        output.delete();
        MavenTestTimeExtractor.readTail(input, output);
        Assert.assertTrue(output.exists());
        
        List<String> expected = Files.readAllLines(new File(base, "testTimesExpected.txt").toPath());        
        List<String> extracted = Files.readAllLines(output.toPath());
        extracted.forEach(l -> expected.remove(l));
        Assert.assertTrue("Not matched expected lines: " + expected, expected.size() == 0);
        
        output.delete();
    }

}
