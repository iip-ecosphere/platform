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

import test.de.iip_ecosphere.platform.support.aas.LocalInvocationTest;
import test.de.iip_ecosphere.platform.support.fakeAas.FactoryTest;
import test.de.iip_ecosphere.platform.support.fakeAas.PrintVisitorTest;
import test.de.iip_ecosphere.platform.support.net.NetworkManagerTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    FileUtilsTest.class,
    JarUtilsTest.class,
    TimeUtilsTest.class,
    CollectionUtilsTest.class,
    NetUtilsTest.class,
    SchemaServerEndpointTest.class,
    FileFormatTest.class,
    FactoryTest.class, // we do not go for a sub-suite for now as Maven may execute both
    PrintVisitorTest.class,
    NetworkManagerTest.class,
    LifecycleHandlerTest.class, 
    ServiceLoaderUtilsTest.class,
    NoOpServerTest.class,
    LocalInvocationTest.class
})
public class AllTests {
}
