/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.spring.metricsProvider.metricsAas;

import static org.junit.Assert.assertNotNull;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;

import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.metricsProvider.metricsAas.MetricsExtractorRestClient;

/**
 * Tests creating a metrics extractor client.
 * 
 * @author Miguel Gomez
 */
public class MetricsExtractorRestClientTest {

    /**
     * Tests {@link MetricsExtractorRestClient#MetricsExtractorRestClient(String, int)}.
     */
    @Test
    public void testInitOk() {
        MetricsExtractorRestClient client = new MetricsExtractorRestClient("localhost", 8080);
        assertNotNull(client);
    }

    /**
     * Tests {@link MetricsExtractorRestClient#MetricsExtractorRestClient(String, int)}.
     */
    @Test
    public void testInitHostIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new MetricsExtractorRestClient(null, 8080));
    }

    /**
     * Tests {@link MetricsExtractorRestClient#MetricsExtractorRestClient(String, int)}.
     */
    @Test
    public void testInitHostIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new MetricsExtractorRestClient("", 8080));
    }

    /**
     * Tests {@link MetricsExtractorRestClient#MetricsExtractorRestClient(String, int)}.
     */
    @Test
    public void testInitHostPortIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new MetricsExtractorRestClient("localhost", -8080));
    }
}
