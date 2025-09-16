/********************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.oktoflow.platform.support.commons.apache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.support.CollectionUtilsTest;
import test.de.iip_ecosphere.platform.support.IOUtilsTest;
import test.de.iip_ecosphere.platform.support.NetUtilsTest;
import test.de.iip_ecosphere.platform.support.ObjectUtilsTest;
import test.de.iip_ecosphere.platform.support.OsUtilsTest;
import test.de.iip_ecosphere.platform.support.StringUtilsTest;
import test.de.iip_ecosphere.platform.support.TimeUtilsTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CommonsTest.class, // assert plugin
    CollectionUtilsTest.class, // this and next: common tests using this plugin 
    StringUtilsTest.class,
    FileUtilsTest.class, 
    IOUtilsTest.class,
    NetUtilsTest.class,
    ObjectUtilsTest.class,
    OsUtilsTest.class,
    TimeUtilsTest.class
})
public class AllTests {
}
