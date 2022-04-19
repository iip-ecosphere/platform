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

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.LangString;
import org.junit.Assert;

/**
 * Tests {@link LangString}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LangStringTest {

    /**
     * Tests {@link LangString}.
     */
    @Test
    public void testLangString() {
        LangString ls = new LangString("de", "Ich bin's.");
        Assert.assertEquals("de", ls.getLanguage());
        Assert.assertEquals("Ich bin's.", ls.getDescription());
 
        LangString ls2 = LangString.create("Ich bin's.@De");
        Assert.assertEquals("de", ls.getLanguage());
        Assert.assertEquals("Ich bin's.", ls.getDescription());
        Assert.assertEquals(ls, ls2);
        Assert.assertEquals(ls.hashCode(), ls2.hashCode());
        
        String dflt = LangString.getDefaultLanguage();
        Assert.assertNotNull(dflt);
        System.out.println(dflt);
        LangString.setDefaultLanguage("de");
        LangString ls3 = LangString.create("Ich bin's.");
        Assert.assertEquals(ls, ls3);
        LangString.setDefaultLanguage(dflt);
    }
    
}
