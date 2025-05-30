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
package test.de.iip_ecosphere.platform.support.aas.basyx2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.support.aas.TechnicalDataSubmodelTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ToolTests.class,
    BaSyxTest.class,
    BaSyxPersistenceTest.class,
    BaSyxDeploymentTest.class,
    BaSyxCodecTest.class,
    TechnicalDataSubmodelTest.class,
    
    BaSyxXmasAas.class,
    BaSyxTimeSeriesDataTest.class,
    BaSyxPCF.class,
    BaSyxContactInformationsTest.class,
    BaSyxSoftwareNameplateTest.class
})
public class AllTests {
}
