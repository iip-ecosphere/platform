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

import de.iip_ecosphere.platform.support.rest.Rest;
import de.iip_ecosphere.platform.support.rest.Rest.RestServer;
import test.de.iip_ecosphere.platform.support.rest.TestRest;

/**
 * Tests {@link Rest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RestTest {
    
    /**
     * Tests basic REST functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testRest() throws IOException {
        // just the very basic
        Rest rest = Rest.getInstance();
        Assert.assertTrue(rest instanceof TestRest);
        Rest.setInstance(rest);
        
        RestServer server = Rest.getInstance().createServer(-1);
        server.stop(true);
    }

}
