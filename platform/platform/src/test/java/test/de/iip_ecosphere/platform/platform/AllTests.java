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
package test.de.iip_ecosphere.platform.platform;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.iip_ecosphere.platform.services.environment.Starter;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PlatformTest.class,
    CliTest.class,
    ServiceDeploymentPlanTest.class,
    ServiceAasTest.class,
    PlatformAasTest.class
})
public class AllTests {
    
    /**
     * Prevent System.exit vs. shurefire.
     */
    @BeforeClass
    public static void startup() {
        System.setProperty(Starter.IIP_TEST, String.valueOf(true));
    }
    
}
