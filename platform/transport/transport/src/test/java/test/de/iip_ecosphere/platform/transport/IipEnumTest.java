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

package test.de.iip_ecosphere.platform.transport;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.IipEnum;

/**
 * Tests {@link IipEnum}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IipEnumTest {
    
    public enum MyEnum implements IipEnum {
        
        TEST(10);
        
        private int ord;
        
        /**
         * Creates a model instance with given {@link #getModelOrdinal()}.
         * 
         * @param ordinal the ordinal
         */
        private MyEnum(int ordinal) {
            ord = ordinal;
        }
        
        @Override
        public int getModelOrdinal() {
            return ord;
        }
        
    }

    /**
     * Tests {@link IipEnum#valueByModelOrdinal(Class, int)}.
     */
    @Test
    public void testIipEnum() {
        Assert.assertNull(IipEnum.valueByModelOrdinal(MyEnum.class, 0));
        Assert.assertTrue(MyEnum.TEST == IipEnum.valueByModelOrdinal(MyEnum.class, 10));
    }

}
