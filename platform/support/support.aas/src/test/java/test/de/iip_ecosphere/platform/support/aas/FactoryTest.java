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
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
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
            public SubmodelBuilder createSubmodelBuilder(String idShort) {
                return DUMMY.createSubmodelBuilder(idShort);
            }
            
            @Override
            public DeploymentRecipe createDeploymentRecipe(String contextPath, String host, int port) {
                return DUMMY.createDeploymentRecipe(contextPath, host, port);
            }
            
            @Override
            public DeploymentRecipe createDeploymentRecipe(String host, int port) {
                return DUMMY.createDeploymentRecipe(host, port);
            }
            
            @Override
            public AasBuilder createAasBuilder(String idShort, String urn) {
                return DUMMY.createAasBuilder(idShort, urn);
            }

            @Override
            public PersistenceRecipe createPersistenceRecipe() {
                return DUMMY.createPersistenceRecipe();
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
        Assert.assertNotNull(instance.createSubmodelBuilder(""));
        Assert.assertNull(instance.retrieveAas("", 1234, "", ""));
        
        Assert.assertNull(instance.createDeploymentRecipe("localhost", 1234));
        Assert.assertNull(instance.createDeploymentRecipe("/path", "localhost", 1234));

        Assert.assertNull(instance.createPersistenceRecipe());
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
        Assert.assertNull(instance.createSubmodelBuilder(""));
        Assert.assertNull(instance.retrieveAas("", 1234, "", ""));
        
        Assert.assertNull(instance.createDeploymentRecipe("localhost", 1234));
        Assert.assertNull(instance.createDeploymentRecipe("/path", "localhost", 1234));

        AasFactory.setInstance(old);
    }

}
