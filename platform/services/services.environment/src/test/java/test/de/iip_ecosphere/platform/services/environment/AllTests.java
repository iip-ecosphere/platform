/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.services.environment;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProviderTests;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ServiceStateTest.class,
    ServiceTest.class,
    AasTest.class,
    YamlTest.class, 
    StreamTest.class,
    JavaEnvironmentTest.class,
    PythonEnvironmentTest.class,
    PythonEnvironmentSuiteTest.class,
    StarterTest.class,
    ProcessServiceTest.class,
    ReconfigureTest.class,
    PythonProcessServiceTest.class,
    InstalledDependenciesSetupTest.class,
    ServiceSelectorTest.class,
    IipStringStyleTest.class,
    ProcessSupportTest.class,
    DataMapperTest.class,
    MonitoringServiceTest.class,
    ConnectorServiceWrapperTest.class,
    HeartbeatWatcherTest.class,
    DefaultServiceImplTest.class,
    
    MetricsProviderTests.class,
    test.de.iip_ecosphere.platform.services.environment.services.AllTests.class
})
public class AllTests {
}
