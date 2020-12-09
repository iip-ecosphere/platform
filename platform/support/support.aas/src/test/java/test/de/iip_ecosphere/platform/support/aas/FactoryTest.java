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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;

/**
 * Tests the factory/descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FactoryTest {
    
    /**
     * Tests that there is a fake factory through Java Service loader.
     */
    @Test
    public void testFakeFactory() throws IOException {
        AasFactory instance = AasFactory.getInstance();
        Assert.assertNotNull(instance);
        Assert.assertTrue(instance instanceof FakeAasFactory);
        // it's just a fake
        Assert.assertNull(instance.createAasBuilder("", ""));
        Assert.assertNull(instance.createSubModelBuilder(""));
        Assert.assertNull(instance.retrieveAas("", 1234, "", ""));
        
        Assert.assertNull(instance.createDeploymentBuilder("localhost", 1234));
        Assert.assertNull(instance.createDeploymentBuilder("/path", "localhost", 1234));
    }

}
