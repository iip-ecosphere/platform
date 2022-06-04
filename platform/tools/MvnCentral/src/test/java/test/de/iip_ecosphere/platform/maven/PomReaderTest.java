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

package test.de.iip_ecosphere.platform.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.maven.PomReader;
import de.iip_ecosphere.platform.maven.PomReader.PomInfo;

/**
 * Tests {@link PomReader}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PomReaderTest {

    /**
     * Test no-POM and non-existing file.
     */
    @Test
    public void testNoPom() {
        Assert.assertNull(PomReader.getInfo(new File("src/test/resources/noPom.xml")));
        Assert.assertNull(PomReader.getInfo(new File("src/test/resources/x.xml")));
    }

    /**
     * Test main POM.
     */
    @Test
    public void testMainPom() {
        PomInfo info = PomReader.getInfo(new File("pom.xml"));
        Assert.assertNotNull(info);
        Assert.assertTrue(info.getArtifactId().length() > 0);
        Assert.assertTrue(info.getGroupId().length() > 0);
        Assert.assertTrue(info.getVersion().length() > 0);
        Assert.assertTrue(info.getGroupPath().length() > 0);
        Assert.assertNull(info.getParentArtifactId());
        Assert.assertNull(info.getParentGroupId());
        Assert.assertNull(info.getParentVersion());
    }

    /**
     * Test test POM.
     */
    @Test
    public void testTestPom() {
        PomInfo info = PomReader.getInfo(new File("src/test/resources/testPom.xml"));
        Assert.assertNotNull(info);
        Assert.assertNotNull(info.getParentArtifactId());
        Assert.assertNotNull(info.getParentGroupId());
        Assert.assertNotNull(info.getParentVersion());
        Assert.assertTrue(info.getArtifactId().length() > 0);
        Assert.assertEquals(info.getParentGroupId(), info.getGroupId());
        Assert.assertEquals(info.getParentVersion(), info.getVersion());
        Assert.assertTrue(info.getGroupPath().length() > 0);
    }

    /**
     * Test test POM2.
     */
    @Test
    public void testTestPom2() {
        PomInfo info = PomReader.getInfo(new File("src/test/resources/testPom2.xml"));
        Assert.assertNotNull(info);
        Assert.assertNotNull(info.getParentArtifactId());
        Assert.assertNotNull(info.getParentGroupId());
        Assert.assertNotNull(info.getParentVersion());
        Assert.assertTrue(info.getArtifactId().length() > 0);
        Assert.assertEquals(info.getParentGroupId(), info.getGroupId());
        Assert.assertNotEquals(info.getParentVersion(), info.getVersion());
        Assert.assertTrue(info.getGroupPath().length() > 0);
    }

    /**
     * Replaces the POM version in testPom3.xml.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testReplacePom3() throws IOException {
        Path source = Paths.get("src/test/resources/testPom3.xml");
        Path target = new File(FileUtils.getTempDirectory(), "testPom3.xml").toPath();
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        PomReader.replaceVersion(target.toFile(), "0.2.0-SNAPSHOT", "0.3.0", null, null);
        PomInfo info = PomReader.getInfo(target.toFile());
        Assert.assertEquals("0.3.0", info.getVersion());
    }

    /**
     * Replaces the parent POM version in testPom.xml.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testReplacePom() throws IOException {
        Path source = Paths.get("src/test/resources/testPom.xml");
        Path target = new File(FileUtils.getTempDirectory(), "testPom.xml").toPath();
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        PomReader.replaceVersion(target.toFile(), null, null, "0.3.0-SNAPSHOT", "0.4.0");
        PomInfo info = PomReader.getInfo(target.toFile());
        Assert.assertEquals("0.4.0", info.getParentVersion());
    }

    /**
     * Replaces POM and the parent POM version in testPom2.xml.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testReplacePom2() throws IOException {
        Path source = Paths.get("src/test/resources/testPom2.xml");
        Path target = new File(FileUtils.getTempDirectory(), "testPom2.xml").toPath();
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        PomReader.replaceVersion(target.toFile(), "0.2.0-SNAPSHOT", "0.1.0", "0.3.0-SNAPSHOT", "0.4.0");
        PomInfo info = PomReader.getInfo(target.toFile());
        Assert.assertEquals("0.1.0", info.getVersion());
        Assert.assertEquals("0.4.0", info.getParentVersion());
    }

}
