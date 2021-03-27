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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.Result;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;

/**
 * Tests {@link JsonResultWrapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonResultWrapperTest {

    /**
     * Something to test that throws an exception.
     * 
     * @param id an id param
     * @return {@code id}
     * @throws ExecutionException if {@code id} is <b>null</b>
     */
    private String testFunc(String id) throws ExecutionException {
        if (null == id || id.length() == 0) {
            throw new ExecutionException("null or empty", null);
        }
        return id;
    }
    
    /**
     * Tests {@link JsonResultWrapper}.
     */
    @Test
    public void testWrapper() {
        JsonResultWrapper wr = new JsonResultWrapper(p -> testFunc(readString(p, 0, "")));
        
        // this is a normal execution of testFunc
        Result res = JsonResultWrapper.resultFromJson(wr.apply(new Object[]{"1234"}));
        Assert.assertNotNull(res);
        Assert.assertFalse(res.isException());
        Assert.assertNull(res.getException());
        Assert.assertEquals("1234", res.getResult());

        // this causes an exception in testFunc
        res = JsonResultWrapper.resultFromJson(wr.apply(new Object[]{null}));
        Assert.assertNotNull(res);
        Assert.assertTrue(res.isException());
        Assert.assertNotNull(res.getException());
        Assert.assertNull(res.getResult());
        System.out.println("EXC: " + res.getException());
    }
    
}
