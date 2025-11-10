/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.oktoflow.platform.support.commons.apache;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link IOUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IOUtilsTest {

    /**
     * Tests {@link IOUtils#readLines(java.io.InputStream)} and 
     * {@link IOUtils#readLines(java.io.InputStream, Charset)}.
     * 
     * @throws IOException shall not occur if successful
     */
    @Test
    public void testReadLines() throws IOException {
        List<String> lines = IOUtils.readLines(ResourceLoader.getResourceAsStream("identityStore.yml"));
        Assert.assertNotNull(lines);
        Assert.assertTrue(lines.size() > 0);

        lines = IOUtils.readLines(ResourceLoader.getResourceAsStream("identityStore.yml"), Charset.defaultCharset());
        Assert.assertNotNull(lines);
        Assert.assertTrue(lines.size() > 0);
    }
   
    /**
     * Tests {@link IOUtils#toString(java.io.InputStream)} and 
     * {@link IOUtils#toString(java.io.InputStream, Charset)}.
     * 
     * @throws IOException shall not occur if successful
     */
    @Test
    public void testToString() throws IOException {
        String contents = IOUtils.toString(ResourceLoader.getResourceAsStream("identityStore.yml"));
        Assert.assertNotNull(contents);
        Assert.assertTrue(contents.length() > 0);

        contents = IOUtils.toString(ResourceLoader.getResourceAsStream("identityStore.yml"), Charset.defaultCharset());
        Assert.assertNotNull(contents);
        Assert.assertTrue(contents.length() > 0);
    }

}
