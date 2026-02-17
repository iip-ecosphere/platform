/********************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.support.aas.basyx2.plugintest;

import org.junit.runner.RunWith;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import junit.framework.TestSuite;
import test.de.iip_ecosphere.platform.support.TestUtils;
import static test.de.iip_ecosphere.platform.support.aas.TestWithPlugin.*;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(org.junit.runners.AllTests.class)
public class AllTests {
    
    static {
        enableLocalPlugins(false); // we need them relocated and appended/mixed
        setInstallDir("target/plugins");
        setupAASPlugins();
        addPluginLocation("support", "support.log-slf4j-simple");
        addPluginLocation("support", "support.processInfo-oshi");
        addPluginLocation("support", "support.websocket-websocket");
        addPluginLocation("support", "support.yaml-snakeyaml");
        addPluginLocation("support", "support.commons-apache");
        addPluginLocation("support", "support.bytecode-bytebuddy");
        addPluginLocation("support", "support.json-jackson");
        addPluginLocation("support", "support.rest-spark");
        addPluginLocation("support", "support.aas.basyx2");
        addPluginLocation("support/support.aas.basyx2", "support.aas.basyx2.server");
        loadPlugins();
    }
    
    /**
     * Creates the test suite to execute, called by jUnit.
     * 
     * @return the test suite
     */
    public static TestSuite suite() {
        return TestUtils.suite(AasFactory.getInstance().getTests(0));
    }

}
