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

package test.de.iip_ecosphere.platform.services.environment;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.DataIngestors;

/**
 * Tests {@link DataIngestors}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataIngestorsTest {
    
    /**
     * Tests {@link DataIngestors}.
     */
    @Test
    public void testIngestors() {
        List<String> received = new ArrayList<>();
        DataIngestors<String> ingestors = new DataIngestors<>();
        ingestors.attachIngestor(s -> received.add(s));
        ingestors.ingest(null);
        Assert.assertEquals(0, received.size());
        ingestors.ingest("a");
        Assert.assertEquals(1, received.size());
        Assert.assertTrue(received.contains("a"));
    }

}
