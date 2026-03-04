/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.model.LongIndex;
import org.junit.Assert;

/**
 * Tests {@link LongIndex}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LongIndexTest {
    
    /**
     * Tests {@link LongIndex}.
     */
    @Test
    public void testLongIndex() {
        LongIndex li = new LongIndex(10);
        Assert.assertEquals(10, li.getValue());

        li = new LongIndex(Long.valueOf(11));
        Assert.assertEquals(11, li.getValue());
    }

}
