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

package test.de.iip_ecosphere.platform.examples.hm23;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import iip.datatypes.AiResult;
import iip.datatypes.AiResultImpl;
import iip.datatypes.DecisionResult;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;

import static de.iip_ecosphere.platform.examples.hm23.ActionDecider.*;

/**
 * Tests the action decider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ActionDeciderTest {
    
    /**
     * Tests the action decider aggregation.
     */
    @Test
    public void aggregateTest() {
        String[] errors = new String[]{ERROR_CLASS_SHATTER, ERROR_CLASS_SCRATCH, 
            ERROR_CLASS_GEOMETRY, ERROR_CLASS_CAR_MISSING, ERROR_CLASS_NORMAL};
        List<AiResult> aiResults = new ArrayList<>();
        
        AiResult res = new AiResultImpl(); // left
        res.setAiId("ID");
        res.setModelId("Model");
        res.setImageUri("img1");
        res.setRobotId(1);
        res.setError(errors);
        res.setErrorConfidence(new double[] {0.1, 0.2, 0.1, 0.4, 0.9});
        aiResults.add(res);
        
        res = new AiResultImpl(); // top
        res.setAiId("ID");
        res.setModelId("Model");
        res.setImageUri("img2");
        res.setRobotId(1);
        res.setError(errors);
        res.setErrorConfidence(new double[] {0.55, 0.3, 0.01, 0.4, 0.2});
        aiResults.add(res);
        
        res = new AiResultImpl(); // right
        aiResults.add(res);
        res.setAiId("ID");
        res.setModelId("Model");
        res.setImageUri("img3");
        res.setRobotId(1);
        res.setError(errors);
        res.setErrorConfidence(new double[] {0.1, 0.4, 0.05, 0.4, 0.9});
        
        DecisionResult dr = aggregateResults(aiResults, 0.8);
        Assert.assertEquals("ID", dr.getAiId());
        Assert.assertEquals("Model", dr.getModelId());
        Assert.assertEquals(1, dr.getRobotId());
        Assert.assertNotNull(dr.getImageUri());
        Assert.assertArrayEquals(new String[] {"img1", "img2", "img3"}, dr.getImageUri());
        Assert.assertNotNull(dr.getError());
        Set<String> set = CollectionUtils.toSet(dr.getError());
        Assert.assertEquals(set.size(), dr.getError().length);
        Assert.assertNotNull(dr.getErrorConfidence());
        Assert.assertEquals(errors.length, dr.getErrorConfidence().length);
        for (int i = 0; i < dr.getErrorConfidence().length; i++) {
            if (!dr.getError()[i].equals(ERROR_CLASS_NORMAL)) { // score
                Assert.assertTrue(dr.getErrorConfidence()[i] >= 0 && dr.getErrorConfidence()[i] <= 1);
            }
        }
        Assert.assertNotNull(dr.getImg1Error());
        Assert.assertNotNull(dr.getImg2Error());
        Assert.assertNotNull(dr.getImg3Error());
        
        System.out.println(Arrays.toString(dr.getError()));
        System.out.println(Arrays.toString(dr.getErrorConfidence()));
    }

}
