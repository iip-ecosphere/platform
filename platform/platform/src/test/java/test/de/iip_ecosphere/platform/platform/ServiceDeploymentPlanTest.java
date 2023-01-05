/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.platform;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.platform.cli.ServiceDeploymentPlan;
import de.iip_ecosphere.platform.platform.cli.ServiceDeploymentPlan.ServiceResourceAssignment;

/**
 * Tests {@link ServiceDeploymentPlan}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceDeploymentPlanTest {
    
    /**
     * Tests the plan in {@code plan.yml}.
     * 
     * @throws IOException shall not occur in a successful test
     */
    @Test
    public void testPlan() throws IOException {
        ServiceDeploymentPlan plan = ServiceDeploymentPlan.readFromYaml("plan.yml");
        Assert.assertNotNull(plan);
        Assert.assertEquals("test.jar", plan.getArtifact());
        Assert.assertNotNull(plan.getAssignments());
        Assert.assertEquals(2, plan.getAssignments().size());
        Assert.assertFalse(plan.isParallelize());

        plan.setParallelize(true);
        Assert.assertTrue(plan.isParallelize());
        
        ServiceResourceAssignment assng = plan.getAssignments().get(0);
        Assert.assertEquals("a1234", assng.getResource());
        Assert.assertNotNull(assng.getServices());
        Assert.assertEquals(2, assng.getServices().size());
        Assert.assertEquals(2, assng.getServicesAsArray(null, null).length);
        Assert.assertEquals("Start", assng.getServices().get(0));
        Assert.assertEquals("Transform", assng.getServices().get(1));
        
        assng = plan.getAssignments().get(1);
        Assert.assertEquals("a1235", assng.getResource());
        Assert.assertNotNull(assng.getServices());
        Assert.assertEquals(2, assng.getServices().size());
        Assert.assertEquals(2, assng.getServicesAsArray(null, null).length);
        Assert.assertEquals(2, assng.getServicesAsArray("app", "001").length);
        Assert.assertEquals("AI", assng.getServices().get(0));
        Assert.assertEquals("End", assng.getServices().get(1));
        
        Assert.assertNotNull(plan.getEnsembles());
        Assert.assertTrue(plan.getEnsembles().size() == 2);
        Assert.assertNull(plan.getEnsembles().get("Start"));
        Assert.assertEquals("End", plan.getEnsembles().get("AI"));
        
        Assert.assertFalse(plan.isDisabled());
    }

}
