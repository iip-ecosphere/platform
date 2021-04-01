/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import test.de.iip_ecosphere.platform.support.aas.AasTest;

/**
 * Tests the AAS abstraction implementation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTest extends AasTest {
    
    /**
     * Tests {@link AasFactory#fixId(String)} for BaSyX.
     */
    @Test
    public void testFixId() {
        AasFactory instance = AasFactory.getInstance();
        Assert.assertNull(instance.fixId(null));
        Assert.assertEquals("", instance.fixId(""));
        Assert.assertEquals("id", instance.fixId("id"));
        Assert.assertEquals("a1id", instance.fixId("1id"));
        Assert.assertEquals("a_id", instance.fixId("a_id"));
        Assert.assertEquals("a_id", instance.fixId("a id"));
        Assert.assertEquals("test_log", instance.fixId("test-log"));
    }
        
}
