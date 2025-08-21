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

import de.iip_ecosphere.platform.support.http.Http;
import test.de.iip_ecosphere.platform.support.http.TestHttp;

/**
 * Tests {@link Http}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HttpTest {
    
    /**
     * Tests basic HTTP functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testHttp() throws IOException {
        // just the very basic
        Http http = Http.getInstance();
        Assert.assertTrue(http instanceof TestHttp);
        Http.setInstance(http);
    }

}
