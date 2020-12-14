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

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Tests the factory/descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FactoryTest {
    
    /**
     * Creates a factory instance that does nothing. The instance delegates to {@link AasFactory#DUMMY} to keep
     * this instance in the testing loop.
     * 
     * @return the factory instance
     */
    public static AasFactory createDisabledFactory() {
        return new AasFactory() {
            
            @Override
            public Aas retrieveAas(String host, int port, String endpointPath, String urn) throws IOException {
                return DUMMY.retrieveAas(host, port, endpointPath, urn);
            }
            
            @Override
            public String getName() {
                return DUMMY.getName();
            }
            
            @Override
            public SubmodelBuilder createSubModelBuilder(String idShort) {
                return DUMMY.createSubModelBuilder(idShort);
            }
            
            @Override
            public DeploymentBuilder createDeploymentBuilder(String contextPath, String host, int port) {
                return DUMMY.createDeploymentBuilder(contextPath, host, port);
            }
            
            @Override
            public DeploymentBuilder createDeploymentBuilder(String host, int port) {
                return DUMMY.createDeploymentBuilder(host, port);
            }
            
            @Override
            public AasBuilder createAasBuilder(String idShort, String urn) {
                return DUMMY.createAasBuilder(idShort, urn);
            }
        };
        
    }
    
    /**
     * Tests that there is a fake factory through Java Service loader.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testFakeFactory() throws IOException {
        AasFactory instance = AasFactory.getInstance();
        Assert.assertNotNull(instance);

        // it's just a fake
        Assert.assertEquals("fake", instance.getName());
        Assert.assertNotNull(instance.createAasBuilder("", ""));
        Assert.assertNotNull(instance.createSubModelBuilder(""));
        Assert.assertNull(instance.retrieveAas("", 1234, "", ""));
        
        Assert.assertNull(instance.createDeploymentBuilder("localhost", 1234));
        Assert.assertNull(instance.createDeploymentBuilder("/path", "localhost", 1234));
    }

    /**
     * Tests explicitly setting the dummy factory.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDummyFactory() throws IOException {
        AasFactory old = AasFactory.setInstance(createDisabledFactory()); // otherwise service loader is consulted

        AasFactory instance = AasFactory.getInstance();
        Assert.assertNotNull(instance);
        Assert.assertEquals(AasFactory.DUMMY.getName(), instance.getName());

        Assert.assertNull(instance.createAasBuilder("", ""));
        Assert.assertNull(instance.createSubModelBuilder(""));
        Assert.assertNull(instance.retrieveAas("", 1234, "", ""));
        
        Assert.assertNull(instance.createDeploymentBuilder("localhost", 1234));
        Assert.assertNull(instance.createDeploymentBuilder("/path", "localhost", 1234));

        AasFactory.setInstance(old);
    }

}
