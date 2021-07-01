/********************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.services.environment.metricsProvider;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentations.CounterRepresentationTest;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentations.GaugeRepresentationTest;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentations.MeterRepresentationTest;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentations.TimerRepresentationTest;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructionBundleTest;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClientTest;

/**
 * Defines the tests to be executed for the metrics provider.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CapacityBaseUnitTest.class,
    
    CounterRepresentationTest.class,
    GaugeRepresentationTest.class,
    MeterRepresentationTest.class,
    TimerRepresentationTest.class, 
    
    MetricsProviderTest.class,
    
    MetricsExtractorRestClientTest.class,
    MetricsAasConstructionBundleTest.class
})
public class MetricsProviderTests {
}
