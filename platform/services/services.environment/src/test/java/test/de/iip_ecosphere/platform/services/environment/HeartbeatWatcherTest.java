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

package test.de.iip_ecosphere.platform.services.environment;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.HeartbeatWatcher;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * Tests {@link HeartbeatWatcher}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HeartbeatWatcherTest {

    /**
     * Asserts that there are reception callbacks.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testReceptionCallback() throws IOException {
        HeartbeatWatcher watcher = new HeartbeatWatcher();
        Assert.assertNotNull(watcher.createMetricsReceptionCallback());
        Assert.assertNotNull(watcher.createStatusReceptionCallback());
       
        TransportConnector conn = TransportFactory.createDirectMemoryConnector();
        watcher.installInto(conn);
        watcher.uninstallFrom(conn);
    }
    
    /**
     * Asserts the watcher functionality.
     */
    @Test
    public void testWatcher() {
        HeartbeatWatcher watcher = new HeartbeatWatcher();
        long orig = watcher.setTimeout(1000);
        final String dev1 = "0ab1234c1";
        final String dev2 = "0ab2234c3";
        
        watcher.notifyRecordReceived(dev1);
        Assert.assertEquals(1, watcher.getDeviceCount());
        TimeUtils.sleep(200);
        watcher.notifyRecordReceived(dev2);
        Assert.assertEquals(2, watcher.getDeviceCount());
        TimeUtils.sleep(200);
        watcher.notifyRecordReceived(dev2);
        Assert.assertEquals(2, watcher.getDeviceCount());
        TimeUtils.sleep(200);
        watcher.notifyRecordReceived(dev2);
        TimeUtils.sleep(200);
        watcher.notifyRecordReceived(dev2);
        TimeUtils.sleep(200);
        watcher.notifyRecordReceived(dev2);
        TimeUtils.sleep(200);
        Assert.assertEquals(2, watcher.getDeviceCount());
        
        // now the timeout shall be reached
        watcher.deleteOutdated(d -> {
            Assert.assertEquals(dev1, d);
        });
        // dev1 deleted
        Assert.assertEquals(1, watcher.getDeviceCount());
        watcher.clear();
        Assert.assertEquals(0, watcher.getDeviceCount());
       
        watcher.setTimeout(orig);
    }

}
