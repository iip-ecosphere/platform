/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.configuration.maven;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.maven.ProcessUnit;
import de.iip_ecosphere.platform.support.TimeUtils;

import org.junit.Assert;

/**
 * Tests {@link ProcessUnit}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessUnitTest {

    /**
     * Tests a successfully terminating process with timeout and regEx match.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testTerminatingMatchingProcess() throws IOException {
        System.out.println("Testing process with successful regex terminating on match:");
        Pattern p = Pattern.compile("^DONE: \\d+$");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("p", null)
            .addArgument("java")
            .addArgument(DummyApp.class.getName())
            .setHome(new File("./target/test-classes"))
            .setNotifyListenerByLogMatch(true)
            .addCheckRegEx(p)
            .build();
        TimeUtils.sleep(500);
        Assert.assertTrue(unit.isRunning());
        TimeUtils.sleep(3000); // runs shorter, terminated by match
        Assert.assertTrue(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("p", unit.getDescription());
        Assert.assertTrue(unit.hasCheckRegEx());
    }

    /**
     * Tests a successfully terminating process with timeout and failing regEx match.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testTerminatingNonMatchingProcess() throws IOException {
        System.out.println("Testing process with failing regex terminating itself when over:");
        Pattern p = Pattern.compile("^XYZ: \\d+$");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("p2", null)
            .addArgument("java")
            .addArgument(DummyApp.class.getName())
            .setHome(new File("./target/test-classes"))
            .setNotifyListenerByLogMatch(false)
            .addCheckRegEx(p)
            .setRegExConjunction(false) // no difference here
            .build();
        TimeUtils.sleep(500);
        Assert.assertTrue(unit.isRunning());
        TimeUtils.sleep(5000); // runs longer, not terminated by match
        Assert.assertFalse(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("p2", unit.getDescription());
        Assert.assertTrue(unit.hasCheckRegEx());
        Assert.assertEquals(0, unit.getExitValue());
    }

    /**
     * Tests a process with timeout.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testTerminatedProcess() throws IOException {
        System.out.println("Testing process being explicitly terminated:");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("", null)
            .addArgument("java")
            .addArgument(DummyApp.class.getName())
            .setHome(new File("./target/test-classes"))
            .build();
        TimeUtils.sleep(500);
        Assert.assertTrue(unit.isRunning());
        unit.stop();
        TimeUtils.sleep(1000); // shall be terminated now
        Assert.assertFalse(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("", unit.getDescription());
        Assert.assertFalse(unit.hasCheckRegEx());
    }

    /**
     * Tests a process with timeout.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testProcessMultiPattern() throws IOException {
        Pattern p = Pattern.compile("^DONE: \\d+$");
        AtomicInteger terminationCount = new AtomicInteger();
        System.out.println("Testing process with timeout and multi patterns:");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("p", null)
            .addArgument("java")
            .addArgument(DummyApp.class.getName())
            .addArgument("--modulo=2")
            .setHome(new File("./target/test-classes"))
            .setNotifyListenerByLogMatch(true)
            .addCheckRegEx(p)
            .setListener(r -> terminationCount.incrementAndGet() > 2)
            .build();
        TimeUtils.sleep(500);
        Assert.assertTrue(unit.isRunning());
        TimeUtils.sleep(3500); // shall be terminated
        Assert.assertTrue(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("p", unit.getDescription());
        Assert.assertTrue(unit.hasCheckRegEx());
        Assert.assertEquals(3, terminationCount.get());
    }
    
    /**
     * Tests a process with timeout.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testTimeoutProcess() throws IOException {
        AtomicInteger terminationCount = new AtomicInteger();
        System.out.println("Testing process with timeout:");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("p", null)
            .addArgument("java")
            .addArgument(DummyApp.class.getName())
            .setHome(new File("./target/test-classes"))
            .setTimeout(1000)
            .setListener(r -> { 
                terminationCount.incrementAndGet(); return true; })
            .build();
        TimeUtils.sleep(500);
        Assert.assertTrue(unit.isRunning());
        TimeUtils.sleep(1000); // shall be terminated
        Assert.assertFalse(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("p", unit.getDescription());
        Assert.assertFalse(unit.hasCheckRegEx());
        Assert.assertEquals(1, terminationCount.get());
    }
    

    /**
     * Tests a process started/executed by a shell script to be terminated.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testShellProcess() throws IOException {
        System.out.println("Testing process in script, terminated:");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("", null)
            .addShellScriptCommand("test")
            .addArgument("--start=2")
            .setHome(new File("./target/test-classes"))
            .build();
        TimeUtils.sleep(1500);
        Assert.assertTrue(unit.isRunning());
        unit.stop();
        TimeUtils.sleep(1000); // shall be terminated now
        Assert.assertFalse(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("", unit.getDescription());
        Assert.assertFalse(unit.hasCheckRegEx());
    }

    /**
     * Tests a maven process. Applies (not needed) additional arguments to test more complex (Windows) command lines.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testMvnProcess() throws IOException {
        System.out.println("Testing mvn process, terminated:");
        Pattern p = Pattern.compile("^.*Scanning for projects.*$");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("mvn", null)
            .addMavenCommand()
            .addArgument("-P")
            .addArgument("App")
            .addArgument("validate")
            .addArgument("-Diip.springStart.args=\"--iip.test.stop=1000 --iip.test.brokerPort=1234\"")
            .addCheckRegEx(p)
            .logTo(null)
            .build();
        Assert.assertTrue(unit.isRunning());
        unit.waitFor(); // exit value may differ :/
        Assert.assertTrue(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("mvn", unit.getDescription());
        Assert.assertTrue(unit.hasCheckRegEx());
    }

    /**
     * Tests a maven process.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testMvnProcess2() throws IOException {
        System.out.println("Testing mvn process, terminated:");
        Pattern p = Pattern.compile("^.*gen.*$");
        ProcessUnit unit = new ProcessUnit.ProcessUnitBuilder("mvn", null)
            .addMavenCommand()
            .addArgument(true, "exec:java@app")
            .addArgument(false, "xyz") // disabled, shall not occur
            .addArgument("-Diip.springStart.args=\"--iip.test.stop=1000 --iip.test.brokerPort=1234\"")
            .addCheckRegEx(p)
            .logTo(null)
            .build();
        Assert.assertTrue(unit.isRunning());
        unit.waitFor(); // exit value may differ :/
        Assert.assertTrue(unit.getLogMatches());
        Assert.assertFalse(unit.isRunning());
        Assert.assertEquals("mvn", unit.getDescription());
        Assert.assertTrue(unit.hasCheckRegEx());
    }

}
