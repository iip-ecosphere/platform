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
package test.de.iip_ecosphere.platform.support;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.support.aas.ContactInformationsTest;
import test.de.iip_ecosphere.platform.support.aas.IdentifierTypeTest;
import test.de.iip_ecosphere.platform.support.aas.LangStringTest;
import test.de.iip_ecosphere.platform.support.aas.LocalInvocationTest;
import test.de.iip_ecosphere.platform.support.aas.PCF;
import test.de.iip_ecosphere.platform.support.aas.TechnicalDataSubmodelTest;
import test.de.iip_ecosphere.platform.support.aas.TimeSeriesDataTest;
import test.de.iip_ecosphere.platform.support.aas.XmasAas;
import test.de.iip_ecosphere.platform.support.fakeAas.FactoryTest;
import test.de.iip_ecosphere.platform.support.fakeAas.PrintVisitorTest;
import test.de.iip_ecosphere.platform.support.metrics.MetricsTests;
import test.de.iip_ecosphere.platform.support.net.KeyStoreDescriptorTest;
import test.de.iip_ecosphere.platform.support.net.NetworkManagerTest;
import test.de.iip_ecosphere.platform.support.net.SslUtilsTest;
import test.de.iip_ecosphere.platform.support.net.UriResolverTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    NetUtilsTest.class,
    SchemaServerEndpointTest.class,
    FactoryTest.class, // we do not go for a sub-suite for now as Maven may execute both
    PrintVisitorTest.class,
    NetworkManagerTest.class,
    UriResolverTest.class,
    SslUtilsTest.class,
    KeyStoreDescriptorTest.class,
    LifecycleHandlerTest.class, 
    ServiceLoaderUtilsTest.class,
    NoOpServerTest.class,
    LocalInvocationTest.class, 
    SystemMetricsTest.class,
    IOVoidFunctionTest.class,
    SemanticIdResolverTest.class,
    IdentifierTypeTest.class,
    
    MetricsTests.class,
    test.de.iip_ecosphere.platform.support.identities.AllTests.class,
    TechnicalDataSubmodelTest.class,
    
    // non-generic AAS 
    LangStringTest.class,
    XmasAas.class,
    TimeSeriesDataTest.class,
    PCF.class,
    ContactInformationsTest.class
})
public class AllTests {
}
