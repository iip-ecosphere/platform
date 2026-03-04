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

import de.iip_ecosphere.platform.connectors.model.FloatIndex;
import org.junit.Assert;

/**
 * Tests {@link FloatIndex}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FloatIndexTest {
    
    /**
     * Tests {@link FloatIndex}.
     */
    @Test
    public void testLongIndex() {
        FloatIndex fi = new FloatIndex(10.21f);
        Assert.assertEquals(10.21, fi.getValue(), 0.001);

        fi = new FloatIndex(Float.valueOf(11.39f));
        Assert.assertEquals(11.39, fi.getValue(), 0.001);
    }

}
