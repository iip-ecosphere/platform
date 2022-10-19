/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.formatter.FormatCache;
import org.junit.Assert;

/**
 * Tests {@link FormatCache}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FormatCacheTest {

    /**
     * Tests {@link FormatCache}.
     */
    @Test
    public void testFormatCache() throws IOException {
        final String format = "yyyy-MM-dd HH:mm:ss";
        Date d = new Date();
        Date t = FormatCache.parse(d, format);
        Assert.assertNotNull(t);
        LocalDateTime l = LocalDateTime.now();
        t = FormatCache.parse(l, format);
        Assert.assertNotNull(t);
        t = FormatCache.parse("2022-10-19 08:00:21", format);
        Assert.assertNotNull(t);
        t = FormatCache.parse(null, format);
        Assert.assertNull(t);
        t = FormatCache.parse("2019-05-09T05:47:39.407Z", FormatCache.ISO8601_FORMAT);
        Assert.assertNotNull(t);
        
        String s = FormatCache.format(d, FormatCache.ISO8601_FORMAT);
        Assert.assertNotNull(s);
        s = FormatCache.format(d, format);
        Assert.assertNotNull(s);
    }

}
