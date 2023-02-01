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

import org.apache.maven.project.MavenProject;
import org.junit.Test;

import de.iip_ecosphere.platform.tools.maven.python.PythonTestMojo;

/**
 * Test for {@link PythonTestMojo}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonTestTest extends AbstractTest {
    
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
     * Tests the Python compile plugin.
     * 
     * @throws Exception if the test fails
     */
    @Test
    public void testTest() throws Exception {
        MavenProject project = readMavenProject(new File( "src/test/resources/unit/project-test"));
        PythonTestMojo myMojo = (PythonTestMojo) lookupConfiguredMojo(project, "test-python");
        assertNotNull(myMojo);
        myMojo.execute();
        assertEquals(1, myMojo.getTestedFileCount(true));
        
        myMojo.setTest("HelloWorldTest");
        myMojo.execute();
        assertEquals(1, myMojo.getTestedFileCount(true));

        // shall not lead to test execution
        myMojo.setTest("HelloWorldTest2");
        myMojo.execute();
        assertEquals(0, myMojo.getTestedFileCount(true));
    }
    
    // checkstyle: resume exception type check
    
}
