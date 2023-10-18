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

package test.de.iip_ecosphere.platform.tools.maven.python;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import de.iip_ecosphere.platform.tools.maven.python.PythonCompileMojo;

import org.junit.Assert;

/**
 * Test for {@link PythonCompileMojo}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonCompileTest extends AbstractTest {
    
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
     * Tests the Python compile plugin (project ok).
     * 
     * @throws Exception if the test fails
     */
    @Test
    public void testOk() throws Exception {
        testOk(true);
        testOk(false);
    }
    
    /**
     * Tests the Python compile plugin (project ok).
     * 
     * @param cleanFirst get rid of MD5 file before the test
     * @throws Exception if the test fails
     */
    private void testOk(boolean cleanFirst) throws Exception {
        MavenProject project = readMavenProject(new File( "src/test/resources/unit/project-ok"));
        PythonCompileMojo myMojo = (PythonCompileMojo) lookupConfiguredMojo(project, "compile-python");
        assertNotNull(myMojo);
        cleanMd5File(cleanFirst, myMojo);
        myMojo.execute();
        assertMd5File(myMojo);
        cleanMd5File(!cleanFirst, myMojo);
    }

    /**
     * Tests the Python compile plugin (erroneous project).
     * 
     * @throws Exception if the test fails
     */
    @Test
    public void testFail() throws Exception {
        testFail(true);
        testFail(false);
    }
    
    /**
     * Tests the Python compile plugin (erroneous project).
     * 
     * @param cleanFirst get rid of MD5 file before the test
     * @throws Exception if the test fails
     */
    private void testFail(boolean cleanFirst) throws Exception {
        MavenProject project = readMavenProject(new File( "src/test/resources/unit/project-fail"));
        
        PythonCompileMojo myMojo = (PythonCompileMojo) lookupConfiguredMojo(project, "compile-python");
        assertNotNull(myMojo);
        cleanMd5File(cleanFirst, myMojo);
        try {
            myMojo.execute();
            Assert.fail("No failure");
        } catch (MojoExecutionException e) {
            // this is fine
        }
        // if it fails, there may not be an md5file
        cleanMd5File(!cleanFirst, myMojo);
    }

    /**
     * Asserts the MD5 file existence.
     * 
     * @param mojo instance carrying the MD5 file
     */
    private static void assertMd5File(PythonCompileMojo mojo) {
        Assert.assertTrue(mojo.getHashFile().exists());
    }

    /**
     * Cleans the MD5 file if requested.
     * 
     * @param clean to clean or not clean
     * @param mojo instance carrying the MD5 file
     */
    private static void cleanMd5File(boolean clean, PythonCompileMojo mojo) {
        if (clean) {
            FileUtils.deleteQuietly(mojo.getHashFile().getParentFile()); // we do not have targets, no real mvn run
        }
    }
    
    // checkstyle: resume exception type check
    
}
