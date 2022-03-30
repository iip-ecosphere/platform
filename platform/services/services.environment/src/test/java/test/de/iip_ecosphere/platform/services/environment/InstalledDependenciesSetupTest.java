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

package test.de.iip_ecosphere.platform.services.environment;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.InstalledDependenciesSetup;

/**
 * Tests {@link InstalledDependenciesSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class InstalledDependenciesSetupTest {
    
    /**
     * Tests the dependency setup.
     */
    @Test
    public void testDependenciesSetup() {
        Assert.assertTrue(InstalledDependenciesSetup.getJavaKey().length() > 0);
        Assert.assertTrue(InstalledDependenciesSetup.getJavaKey().startsWith(
            InstalledDependenciesSetup.KEY_PREFIX_JAVA));
        InstalledDependenciesSetup inst = new InstalledDependenciesSetup();
        Assert.assertTrue(inst.getLocations().size() > 0); // actual Java must be in
        Assert.assertNotNull(inst.getLocation(InstalledDependenciesSetup.getJavaKey()));  // actual Java must be in
        
        inst = InstalledDependenciesSetup.readFromYaml(); // read from test/resources via classloader
        Assert.assertTrue(inst.getLocations().size() > 1); // more than the last one ;)
        Assert.assertNotNull(inst.getLocation(InstalledDependenciesSetup.getJavaKey())); // default Java is still there
        Assert.assertNotNull(inst.getLocation("PYTHON2"));
        Assert.assertNotNull(inst.getLocation("PYTHON3"));
        Assert.assertNotNull(inst.getLocation("JAVA99"));
        Assert.assertNull(inst.getLocation("JAVA0"));
    }

}
