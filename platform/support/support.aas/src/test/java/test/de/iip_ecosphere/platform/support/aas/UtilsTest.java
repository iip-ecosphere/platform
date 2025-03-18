/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.types.common.EnumRegistry;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;

/**
 * Tests {@link Utils} and {@link EnumRegistry}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class UtilsTest {

    /**
     * A test (enum) interface. Must be public for access from {@link Utils}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface IMyEnum {
    }
    
    /**
     * A test enum. Must be public for access from {@link Utils}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum MyEnum implements IMyEnum {
        VALUE1,
        VALUE2
    }

    /**
     * An "extending" test enum. Must be public for access from {@link Utils}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum MyExtendingEnum implements IMyEnum {
        VALUE3
    }
   
    /**
     * Tests {@link Utils#getEnumValue(String, Class, Class)} with and without registered enums.
     */
    @Test
    public void testGetEnumValue() {
        Assert.assertEquals(MyEnum.VALUE1, Utils.getEnumValue("VALUE1", IMyEnum.class, MyEnum.class));
        Assert.assertEquals(null, Utils.getEnumValue("VALUE3", IMyEnum.class, MyEnum.class));

        EnumRegistry.registerEnum(MyExtendingEnum.class);
        Assert.assertEquals(MyEnum.VALUE1, Utils.getEnumValue("VALUE1", IMyEnum.class, MyEnum.class));
        Assert.assertEquals(null, Utils.getEnumValue("VALUE4", IMyEnum.class, MyEnum.class));
        // typical call against unknown
        Assert.assertEquals(MyExtendingEnum.VALUE3, Utils.getEnumValue("VALUE3", IMyEnum.class, MyEnum.class));
        
        EnumRegistry.unregisterEnum(MyExtendingEnum.class);
        Assert.assertEquals(null, Utils.getEnumValue("VALUE3", IMyEnum.class, MyEnum.class));
    }
    
    /**
     * Tests {@link AasFactory#composeIdShort(String...)}.
     */
    @Test
    public void testComposeIdShort() {
        Assert.assertEquals("", AasFactory.composeIdShort());
        Assert.assertEquals("Test", AasFactory.composeIdShort("Test"));
        Assert.assertEquals("Test", AasFactory.composeIdShort("Test", ""));
        Assert.assertEquals("TestA", AasFactory.composeIdShort("Test", "a"));
        Assert.assertEquals("TestAnother", AasFactory.composeIdShort("Test", "another"));
        Assert.assertEquals("testAnotherName", AasFactory.composeIdShort("test", "another", "Name"));
    }
    
    
}
