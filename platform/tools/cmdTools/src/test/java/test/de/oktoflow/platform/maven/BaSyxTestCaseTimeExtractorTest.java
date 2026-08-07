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
import de.oktoflow.platform.cmdTools.BaSyxTestCaseTimeExtractor;

/**
 * Tests {@link BaSyxTestCaseTimeExtractorTest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTestCaseTimeExtractorTest {

    /**
     * Tests {@link BaSyxTestCaseTimeExtractorTest}.
     * 
     * @throws IOException if IO operations fail
     */
    @Test
    public void testExtractor() throws IOException {
        File base = new File("src/test/resources/plugins/target/surefire-reports");
        File input = new File(base, "test.de.iip_ecosphere.platform.support.aas.basyx.BaSyxTest-output.txt");
        File output = new File(FileUtils.getTempDirectory(), "basyx-time-extraction.csv");
        output.delete();
        BaSyxTestCaseTimeExtractor.main(new String[] {input.getPath(), output.getPath()});
        Assert.assertTrue(output.exists());
        
        List<String> expected = Files.readAllLines(new File(base, "basyxTimeExpected.txt").toPath());                
        List<String> extracted = Files.readAllLines(output.toPath());
        extracted.forEach(l -> expected.remove(l));
        Assert.assertTrue("Not matched expected lines: " + expected, expected.size() == 0);
        
        output.delete();
    }

}
