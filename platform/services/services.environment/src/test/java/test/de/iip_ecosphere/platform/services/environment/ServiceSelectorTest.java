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

package test.de.iip_ecosphere.platform.services.environment;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.ServiceSelector;
import org.junit.Assert;

/**
 * Tests {@link ServiceSelector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceSelectorTest {
    
    /**
     * A test service selector.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyServiceSelector implements ServiceSelector<String> {

        @Override
        public String select(String input) {
            // do some logic on input
            return input;
        }
        
    }
    
    /**
     * Tests the selector.
     */
    @Test
    public void testSelector() {
        ServiceSelector<String> sel = ServiceSelector.createInstance(
            ServiceSelectorTest.class.getClassLoader(), "abc", String.class, "dflt");
        Assert.assertNotNull(sel);
        Assert.assertEquals("dflt", sel.select(""));
        
        sel = ServiceSelector.createInstance(
            ServiceSelectorTest.class.getClassLoader(), MyServiceSelector.class.getName(), String.class, "dflt");
        Assert.assertNotNull(sel);
        Assert.assertEquals("abc", sel.select("abc"));
        
        sel.actionCompleted("abc"); // just to call it
    }

}
