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

import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory.ProcessInfo;
import test.de.iip_ecosphere.platform.support.processInfo.TestProcessInfoFactory;

/**
 * Tests {@link ProcessInfo}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessInfoTest {
    
    /**
     * Tests basic ProcessInfo functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testProcessInfo() throws IOException {
        // just the very basic
        ProcessInfoFactory pif = ProcessInfoFactory.getInstance();
        Assert.assertTrue(pif instanceof TestProcessInfoFactory);
        ProcessInfoFactory.setInstance(pif);
        
        // just called, result irrelevant here
        pif.getProcessId(); 
        pif.getProcessId(null);
        
        ProcessInfo pi = pif.create(null);
        Assert.assertTrue(pi.getVirtualSize() > 0);

        pi = pif.create(0);
        Assert.assertTrue(pi.getVirtualSize() > 0);
    }

}
