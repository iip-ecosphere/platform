/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.SharedBuffer;
import org.junit.Assert;

/**
 * Tests {@link SharedBuffer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SharedBufferTest {

    /**
     * Tests {@link SharedBuffer}.
     *  
     * @throws InterruptedException shall not occur
     */
    @Test
    public void testSharedBuffer() throws InterruptedException {
        // just a synchronous very simple test for now
        SharedBuffer<String> buffer = new SharedBuffer<>(10);
        buffer.clear();
        buffer.offer("abc");
        String value = buffer.poll();
        Assert.assertEquals("abc", value);
    }

}
