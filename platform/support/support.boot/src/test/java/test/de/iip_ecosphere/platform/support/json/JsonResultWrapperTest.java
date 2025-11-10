/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.json;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.OperationCompletedListener;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.Result;
import org.junit.Assert;

/**
 * Tests {@link JsonResultWrapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonResultWrapperTest {
    
    /**
     * Tests {@link JsonResultWrapper} basic functionality, successful function execution, exception-throwing execution.
     */
    @Test
    public void testWrapperBasic() throws ExecutionException {
        JsonResultWrapper w = new JsonResultWrapper(p -> true);
        Result res = JsonResultWrapper.resultFromJson(w.apply(null));
        Assert.assertNotNull(res);
        Assert.assertNull(res.getException());
        Assert.assertEquals(true, Boolean.valueOf(res.getResult()));

        JsonResultWrapper.fromJson(w.apply(null));
      
        w = new JsonResultWrapper(p -> { throw new ExecutionException("exc", null); });
        res = JsonResultWrapper.resultFromJson(w.apply(null));
        Assert.assertNotNull(res);
        Assert.assertNull(res.getResult());
        Assert.assertNotNull(res.getException());
        Assert.assertEquals("exc", res.getException());

        try {
            JsonResultWrapper.fromJson(w.apply(null));
            Assert.fail("No exception");
        } catch (ExecutionException e) {
        }
        
        Assert.assertNull(JsonResultWrapper.fromJson(null));
        res = JsonResultWrapper.resultFromJson("");
        Assert.assertNotNull(res);
        Assert.assertNotNull(res.getException());
        Assert.assertTrue(res.getException().length() > 0);  // whatever

        JsonResultWrapper.fromJson(new JsonResultWrapper(p -> true), 1); // ,1 is pseudo param
    }

    /**
     * Tests {@link JsonResultWrapper} completion listener functionality.
     */
    @Test
    public void testWrapperListener() throws ExecutionException {
        AtomicInteger completedCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();
        OperationCompletedListener listener = new OperationCompletedListener() {

            @Override
            public void operationCompleted() {
                completedCount.incrementAndGet();
            }

            @Override
            public void operationFailed() {
                failedCount.incrementAndGet();
            }
            
        };
        JsonResultWrapper w = new JsonResultWrapper(p -> true, listener);
        JsonResultWrapper.resultFromJson(w.apply(null));
        Assert.assertEquals(1, completedCount.getAndSet(0));
        Assert.assertEquals(0, failedCount.getAndSet(0));

        w = new JsonResultWrapper(p -> { throw new ExecutionException("exc", null); }, listener);
        JsonResultWrapper.resultFromJson(w.apply(null));
        Assert.assertEquals(0, completedCount.getAndSet(0));
        Assert.assertEquals(1, failedCount.getAndSet(0));
    }

    /**
     * Tests {@link JsonResultWrapper} task functionality.
     */
    @Test
    public void testWrapperTask() throws ExecutionException {
        JsonResultWrapper w = new JsonResultWrapper(p -> true, p -> "t123");
        JsonResultWrapper.resultFromJson(w.apply(null));
        // no access to TaskData anymore

        w = new JsonResultWrapper(p -> { throw new ExecutionException("exc", null); }, p -> "t124");
        JsonResultWrapper.resultFromJson(w.apply(null));
        // no access to TaskData anymore
    }

}
