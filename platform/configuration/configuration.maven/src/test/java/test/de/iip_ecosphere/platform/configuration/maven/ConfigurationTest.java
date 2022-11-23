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

package test.de.iip_ecosphere.platform.configuration.maven;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.maven.AbstractConfigurationMojo;

/**
 * Test for the configuration Mojos.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationTest extends AbstractTest {
    
    // checkstyle: stop exception type check
    
    @Override
    protected void setUp() throws Exception {
        // required
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        // required
        super.tearDown();
    }
    
    /**
     * Tests the configuration plugins without fully executing them as no IVML file is there.
     * 
     * @throws Exception if the test fails
     */
    @Test
    public void testNoIvml() throws Exception {
        MavenProject project = readMavenProject(new File( "src/test/resources/unit/project-noIvml"));

        assertBase(project, "generateApps");
        assertBase(project, "generateAppsNoDeps");
        assertBase(project, "generateBroker");
        assertBase(project, "generateInterfaces");
        assertBase(project, "generatePlatform");
    }

    /**
     * Asserts basic configuration Mojo properties. Tries executing the Mojo. Uses {@code goal} as expected
     * VIL start rule name.
     * 
     * @param project the Maven project to obtain the Mojo from
     * @param goal the Maven goal to look for
     * @throws Exception if execution fails
     * @see #assertBase(MavenProject, String, String)
     */
    private void assertBase(MavenProject project, String goal) throws Exception {
        assertBase(project, goal, goal);
    }

    /**
     * Asserts basic configuration Mojo properties. Tries executing the Mojo.
     * 
     * @param project the Maven project to obtain the Mojo from
     * @param goal the Maven goal to look for
     * @param expectedStartRule the expected start rule
     * @throws Exception if execution fails
     */
    private void assertBase(MavenProject project, String goal, String expectedStartRule) throws Exception {
        AbstractConfigurationMojo mojo = (AbstractConfigurationMojo) lookupConfiguredMojo(project, goal);
        assertNotNull(mojo);
        Assert.assertEquals(expectedStartRule, mojo.getStartRule());
        Assert.assertEquals("ExamplePython", mojo.getModel());
        Assert.assertTrue(mojo.getModelDirectory().endsWith("/src/test/easy"));
        Assert.assertTrue(mojo.getOutputDirectory().endsWith("/gen/py"));
        Assert.assertTrue(mojo.getResourcesDirectory().endsWith("/resources"));
        Assert.assertTrue(mojo.getFallbackResourcesDirectory().endsWith("/resources"));
        Assert.assertEquals("TOP", mojo.getTracingLevel());
        mojo.execute();
    }

    // checkstyle: resume exception type check
}
