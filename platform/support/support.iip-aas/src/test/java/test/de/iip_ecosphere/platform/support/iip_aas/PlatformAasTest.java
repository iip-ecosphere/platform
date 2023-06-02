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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstanceAasConstructor;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstancesAasClient;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAasClient;
import de.iip_ecosphere.platform.support.iip_aas.PlatformClient;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.Eclass;

/**
 * Tests the platform AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAasTest {
    
    /**
     * Tests the transport AAS.
     * 
     * @throws ExecutionException shall not occur in a valid test
     * @throws IOException shall not occur in a valid test
     */
    @Test
    public void testAas() throws ExecutionException, IOException {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(PlatformAas.class));
        // obtain the plattform AAS and go then on with the transport sub-model
        // shortcut test as AAS is not dynamic/active

        AasSetup oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof PlatformAas);

        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        Server aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
        
        Aas aas = res.getAas().get(0);
        Assert.assertNotNull(aas);
        
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
        
        PlatformClient client = new PlatformAasClient();
        // no supplier, fail
        try {
            client.snapshotAas("xyz");
            Assert.fail("No exception thrown");
        } catch (ExecutionException e) {
            // this is desired
        }
        
        SemanticIdResolutionResult sres = client.resolveSemanticId(Eclass.IRDI_UNIT_BYTE);
        Assert.assertNotNull(sres);
        
        assertApplicationInstances();
        
        AasPartRegistry.setAasSupplier(() -> res.getAas());
        // seems to work only once with BaSyx
        //assertString(client.snapshotAas(null), null);
        //assertString(client.snapshotAas(""), null);
        assertString(client.snapshotAas("xyz"), "xyz");
        
        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
    }
    
    /**
     * Asserts results around application instances.
     * 
     * @throws IOException shall not occur, may occur if {@link ApplicationInstancesAasClient} fails
     */
    private static void assertApplicationInstances() throws IOException {
        Aas aas = AasPartRegistry.retrieveIipAas(); // remote
        aas.createSubmodelBuilder(ApplicationInstanceAasConstructor.NAME_SUBMODEL_APPINSTANCES, null).build(); 

        ApplicationInstancesAasClient instClient = new ApplicationInstancesAasClient();
        Assert.assertEquals(0, instClient.getInstanceCount("app-1"));
        Assert.assertEquals(0, instClient.getInstanceCount("app-1", "plan-1"));
        
        String id1 = ApplicationInstanceAasConstructor.notifyAppNewInstance("app-1", "plan-1");
        if (ApplicationInstanceAasConstructor.firstAppWithoutAppId()) {
            Assert.assertNull(id1); // it's the first one
        } else {
            Assert.assertNotNull(id1);
        }
        String id2 = ApplicationInstanceAasConstructor.notifyAppNewInstance("app-1", "plan-1");
        Assert.assertNotNull(id2);
        
        instClient = new ApplicationInstancesAasClient(); // short term use, no update
        Assert.assertEquals(2, instClient.getInstanceCount("app-1"));
        Assert.assertEquals(2, instClient.getInstanceCount("app-1", "plan-1"));
        
        int count = ApplicationInstanceAasConstructor.notifyAppInstanceStopped("app-1", id2);
        Assert.assertEquals(1, count); // already one gone
        count = ApplicationInstanceAasConstructor.notifyAppInstanceStopped("app-1", id1);
        Assert.assertEquals(0, count); // both gone      
        
        instClient = new ApplicationInstancesAasClient(); // short term use, no update
        Assert.assertEquals(0, instClient.getInstanceCount("app-1"));
        Assert.assertEquals(0, instClient.getInstanceCount("app-1", "plan-1"));
    }
    
    /**
     * Asserts that a string is there, not empty, potentially having a given substring.
     * 
     * @param string the string to assert
     * @param substring the optional substring to assert (no substring assertion if <b>null</b>)
     */
    private static void assertString(String string, String substring) {
        Assert.assertNotNull(string);
        Assert.assertTrue(string.length() > 0);
        if (null != substring) {
            Assert.assertTrue(string.indexOf(substring) > 0);
        }
    }

}
