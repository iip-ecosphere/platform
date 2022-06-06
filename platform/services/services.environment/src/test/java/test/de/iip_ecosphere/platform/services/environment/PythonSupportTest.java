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

import de.iip_ecosphere.platform.services.environment.PythonSupport;
import de.iip_ecosphere.platform.services.environment.PythonSupport.ScriptOwner;
import org.junit.Assert;

/**
 * Tests {@link PythonSupport}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonSupportTest {
    
    /**
     * Tests {@link PythonSupport}.
     */
    @Test
    public void testPythonSupport() {
        AtomicReference<String> ref = new AtomicReference<>(null);
        ScriptOwner owner = new ScriptOwner("pythonSupportTest", "src/test/python", "notEx.zip");
        PythonSupport.callPython(owner, "ForwardingApp.py", s -> ref.set(s), "testABC");
        Assert.assertEquals("testABC", ref.get().trim());
    }

}
