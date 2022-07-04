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
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
    private static final Consumer<String> SIMPLE_RECEIVED_ASSERTER = s -> assertContains(s, "RECEIVED ");
    private static final boolean DEBUG = false;

    /**
     * Configures the process builder {@code pb} for output handling depending on {@link #DEBUG}.
     * 
     * @param pb the process builder
     * @param res the file to redirect the output to if {@link #DEBUG} is false. 
     */
    private static final void configureProcess(ProcessBuilder pb, File res) {
        if (DEBUG) {
            pb.inheritIO(); 
        } else {
            pb.redirectOutput(res);
        }
    }
        
    /**
     * Asserts whether {@code output} contains {@code test}.
     * 
     * @param output the output to test
     * @param test the test string to search within {@code output}
     */
    private static final void assertContains(String output, String test) {
        Assert.assertTrue("Output does not contain '" + test + "'", output.indexOf(test) > 0);
    }

    /**
     * Asserts whether {@code output} matches {@code regEx}.
     * 
     * @param output the output to test
     * @param regEx the regular expression to match within {@code output}
     */
    private static final void assertMatches(String output, String regEx) {
        Pattern p = Pattern.compile(regEx, Pattern.DOTALL | Pattern.MULTILINE);
        Assert.assertTrue("Output does not match '" + regEx + "'", p.matcher(output).matches());
    }

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
     * @param asserter asserts on the output log
     * @throws IOException if any I/O reading problem occurs
     */
    private void testInstantiatedExample(String folder, String appName, int stopTime, Consumer<String> asserter) 
        throws IOException {
        File cfg = new File("../../configuration/configuration/gen/tests"); // git
        if (!cfg.exists()) {
            cfg = new File("../IIP_configuration.configuration/gen/tests"); // Jenkins
        }
        Assert.assertTrue("configuration.configuration must be built before", cfg.exists());
        File f = new File(cfg, folder + "/" + appName + "/target/" + appName + "-0.1.0-SNAPSHOT-bin.jar");
        File res = File.createTempFile("examples-test", ".out");
        res.deleteOnExit();
        SpringStartup.start(f, false, p -> configureProcess(p, res), 
            "--iip.test.stop=" + stopTime, "--iip.test.brokerPort=" + broker.getPort());
        asserter.accept(FileUtils.readFileAsString(res));
        res.delete();
    }

    /**
     * Tests the simple mesh example from configuration.configuration.
     * 
     * @throws IOException if any I/O problem occurs
     */
    @Test
    public void testSimpleMesh() throws IOException {
        testInstantiatedExample("SimpleMesh", "SimpleMeshTestingApp", 15000, SIMPLE_RECEIVED_ASSERTER);
    }

    /**
     * Tests the simple mesh 3 example from configuration.configuration.
     * 
     * @throws IOException if any I/O problem occurs
     */
    @Test
    public void testSimpleMesh3() throws IOException {
        testInstantiatedExample("SimpleMesh3", "SimpleMeshTestingApp3", 25000, SIMPLE_RECEIVED_ASSERTER);
    }
    
    /**
     * Tests the routing test from configuration.configuration.
     * 
     * @throws IOException if any I/O problem occurs
     */
    @Ignore("Temporarily disabled for building")
    @Test
    public void testRoutingTest() throws IOException {
        testInstantiatedExample("RoutingTest", "RoutingTestApp", 25000, s -> {
            assertContains(s, "RECEIVED: RoutingTestDataImpl["); // in sink regardless if TestData or ConnOut
            assertMatches(s, ".*RECEIVED: RoutingTestDataImpl\\[.* - P1\\].*"); 
            assertMatches(s, ".*RECEIVED: RoutingTestDataImpl\\[.* - P2\\].*"); 
            assertContains(s, "Processor received: RoutingConnOutImpl["); // ConnOut in processor
            assertContains(s, "Processor sent: RoutingTestDataImpl["); // TestData in processor
            
            // Commands sent backwards
            assertContains(s, "Source received cmd: RoutingCommandImpl[cmd=Batch completed]");
            assertContains(s, "Processor received cmd: RoutingCommandImpl[cmd=Batch completed]");
            assertContains(s, "Connector received cmd: RoutingCommandImpl[cmd=Batch completed]");
            assertContains(s, "Processor P1 received cmd: RoutingCommandImpl[cmd=Batch completed]");
            assertContains(s, "Processor P2 received cmd: RoutingCommandImpl[cmd=Batch completed]");
        });
    }

}
