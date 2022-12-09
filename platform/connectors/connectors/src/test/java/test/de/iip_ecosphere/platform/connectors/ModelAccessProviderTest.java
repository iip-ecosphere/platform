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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.model.ModelAccessProvider;

/**
 * Tests {@link ModelAccessProvider}. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModelAccessProviderTest {
    
    /**
     * Tests {@link ModelAccessProvider#optional(de.iip_ecosphere.platform.connectors.model.
     * ModelAccess, de.iip_ecosphere.platform.connectors.model.ModelAccessProvider.IOVoidFunction)}.
     */
    @Test
    public void testIoVoidFunction() {
        Assert.assertTrue(ModelAccessProvider.optional(null, (a) -> { }));
        Assert.assertFalse(ModelAccessProvider.optional(null, (a) -> { throw new IOException(); }));
    }

}
