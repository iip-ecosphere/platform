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

import test.de.iip_ecosphere.platform.support.logging.FallbackLoggerTest;
import test.de.iip_ecosphere.platform.support.logging.LoggerFactoryTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ClassLoaderUtilsTest.class,
    ServiceLoaderUtilsTest.class,
    CollectionUtilsTest.class,
    NoOpServerTest.class,
    FileFormatTest.class,
    VersionTest.class,
    AbstractSetupTest.class,
    CmdLineTest.class,
    FileUtilsTest.class,
    ZipUtilsTest.class,
    OsUtilsTest.class,
    PidFileTest.class,
    TimeUtilsTest.class,
    ResourceLoaderTest.class,
    PythonUtilsTest.class,
    NetUtilsTest.class,
    SchemaServerEndpointTest.class,
    ServerTest.class,
    InstalledDependenciesSetupTest.class,
    YamlTest.class,
    YamlFileTest.class,
    JsonTest.class,
    TaskRegistryTests.class,
    CollectorTest.class,
    PluginManagerTest.class,
    
    FallbackLoggerTest.class,
    LoggerFactoryTest.class
})
public class AllTests {
}
