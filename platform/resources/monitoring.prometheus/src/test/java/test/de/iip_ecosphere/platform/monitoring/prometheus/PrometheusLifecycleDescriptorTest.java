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

package test.de.iip_ecosphere.platform.monitoring.prometheus;

import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.monitoring.prometheus.IipEcospherePrometheusExporter;
import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusLifecycleDescriptor;
import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusMonitoringSetup;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.streams.StreamNames;
import io.micrometer.core.instrument.Meter;
import test.de.iip_ecosphere.platform.monitoring.AbstractMonitoringReceiverTest;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer; // QPID-8588

/**
 * Testing {@link PrometheusLifecycleDescriptor}.
 * 
 * @author bettelsc
 * @author Holger Eichelberger, SSE
 */
public class PrometheusLifecycleDescriptorTest extends AbstractMonitoringReceiverTest {

    /**
     * Local exporter test mock.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyIipPrometheusExporter extends IipEcospherePrometheusExporter {
        
        private String createdExporterId;
        private Set<String> receivedMeterStreams = new HashSet<String>();
        private int statusCount;
        private int meterRecCount;
        private int meterCount;

        @Override
        protected Exporter createExporter(String id) {
            createdExporterId = id;
            return super.createExporter(id);
        }
        
        @Override
        protected void notifyMeterReception(String stream, String id, JsonObject obj) {
            super.notifyMeterReception(stream, id, obj);
            receivedMeterStreams.add(stream);
            meterRecCount++;
        }

        @Override
        protected void notifyStatusReceived(StatusMessage msg) {
            super.notifyStatusReceived(msg);
            statusCount++;
        }

        @Override
        protected void notifyMeterAdded(Meter meter) {
            super.notifyMeterAdded(meter);
            if (meter != null) {
                meterCount++;
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
            Assert.assertTrue(statusCount >= 2); // device on, service up, service down, device out
            Assert.assertTrue(meterRecCount > 0);
            Assert.assertTrue(meterCount > 0);
        }
        
    }
    
    /**
     * Implements the lifecycle for prometheus test.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyMonitoringRecieverLifecycle implements MonitoringRecieverLifecycle {

        private PrometheusLifecycleDescriptor desc;
        private MyIipPrometheusExporter exporter;
        
        @Override
        public void start(TransportSetup transSetup) {
            PrometheusMonitoringSetup.getInstance().setTransport(transSetup);
            ServiceLoader<LifecycleDescriptor> loader = ServiceLoader.load(LifecycleDescriptor.class);
            Optional<PrometheusLifecycleDescriptor> pml = ServiceLoaderUtils
                .stream(loader)
                .filter(d-> d instanceof PrometheusLifecycleDescriptor)
                .map(PrometheusLifecycleDescriptor.class::cast)
                .findFirst();
            Assert.assertTrue(pml.isPresent());
            desc = pml.get();
            exporter = new MyIipPrometheusExporter();
            desc.setExporterSupplier(() -> exporter);
            desc.startup(new String[] {});
            
            System.out.println("Sleeping to be on the safe side...");
            TimeUtils.sleep(30000);

            IipEcospherePrometheusExporter exp = desc.getExporter();
            Assert.assertNotNull(exp);
        }

        @Override
        public void stop() {
            desc.shutdown();
            exporter.doAsserts();
        }

    }
    
    /** 
     * Tests the descriptor.
     */
    @Test
    public void testDescriptor() {
        MyMonitoringRecieverLifecycle mrl = new MyMonitoringRecieverLifecycle();
        runScenario(mrl);
    }
    
    @Override
    protected Server createBroker(ServerAddress broker) {
        return new TestQpidServer(broker);
    }

    @Override
    protected int getSleepTime() {
        return 10000;
    }
    
}
