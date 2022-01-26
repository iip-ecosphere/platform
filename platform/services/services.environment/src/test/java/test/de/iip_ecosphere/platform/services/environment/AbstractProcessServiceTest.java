/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
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

import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import org.junit.Assert;

/**
 * Basic process tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractProcessServiceTest {
    
    /**
     * Tests sanitizing file names.
     */
    @Test
    public void testSanitize() {
        Assert.assertEquals("a", AbstractProcessService.sanitizeFileName("a"));
        Assert.assertEquals("a", AbstractProcessService.sanitizeFileName("a", false));
        Assert.assertTrue(AbstractProcessService.sanitizeFileName("a", true).startsWith("a"));
    }
    
}
