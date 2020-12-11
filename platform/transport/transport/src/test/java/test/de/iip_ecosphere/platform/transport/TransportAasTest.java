/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.transport.TransportAas;

/**
 * Tests the transport AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportAasTest {
    
    /**
     * Tests the transport AAS.
     */
    @Test
    public void testAas() {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(TransportAas.class));
    }

}
