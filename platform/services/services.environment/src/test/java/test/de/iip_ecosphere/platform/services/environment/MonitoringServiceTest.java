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

import java.net.URI;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.MonitoringService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.UpdatingMonitoringService;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link MonitoringService}/{@link UpdatingMonitoringService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoringServiceTest {
    
    /**
     * Test service.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyService extends AbstractService {

        /**
         * Creates a service.
         */
        protected MyService() {
            super(ServiceKind.PROBE_SERVICE);
        }

        @Override
        public void migrate(String resourceId) throws ExecutionException {
        }

        @Override
        public void update(URI location) throws ExecutionException {
        }

        @Override
        public void switchTo(String targetId) throws ExecutionException {
        }
        
    }

    /**
     * Test monitoring service.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyMonitoringService extends MyService implements MonitoringService {

        protected MetricsProvider provider;
        
        @Override
        public void attachMetricsProvider(MetricsProvider provider) {
            this.provider = provider;
        }
        
    }

    /**
     * Test updating monitoring service.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyUpdatingMonitoringService extends MyMonitoringService implements UpdatingMonitoringService {

        private boolean called = false;
        
        @Override
        public void calculateMetrics() {
            called = true;
        }
        
    }

    /**
     * Tests {@link MonitoringService#setUp(de.iip_ecosphere.platform.services.environment.Service, MetricsProvider)}.
     */
    @Test
    public void testSetup() {
        MonitoringService.setUp(null, null);
        
        MetricsProvider prov = new MetricsProvider(new SimpleMeterRegistry());
        MonitoringService.setUp(null, prov);
        
        MyService service = new MyService();
        MonitoringService.setUp(service, prov);
        
        MyMonitoringService mService = new MyMonitoringService();
        MonitoringService.setUp(mService, prov);

        MyUpdatingMonitoringService uService = new MyUpdatingMonitoringService();
        MonitoringService.setUp(uService, prov);

        Assert.assertTrue(mService.provider == prov);
        Assert.assertTrue(uService.provider == prov);
        
        prov.calculateMetrics();
        Assert.assertTrue(uService.called);
    }

}
