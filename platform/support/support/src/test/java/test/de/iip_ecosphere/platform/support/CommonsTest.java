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

package test.de.iip_ecosphere.platform.support;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.commons.Commons;
import test.de.iip_ecosphere.platform.support.commons.TestCommons;

/**
 * Commons interface test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CommonsTest {
    
    /**
     * Tests basic Commons functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testCommons() throws IOException {
        // just the very basic
        Commons commons = Commons.getInstance();
        Assert.assertTrue(commons instanceof TestCommons);
        Commons.setInstance(commons);
    }

}
