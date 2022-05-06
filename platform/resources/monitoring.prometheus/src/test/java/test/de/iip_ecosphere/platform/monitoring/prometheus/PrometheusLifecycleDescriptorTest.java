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

import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.monitoring.prometheus.IipEcospherePrometheusExporter;
import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusLifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import test.de.iip_ecosphere.platform.monitoring.AbstractMonitoringReceiverTest;

/**
 * Testing {@link PrometheusLifecycleDescriptor}.
 * 
 * @author bettelsc
 * @author Holger Eichelberger, SSE
 */
public class PrometheusLifecycleDescriptorTest extends AbstractMonitoringReceiverTest {
    
    /** 
     * Tests the descriptor.
     */
    @Ignore("Broker/Tomcat conflict")
    @Test
    public void testDescriptor() {
        runScenario(new MonitoringRecieverLifecycle() {

            private PrometheusLifecycleDescriptor desc;
            
            @Override
            public void start(TransportSetup transSetup) {
                ServiceLoader<LifecycleDescriptor> loader = ServiceLoader.load(LifecycleDescriptor.class);
                Optional<PrometheusLifecycleDescriptor> pml = ServiceLoaderUtils
                    .stream(loader)
                    .filter(d-> d instanceof PrometheusLifecycleDescriptor)
                    .map(PrometheusLifecycleDescriptor.class::cast)
                    .findFirst();
                Assert.assertTrue(pml.isPresent());
                desc = pml.get();
                desc.startup(new String[] {});
                
                System.out.println("Sleeping to be on the safe side...");
                TimeUtils.sleep(3000);

                IipEcospherePrometheusExporter exp = desc.getExporter();
                Assert.assertNotNull(exp);
            }

            @Override
            public void stop() {
                desc.shutdown();
            }
            
        });
        
    }
}
