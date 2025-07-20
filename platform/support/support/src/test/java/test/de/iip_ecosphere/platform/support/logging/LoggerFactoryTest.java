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

import de.iip_ecosphere.platform.support.logging.LogLevel;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import org.junit.Assert;

/**
 * Tests {@link LoggerFactory} with fallback logger.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LoggerFactoryTest {

    /**
     * Tests the basic factory functions.
     */
    @Test
    public void testFactory() {
        Logger l = LoggerFactory.getLogger(getClass());
        Logger l1 = LoggerFactory.getLogger(this);
        Assert.assertTrue(l == l1);

        l1 = LoggerFactory.getLogger((Object) getClass());
        Assert.assertTrue(l == l1);

        l1 = LoggerFactory.getLogger(LoggerFactoryTest.class);
        Assert.assertTrue(l == l1);

        l1 = LoggerFactory.getLogger(LoggerFactoryTest.class.getName());
        Assert.assertTrue(l == l1);
    }

    /**
     * Tests with logging level {@link LogLevel#ALL}.
     */
    @Test
    public void testAll() {
        log(LogLevel.ALL);
    }

    /**
     * Tests with logging level {@link LogLevel#TRACE}.
     */
    @Test
    public void testTrace() {
        log(LogLevel.ERROR);
    }

    /**
     * Tests with logging level {@link LogLevel#OFF}.
     */
    @Test
    public void testOff() {
        log(LogLevel.OFF);
    }

    /**
     * Tests logging for the specified level.
     * 
     * @param level the level
     */
    private void log(LogLevel level) {
        Throwable t = new Throwable("JUST A TEST THROWABLE, DO NOT WORRY");
        Logger l = LoggerFactory.getLogger(this);
        l.setLevel(level);
        
        System.out.println("> Log level " + level);
        
        l.error("ERROR-MSG");
        l.error("ERROR-MSG {}", 1);
        l.error("ERROR-MSG {} {}", 1, 2);
        l.error("ERROR-MSG {} {}, {}", 1, 2, null);
        l.error("ERROR-MSG", t);

        l.warn("WARN-MSG");
        l.warn("WARN-MSG {}", 1);
        l.warn("WARN-MSG {} {}", 1, 2);
        l.warn("WARN-MSG {} {}, {}", 1, 2, "three");
        l.warn("WARN-MSG", t);

        l.info("INFO-MSG");
        l.info("INFO-MSG {}", 1);
        l.info("INFO-MSG {} {}", 1, 2);
        l.info("INFO-MSG {} {}, {}", 1, 2, "three");
        l.info("INFO-MSG", t);

        l.debug("INFO-DEBUG");
        l.debug("INFO-DEBUG {}", 1);
        l.debug("INFO-DEBUG {} {}", 1, 2);
        l.debug("INFO-DEBUG {} {}, {}", 1, 2, "three");
        l.debug("INFO-DEBUG", t);

        l.trace("INFO-TRACE");
        l.trace("INFO-TRACE {}", 1);
        l.trace("INFO-TRACE {} {}", 1, 2);
        l.trace("INFO-TRACE {} {}, {}", 1, 2, "three");
        l.trace("INFO-TRACE", t);

        System.out.println("< Log level " + level);
    }

}
