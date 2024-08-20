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
package test.de.iip_ecosphere.platform.configuration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.configuration.opcua.DomParserTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestConfigurerTest.class,
    IvmlMetaModelTests.class,
    IvmlKodexMeshTests.class,
    IvmlSimpleMeshTests.class,
    IvmlSimpleMesh3Tests.class,
    //IvmlSerializerConfig1Tests.class, // -> examples to allow for platform/mangementUi build before
    IvmlSerializerConfig1OldTests.class,
    IvmlRoutingTestTests.class,
    IvmlContainerTests.class,
    //IvmlContainerLxcTests.class,  // -> examples to allow for platform/mangementUi build before
    StorageFactoryTest.class,
    FallbackLoggerTest.class,
    IvmlApiTests.class,
    
    DataflowGraphFormatTest.class,
    AasIvmlMapperTest.class,
    CommentTests.class,
    
    DomParserTest.class
})
public class AllTests {
}
