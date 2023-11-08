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
import test.de.iip_ecosphere.platform.support.collector.Collector;
import test.de.iip_ecosphere.platform.support.collector.CollectorSetup;

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
        CollectorSetup orig = Collector.setSetup(null);
        
        Collector
            .collect("tag")
            .addExecutionTimeMs(1000)
            .close();
        
        System.setProperty(Collector.PROPERTY_BUILDID, "9999");
        File tmp = FileUtils.getTempDirectory();
        File tagFile = new File(tmp, "tag.csv");
        FileUtils.deleteQuietly(tagFile);
        
        CollectorSetup setup = new CollectorSetup();
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
        Assert.assertTrue(tagFileLines[0].matches("^\".*\"$"));
        Assert.assertTrue(tagFileLines[1].matches("^\\d+,\"9999\",\\d+$"));
        
        FileUtils.deleteQuietly(tagFile);
        Collector.setSetup(orig);
    }

}
