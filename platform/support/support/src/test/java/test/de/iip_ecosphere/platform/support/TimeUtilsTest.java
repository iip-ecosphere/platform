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
        Assert.assertTrue(80 < diff && diff < 200);
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

}
