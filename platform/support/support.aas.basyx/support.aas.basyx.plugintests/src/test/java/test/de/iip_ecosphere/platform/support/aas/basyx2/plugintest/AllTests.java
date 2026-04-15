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
import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import junit.framework.TestSuite;
import test.de.iip_ecosphere.platform.support.TestUtils;
import test.de.iip_ecosphere.platform.support.aas.TestWithPluginSetup;

import static test.de.iip_ecosphere.platform.support.aas.TestWithPlugin.*;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(org.junit.runners.AllTests.class)
public class AllTests {
    
    static {
        if (Boolean.valueOf(System.getProperty("okto.test.noPlugins", "false"))) {
            PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE);
        } else {
            enableLocalPlugins(false); // we need them relocated and appended/mixed
            setInstallDir("target/plugins");
            TestWithPluginSetup.setBasyx13AasPluginId();
            TestWithPluginSetup.setWithBasyx2(false);
            TestWithPluginSetup.setWithBasyx(true);
            TestWithPluginSetup.setWithBasyxServer(false);
            setupAASPlugins();
            addPluginLocation("support", "support.log-slf4j-simple");
            addPluginLocation("support", "support.processInfo-oshi");
            addPluginLocation("support", "support.websocket-websocket");
            addPluginLocation("support", "support.bytecode-bytebuddy");
            loadPlugins(false);
        }
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
