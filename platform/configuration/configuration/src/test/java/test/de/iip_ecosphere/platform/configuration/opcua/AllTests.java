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

package test.de.iip_ecosphere.platform.configuration.opcua;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for the OPC/IVML translator.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ObjectTypeTest.class,
    DomParserTest.class
})
public class AllTests {
}