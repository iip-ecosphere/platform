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

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.transport.TransportAas;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

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
    public void testAas() throws ExecutionException {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(TransportAas.class));
        Aas aas = AasPartRegistry.getAas(AasPartRegistry.build(), AasPartRegistry.NAME_AAS);
        Assert.assertNotNull(aas);
        
        Submodel tsm = aas.getSubModel(TransportAas.NAME_SUBMODEL);
        Assert.assertNotNull(tsm);
        Property prop = tsm.getProperty(TransportAas.NAME_VAR_CONNECTOR);
        Assert.assertNotNull(prop);
        Assert.assertEquals(TransportFactory.getConnectorName(), prop.getValue());
        prop = tsm.getProperty(TransportAas.NAME_VAR_SERIALIZER);
        Assert.assertNotNull(prop);
        Assert.assertEquals(SerializerRegistry.getName(), prop.getValue());
    }

}
