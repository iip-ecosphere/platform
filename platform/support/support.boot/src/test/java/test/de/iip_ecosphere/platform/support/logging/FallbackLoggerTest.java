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

package test.de.iip_ecosphere.platform.support.logging;

import org.junit.Test;

import de.iip_ecosphere.platform.support.logging.FallbackLogger;
import de.iip_ecosphere.platform.support.logging.LogLevel;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;

/**
 * Fallback logger tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FallbackLoggerTest {
    
    /**
     * Tests {@link FallbackLogger#abbreviate(String)}.
     */
    @Test
    public void testAbbreviate() {
        Assert.assertEquals("Class", FallbackLogger.abbreviate("Class"));
        Assert.assertEquals("a.b.c.Class", FallbackLogger.abbreviate("a.b.c.Class"));
        Assert.assertEquals("a.b.c.Class", FallbackLogger.abbreviate("alpha.beta.charly.Class"));
        Assert.assertEquals("a.Class", FallbackLogger.abbreviate("alpha.Class"));
        Assert.assertEquals(".Class", FallbackLogger.abbreviate(".Class"));
        Assert.assertEquals(".", FallbackLogger.abbreviate("."));
        Assert.assertEquals("t.d.i.p.s.l." + getClass().getSimpleName(), 
            FallbackLogger.abbreviate(getClass().getName()));
    }

    /**
     * Tests {@link FallbackLogger}.
     */
    @Test
    public void testLogger() {
        FallbackLogger logger = new FallbackLogger("cls");
        logger.setStreams(System.out, System.err);
        Assert.assertEquals("cls", logger.getName());
        logger.setLevel(LogLevel.INFO);
        Assert.assertEquals(LogLevel.INFO, logger.getLevel());
        AtomicReference<LogLevel> rcvLevel = new AtomicReference<>();
        AtomicReference<String> rcvName = new AtomicReference<>();
        AtomicReference<String> rcvMsg = new AtomicReference<>();
        AtomicReference<Throwable> rcvThrowable = new AtomicReference<>();
        logger.setEmitter((lvl, name, msg, th, out) -> {
            rcvLevel.set(lvl);
            rcvName.set(name);
            rcvMsg.set(msg);
            rcvThrowable.set(th);
        });
        
        String add = "";
        final String msg = "Startup completed.";
        logger.info("{}{}", msg, add);
        Assert.assertEquals(msg, rcvMsg.get());
        add = " Running until Ctrl-C.";
        logger.info("{}{}", msg, add);
        Assert.assertEquals(msg + add, rcvMsg.get());
    }

}
