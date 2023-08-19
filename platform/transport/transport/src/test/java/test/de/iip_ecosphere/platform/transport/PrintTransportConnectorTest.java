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

package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.connectors.impl.PrintTransportConnector;
import org.junit.Assert;

/**
 * Tests {@link PrintTransportConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrintTransportConnectorTest {

    /**
     * Tests {@link PrintTransportConnector}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testPrintTransportConnector() throws IOException {
        PrintTransportConnector c = new PrintTransportConnector();
        c.asyncSend("a", "a");
        c.syncSend("a", "a");
        
        AtomicInteger count = new AtomicInteger();
        c.setTransportConsumer((s, d) -> { count.incrementAndGet(); System.out.println(s + ":" + new String(d)); });
        c.asyncSend("a", "a");
        c.syncSend("a", "a");
        Assert.assertEquals(2, count.get());
    }
    
}
