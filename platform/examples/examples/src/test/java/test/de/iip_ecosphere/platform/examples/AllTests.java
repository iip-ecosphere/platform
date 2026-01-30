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
package test.de.iip_ecosphere.platform.examples;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.configuration.easyProducer.AbstractIvmlTests;
import test.de.iip_ecosphere.platform.configuration.easyProducer.IvmlContainerTests;
import test.de.iip_ecosphere.platform.configuration.easyProducer.IvmlSerializerConfig1Tests;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PersistentLocalNetworkManagerTest.class,
    SpringStartupTest.class,

    // from configuration.configuration -> to allow for platform/mangementUi build before
    IvmlSerializerConfig1Tests.class,
    IvmlContainerTests.class,
    //IvmlContainerLxcTests.class, // snap installation/cleanup difficult

    ConfigurationTests.class
})
public class AllTests {

    /**
     * Initializes the test suite.
     */
    @BeforeClass
    public static void init() {
        AbstractIvmlTests.setTestMetaModelFolder(new File("./target/easy"));
        AbstractIvmlTests.setTestModelBase(new File("./target/easy-test"));
        System.out.println("Sets test model base to target/easy-test and meta model folder to target/easy");
    }
    
}
