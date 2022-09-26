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

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.TransportLogger;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.transport.streams.StreamNames;
import org.junit.Assert;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Tests {@link TransportLogger}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportLoggerTests {

    /**
     * Tests {@link TransportLogger}.
     */
    @Test
    public void testLogger() {
        final File out = new File(FileUtils.getTempDirectory(), "iip-logger.txt");
        FileUtils.deleteQuietly(out);
        final String outFile = "--outFile=" + out.getAbsolutePath();
        final String setupFile = "--setupFile=./src/test/resources/envSetup.yml";

        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        TestQpidServer qpid = new TestQpidServer(broker);
        qpid.start();

        AtomicInteger loopCount = new AtomicInteger(0);
        AtomicInteger receivedCount = new AtomicInteger(0);
        TransportLogger.setLoopEndSupplier(() -> {
            // during "main program" loop - send data, wait, terminate
            if (loopCount.get() == 1) {
                sendTestData();
            }
            if (loopCount.get() < 10) {
                loopCount.incrementAndGet();
                return true;
            } else {
                loopCount.set(0);
                return false;
            }
        });
        TransportLogger.setSetupCustomizer(s -> s.getTransport().setPort(broker.getPort()));
        TransportLogger.setReceptionConsumer((c, t) -> receivedCount.incrementAndGet());
        TransportLogger.setShutdownRunnable(() -> { }); // do nothing, conflicts with explicit shutdown

        TransportLogger.main(new String[]{setupFile});
        Assert.assertEquals(0, receivedCount.getAndSet(0));
        Assert.assertFalse(out.exists());

        TransportLogger.main(new String[]{setupFile, outFile, "--traces=true"});
        Assert.assertEquals(1, receivedCount.getAndSet(0));
        Assert.assertTrue(out.exists());
        Assert.assertTrue(out.length() > 0);
        FileUtils.deleteQuietly(out);

        TransportLogger.main(new String[]{setupFile, outFile, "--traces=true", "--metrics=true", "--status=true"});
        Assert.assertEquals(3, receivedCount.getAndSet(0));
        Assert.assertTrue(out.exists());
        Assert.assertTrue(out.length() > 0);
        FileUtils.deleteQuietly(out);

        TransportLogger.shutdown();
        qpid.stop(true);
    }
    
    /**
     * Sends test data instances.
     */
    private static void sendTestData() {
        Transport.sendContainerStatus(ActionTypes.ADDED, "myContainer");
        Transport.sendTraceRecord(new TraceRecord("src", "act", "")); // payload needs serializer
        Transport.send(s -> s.asyncSend(StreamNames.RESOURCE_METRICS, "{}"), "metrics");
    }
    
}
