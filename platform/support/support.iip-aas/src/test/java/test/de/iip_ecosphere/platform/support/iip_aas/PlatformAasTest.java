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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;

/**
 * Tests the platform AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAasTest {
    
    /**
     * Tests the transport AAS.
     */
    @Test
    public void testAas() throws ExecutionException {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(PlatformAas.class));
        // obtain the plattform AAS and go then on with the transport sub-model
        Aas aas = AasPartRegistry.getAas(AasPartRegistry.build(), AasPartRegistry.NAME_AAS);
        Assert.assertNotNull(aas);
        
        aas.accept(new AasPrintVisitor());
        
        Submodel psm = aas.getSubmodel(PlatformAas.NAME_SUBMODEL);
        Assert.assertNotNull(psm);
        Property prop = psm.getProperty(PlatformAas.NAME_PROPERTY_VERSION);
        Assert.assertNotNull(prop);
        Object val = prop.getValue();
        Assert.assertNotNull(val);
        Assert.assertFalse(val.toString().equals("??"));
        
        prop = psm.getProperty(PlatformAas.NAME_PROPERTY_BUILDID);
        Assert.assertNotNull(prop);
        val = prop.getValue();
        Assert.assertNotNull(val);
        Assert.assertFalse(val.toString().equals("??"));

        prop = psm.getProperty(PlatformAas.NAME_PROPERTY_RELEASE);
        Assert.assertNotNull(prop);
        val = prop.getValue();
        Assert.assertNotNull(val);
        Assert.assertTrue(val.toString().equals("true") || val.toString().equals("false"));

        prop = psm.getProperty(PlatformAas.NAME_PROPERTY_NAME);
        Assert.assertNotNull(prop);
        val = prop.getValue();
        Assert.assertNotNull(val);
        Assert.assertTrue(val.toString().length() > 0);
    }

}
