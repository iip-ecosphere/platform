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

package test.de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.metrics.MetricsFactory;
import de.iip_ecosphere.platform.support.metrics.Statistic;
import de.iip_ecosphere.platform.support.rest.Rest;
import test.de.iip_ecosphere.platform.support.metrics.TestMetrics;

/**
 * Tests {@link Rest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MetricsTest {
    
    /**
     * Tests basic metrics functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testMetrics() throws IOException {
        // just the very basic
        MetricsFactory fac = MetricsFactory.getInstance();
        Assert.assertTrue(fac instanceof TestMetrics);
        MetricsFactory.setInstance(fac);

        MetricsFactory.buildCounter("");
        MetricsFactory.buildGauge("", () -> 0);
        MetricsFactory.buildTimer("");
        AtomicInteger gVal = new AtomicInteger(5);
        MetricsFactory.buildGauge("myGauge", gVal, v -> v.doubleValue());
        MetricsFactory.acceptNameStartsWith("");
        MetricsFactory.deny();
        MetricsFactory.denyNameStartsWith("");
        MetricsFactory.buildId("", null, null, null, null);
        MetricsFactory.buildImmutableTag("", "");
        MetricsFactory.buildTag("", "");
        MetricsFactory.buildMeasurement(() -> 0.0, Statistic.VALUE);
    }

}
