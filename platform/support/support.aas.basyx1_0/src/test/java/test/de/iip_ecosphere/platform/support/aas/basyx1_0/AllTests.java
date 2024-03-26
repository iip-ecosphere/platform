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
package test.de.iip_ecosphere.platform.support.aas.basyx1_0;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.basyx1_0.BaSyxAasFactory;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
public class AllTests extends test.de.iip_ecosphere.platform.support.aas.basyx.AllTests {
    
    private static AasFactory factory;
    
    /**
     * Set fixed to our factory.
     */
    @BeforeClass
    public static void beforeTests() {
        factory = AasFactory.setInstance(new BaSyxAasFactory()); // pluginId?
    }

    /**
     * Set fixed to our factory.
     */
    @AfterClass
    public static void afterTests() {
        AasFactory.setInstance(factory);
    }

}
