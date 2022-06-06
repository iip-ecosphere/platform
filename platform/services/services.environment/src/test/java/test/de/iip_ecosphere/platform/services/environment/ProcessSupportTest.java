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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.ProcessSupport;
import de.iip_ecosphere.platform.services.environment.ProcessSupport.ScriptOwner;
import org.junit.Assert;

/**
 * Tests {@link ProcessSupport}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessSupportTest {
    
    /**
     * Tests {@link ProcessSupport}.
     */
    @Test
    public void testPythonSupport() {
        AtomicReference<String> ref = new AtomicReference<>(null);
        ScriptOwner owner = new ScriptOwner("pythonSupportTest", "src/test/python", "notEx.zip");
        ProcessSupport.callPython(owner, "ForwardingApp.py", s -> ref.set(s), "testABC");
        Assert.assertEquals("testABC", ref.get().trim());
    }

}
