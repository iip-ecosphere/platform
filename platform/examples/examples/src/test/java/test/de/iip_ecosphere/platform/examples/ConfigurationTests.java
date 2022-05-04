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

package test.de.iip_ecosphere.platform.examples;

import java.io.File;
import java.io.IOException;

import org.apache.qpid.server.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.examples.SpringStartup;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Executes tests on runnable configuration.configuration examples.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationTests {

    private static ServerAddress broker;
    private static TestQpidServer server;

    /**
     * Starts the broker.
     */
    @BeforeClass
    public static void startup() {
        broker = new ServerAddress(Schema.IGNORE);
        server = new TestQpidServer(broker);
        server.start();
    }

    /**
     * Stops the broker.
     */
    @AfterClass
    public static void shutdown() {
        server.stop(true);
    }

    /**
     * Tests an instantiated example.
     * 
     * @param folder the example folder in gen
     * @param appName the app name/artifact name without version
     * @param stopTime the time to stop the application/test
     * @throws IOException if any I/O reading problem occurs
     */
    private void testInstantiatedExample(String folder, String appName, int stopTime) throws IOException {
        File cfg = new File("../../configuration/configuration/gen/tests"); // git
        if (!cfg.exists()) {
            cfg = new File("../IIP_configuration.configuration/gen/tests"); // Jenkins
        }
        Assert.assertTrue("configuration.configuration must be built before", cfg.exists());
        File f = new File(cfg, folder + "/" + appName + "/target/" + appName + "-0.1.0-SNAPSHOT-bin.jar");
        File res = File.createTempFile("examples-test", ".out");
        res.deleteOnExit();
        SpringStartup.start(f, false, p -> p.redirectOutput(res), 
            "--iip.test.stop=" + stopTime, "--iip.test.brokerPort=" + broker.getPort());
        String procOut = FileUtils.readFileAsString(res);
        Assert.assertTrue(procOut.indexOf("RECEIVED ") > 0);
        res.delete();
    }

    /**
     * Tests the simple mesh example from configuration.configuration.
     * 
     * @throws IOException if any I/O problem occurs
     */
    @Test
    public void testSimpleMesh() throws IOException {
        testInstantiatedExample("SimpleMesh", "SimpleMeshTestingApp", 15000);
    }

    /**
     * Tests the simple mesh 3 example from configuration.configuration.
     * 
     * @throws IOException if any I/O problem occurs
     */
    @Ignore("Sync source")
    @Test
    public void testSimpleMesh3() throws IOException {
        testInstantiatedExample("SimpleMesh3", "SimpleMeshTestingApp3", 25000);
    }

}
