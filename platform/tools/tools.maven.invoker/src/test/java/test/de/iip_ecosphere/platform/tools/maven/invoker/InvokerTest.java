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

package test.de.iip_ecosphere.platform.tools.maven.invoker;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

import de.iip_ecosphere.platform.tools.maven.invoker.InvokerMojo;

/**
 * Test for {@link InvokerMojo}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class InvokerTest extends AbstractTest {
    
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
        MavenProject project = readMavenProject(new File( "src/test/resources/unit/project-ok"));
        InvokerMojo myMojo = (InvokerMojo) lookupConfiguredMojo(project, "invoke");
        assertNotNull(myMojo);
        myMojo.disable(); // just for testing
        myMojo.execute();
    }
    
    // checkstyle: resume exception type check
    
}
