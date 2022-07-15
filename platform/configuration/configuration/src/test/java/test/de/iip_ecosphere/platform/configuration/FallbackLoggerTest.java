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

package test.de.iip_ecosphere.platform.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;

import de.iip_ecosphere.platform.configuration.FallbackLogger;
import de.iip_ecosphere.platform.configuration.FallbackLogger.LoggingLevel;

/**
 * Tests {@link FallbackLogger}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FallbackLoggerTest {
    
    /**
     * Tests that there is a logger.
     */
    @Test
    public void testInstantiation() {
        Assert.assertNotNull(FallbackLogger.getLogger(null, getClass(), FallbackLogger.LoggingLevel.ERROR));
    }

    /**
     * Tests the logger. No asserts, just shall not throw exceptions.
     */
    public void testLogger() {
        FallbackLogger logger = new FallbackLogger(LoggingLevel.ERROR);
        logger.isDebugEnabled();
        logger.debug("msg");
        logger.debug("msg", new Throwable());
        logger.debug("msg {}", "X");
        logger.debug("msg {} {}", "X", 1);
        logger.debug("msg {} {} {}", "X", 1, "Y");
        
        Marker marker = null; // cannot create, does not matter for now
        logger.isDebugEnabled(marker);
        logger.debug(marker, "msg");
        logger.debug(marker, "msg", new Throwable());
        logger.debug(marker, "msg {}", "X");
        logger.debug(marker, "msg {} {}", "X", 1);
        logger.debug(marker, "msg {} {} {}", "X", 1, "Y");
        
        logger.isTraceEnabled();
        logger.trace("msg");
        logger.trace("msg", new Throwable());
        logger.trace("msg {}", "X");
        logger.trace("msg {} {}", "X", 1);
        logger.trace("msg {} {} {}", "X", 1, "Y");
        
        logger.isTraceEnabled(marker);
        logger.trace(marker, "msg");
        logger.trace(marker, "msg", new Throwable());
        logger.trace(marker, "msg {}", "X");
        logger.trace(marker, "msg {} {}", "X", 1);
        logger.trace(marker, "msg {} {} {}", "X", 1, "Y");
        
        logger.isInfoEnabled();
        logger.info("msg");
        logger.info("msg", new Throwable());
        logger.info("msg {}", "X");
        logger.info("msg {} {}", "X", 1);
        logger.info("msg {} {} {}", "X", 1, "Y");
        
        logger.isInfoEnabled(marker);
        logger.info(marker, "msg");
        logger.info(marker, "msg", new Throwable());
        logger.info(marker, "msg {}", "X");
        logger.info(marker, "msg {} {}", "X", 1);
        logger.info(marker, "msg {} {} {}", "X", 1, "Y");

        logger.isWarnEnabled();
        logger.warn("msg");
        logger.warn("msg", new Throwable());
        logger.warn("msg {}", "X");
        logger.warn("msg {} {}", "X", 1);
        logger.warn("msg {} {} {}", "X", 1, "Y");
        
        logger.isWarnEnabled(marker);
        logger.warn(marker, "msg");
        logger.warn(marker, "msg", new Throwable());
        logger.warn(marker, "msg {}", "X");
        logger.warn(marker, "msg {} {}", "X", 1);
        logger.warn(marker, "msg {} {} {}", "X", 1, "Y");

        logger.isErrorEnabled();
        logger.error("msg");
        logger.error("msg", new Throwable());
        logger.error("msg {}", "X");
        logger.error("msg {} {}", "X", 1);
        logger.error("msg {} {} {}", "X", 1, "Y");
        
        logger.isErrorEnabled(marker);
        logger.error(marker, "msg");
        logger.error(marker, "msg", new Throwable());
        logger.error(marker, "msg {}", "X");
        logger.error(marker, "msg {} {}", "X", 1);
        logger.error(marker, "msg {} {} {}", "X", 1, "Y");
    }

}
