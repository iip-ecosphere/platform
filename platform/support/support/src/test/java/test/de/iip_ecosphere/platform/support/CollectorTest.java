/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
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
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.collector.Collector;
import de.iip_ecosphere.platform.support.collector.Collector.Field;
import de.iip_ecosphere.platform.support.collector.CollectorSetup;

/**
 * Tests {@link Collector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CollectorTest {

    /**
     * Tests the collector.
     */
    @Test
    public void testCollector() throws IOException {
        CollectorSetup setup = new CollectorSetup();
        CollectorSetup orig = Collector.setSetup(setup);
        Field[] origFields = Collector.getFields();
        
        Collector
            .collect("tag")
            .addExecutionTimeMs(1000)
            .close();
        
        System.setProperty(Collector.PROPERTY_BUILDID, "9999");
        File tmp = FileUtils.getTempDirectory();
        File tagFile = new File(tmp, "tag.csv");
        FileUtils.deleteQuietly(tagFile);
        
        setup.setDataDir(FileUtils.getTempDirectoryPath());
        Collector.setSetup(setup);
        Collector
            .collect("tag")
            .measureMs(() -> TimeUtils.sleep(1000))
            .close();
        
        Assert.assertTrue(tagFile.exists());
        String tagFileContents = FileUtils.readFileToString(tagFile, Charset.defaultCharset());
        String[] tagFileLines = tagFileContents.split("\\r?\\n");
        Assert.assertEquals(2, tagFileLines.length);
        assertHeader(tagFileLines[0], origFields);
        Assert.assertTrue(tagFileLines[1].matches("^\\d+,\"9999\",\\d+$"));
        
        Field[] fields = new Field[origFields.length + 1];
        System.arraycopy(origFields, 0, fields, 0, origFields.length);
        fields[origFields.length] = new Field("myField", -1);
        Collector.setFields(fields);
        Collector
            .collect("tag")
            .measureMs(() -> TimeUtils.sleep(1000))
            .close();

        tagFileContents = FileUtils.readFileToString(tagFile, Charset.defaultCharset());
        tagFileLines = tagFileContents.split("\\r?\\n");
        Assert.assertEquals(3, tagFileLines.length);
        assertHeader(tagFileLines[0], fields);
        Assert.assertTrue(tagFileLines[1].matches("^\\d+,\"9999\",\\d+,-1$"));
        Assert.assertTrue(tagFileLines[2].matches("^\\d+,\"9999\",\\d+,-1$"));

        Collector.setFields(origFields);
        
        FileUtils.deleteQuietly(tagFile);
        Collector.setSetup(orig);
    }
    
    /**
     * Asserts a file header.
     * 
     * @param text the text from the file
     * @param fields the expected fields
     */
    private void assertHeader(String text, Field[] fields) {
        String[] line = text.split(",");
        Assert.assertEquals(fields.length, line.length);
        for (int l = 0; l < line.length; l++) {
            String e = line[l];
            if (e.startsWith("\"") && e.endsWith("\"")) {
                e = e.substring(1, e.length() - 1);
            }
            Assert.assertEquals(e, fields[l].getName());
        }
    }
    
}
