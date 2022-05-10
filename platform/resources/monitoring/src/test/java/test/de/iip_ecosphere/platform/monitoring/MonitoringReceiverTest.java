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

package test.de.iip_ecosphere.platform.monitoring;

import java.util.HashSet;
import java.util.Set;

import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.monitoring.MonitoringReceiver;
import de.iip_ecosphere.platform.monitoring.MonitoringSetup;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.streams.StreamNames;
import io.micrometer.core.instrument.Meter;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Tests {@link MonitoringReceiver}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoringReceiverTest extends AbstractMonitoringReceiverTest {

    /**
     * A simple receiver for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class MyReceiver extends MonitoringReceiver {

        private String createdExporterId;
        private Set<String> receivedMeterStreams = new HashSet<String>();
        private int statusCount;
        private int meterRecCount;
        private int meterCount;
        private int meterCount2;

        /**
         * A simple exporter for testing.
         * 
         * @author Holger Eichelberger, SSE
         */
        private class MyExporter extends Exporter {

            /**
             * Creates an exporter instance.
             * 
             * @param id the monitored object id
             */
            protected MyExporter(String id) {
                super(id);
                createdExporterId = id;
            }

            @Override
            protected void initialize() {
            }

            @Override
            protected void addMeter(Meter meter) {
                if (meter != null) {
                    meterCount++;
                }
            }
            
        }
        
        @Override
        protected Exporter createExporter(String id) {
            return new MyExporter(id);
        }

        @Override
        protected void notifyMeterReception(String stream, String id, JsonObject obj) {
            receivedMeterStreams.add(stream);
            meterRecCount++;
        }

        @Override
        protected void notifyStatusReceived(StatusMessage msg) {
            statusCount++;
        }

        @Override
        protected void notifyMeterAdded(Meter meter) {
            if (meter != null) {
                meterCount2++;
            }
        }

        /**
         * Does the asserts.
         */
        protected void doAsserts() {
            String deviceId = Id.getDeviceId();
            Assert.assertEquals(deviceId, createdExporterId);
            Assert.assertTrue(receivedMeterStreams.contains(StreamNames.RESOURCE_METRICS));
            Assert.assertTrue(receivedMeterStreams.contains(StreamNames.SERVICE_METRICS));
            Assert.assertTrue(statusCount >= 4); // device on, service up, service down, device out
            Assert.assertTrue(meterRecCount > 0);
            Assert.assertTrue(meterCount > 0);
            Assert.assertTrue(meterCount2 > 0);
        }
        
    }

    
    /**
     * Simple internal monitoring receiver lifecycle.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class MyMonitoringReceiverLifecycle implements MonitoringRecieverLifecycle {

        private MyReceiver receiver;
         
        @Override
        public void start(TransportSetup transSetup) {
            MonitoringSetup.getInstance().setTransport(transSetup);
            receiver = new MyReceiver();
            receiver.start();
        }

        @Override
        public void stop() {
            receiver.stop();
        }
        
        /**
         * Returns the receiver instance.
         * 
         * @return the instance
         */
        protected MyReceiver getInstance() {
            return receiver;
        }
        
    }

    @Override
    protected Server createBroker(ServerAddress broker) {
        return new TestQpidServer(broker);
    }
    
    /**
     * Tests the receiver.
     */
    @Test
    public void testReceiver() {
        MyMonitoringReceiverLifecycle mrl = new MyMonitoringReceiverLifecycle();
        runScenario(mrl);
        mrl.getInstance().doAsserts();
    }

}
