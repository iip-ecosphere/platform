/********************************************************************************
 * Copyright (c) {2022} The original author or authors
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

import test.de.iip_ecosphere.platform.support.json.JsonResultWrapperTest;
import test.de.iip_ecosphere.platform.support.logging.FallbackLoggerTest;
import test.de.iip_ecosphere.platform.support.logging.LoggerFactoryTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    IOVoidFunctionTest.class,
    CollectionUtilsTest.class,
    FileUtilsTest.class,
    ZipUtilsTest.class,
    IOUtilsTest.class,
    OsUtilsTest.class,
    TimeUtilsTest.class,
    ResourceLoaderTest.class,
    NetUtilsTest.class,
    ServerTest.class,
    YamlTest.class,
    YamlFileTest.class,
    JsonTest.class,
    TaskRegistryTests.class,
    PluginManagerTest.class,
    CommonsTest.class,
    ServiceLoaderUtilsTest.class,
    FallbackLoggerTest.class,
    LoggerFactoryTest.class,
    JsonResultWrapperTest.class,
    SchemaServerTest.class,
    DelegatingInputStreamTest.class,
    JavaUtilsTest.class,
    StreamGobblerTest.class
})
public class AllTests {
}
