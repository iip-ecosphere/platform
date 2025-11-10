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

import de.iip_ecosphere.platform.support.TimeUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link TimeUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TimeUtilsTest {

    /**
     * Tests the {@link TimeUtils#sleep(int)} method.
     */
    @Test
    public void testSleep() {
        long before = System.currentTimeMillis();
        TimeUtils.sleep(100);
        long after = System.currentTimeMillis();
        long diff = after - before;
        // may diff, e.g., laptop without power plug
        Assert.assertTrue("Diff shall not be less than 40 ms", 60 < diff);
        Assert.assertTrue("Diff shall not be larger than 200 ms", diff < 200);
    }
    
    /**
     * Tests the {@link TimeUtils#waitFor(java.util.function.Supplier, int, int)} method.
     */
    @Test
    public void testWaitFor() {
        Assert.assertTrue(TimeUtils.waitFor(() -> false, 200, 100)); // return immediately
        
        AtomicInteger counter = new AtomicInteger(0);
        // return after 5 rounds, but called to wait endlessly
        Assert.assertTrue(TimeUtils.waitFor(() -> counter.incrementAndGet() < 5, -1, 100)); 

        counter.set(0);
        // return after 5 rounds, before timeout
        Assert.assertTrue(TimeUtils.waitFor(() -> counter.incrementAndGet() < 5, 1000, 100)); 

        counter.set(0);
        // timeout happens before
        Assert.assertFalse(TimeUtils.waitFor(() -> counter.incrementAndGet() < 10, 300, 100)); 
    }
    
    /**
     * Tests the formatting functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testFormats() throws IOException {
        final String format = "yyyy-MM-dd HH:mm:ss";
        Date d = new Date();
        Date t = TimeUtils.parse(d, format);
        Assert.assertNotNull(t);
        LocalDateTime l = TimeUtils.toLocalDateTime(t);
        Assert.assertNotNull(l);
        Assert.assertNull(TimeUtils.toLocalDateTime(null));
        l = LocalDateTime.now();
        t = TimeUtils.parse(l, format);
        Assert.assertNotNull(t);
        t = TimeUtils.parse("2022-10-19 08:00:21", format);
        Assert.assertNotNull(t);
        t = TimeUtils.parse(null, format);
        Assert.assertNull(t);
        t = TimeUtils.parse("2019-05-09T05:47:39.407Z", TimeUtils.ISO8601_FORMAT);
        Assert.assertNotNull(t);
        
        String s = TimeUtils.format(d, TimeUtils.ISO8601_FORMAT);
        Assert.assertNotNull(s);
        s = TimeUtils.format(d, format);
        Assert.assertNotNull(s);
        
        Assert.assertNull(TimeUtils.toDate(null));
    }

}
