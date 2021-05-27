/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx.server;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.aas.basyx.server.BaSyxFullServerRecipe;
import de.iip_ecosphere.platform.support.aas.basyx.server.BaSyxFullServerRecipe.ServerPersistenceType;

/**
 * Tests {@link BaSyxFullServerRecipe}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxFullServerRecipeTest {

    /**
     * Just an unknown constant.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum WrongPersistenceType implements PersistenceType {
        WRONG;
    }
    
    /**
     * Template test.
     */
    @Test
    public void testRecipe() {
        ServerRecipe recipe = AasFactory.getInstance().createServerRecipe();
        Assert.assertNotNull(recipe);
        Assert.assertTrue(recipe instanceof BaSyxFullServerRecipe);
        
        Endpoint serverEp = new Endpoint(new ServerAddress(Schema.HTTP), "aas");
        Endpoint regEp = new Endpoint(new ServerAddress(Schema.HTTP), "registry");
        try {
            recipe.createRegistryServer(regEp, null, "");
            Assert.fail("PersistenceType not handled");
        } catch (UnsupportedOperationException e) {
            // this is ok
        }
        
        Server regServer = recipe.createRegistryServer(regEp, LocalPersistenceType.INMEMORY);
        regServer.start();
        
        try {
            recipe.createAasServer(serverEp, WrongPersistenceType.WRONG, regEp, "");
            Assert.fail("PersistenceType not handled");
        } catch (UnsupportedOperationException e) {
            // this is ok
        }
        
        AasServer aasServer = recipe.createAasServer(serverEp, LocalPersistenceType.INMEMORY, regEp);
        aasServer.start();
        
        // deployments would be anyway local...
        
        aasServer.stop(true);
        regServer.stop(true);
    }
    
    /**
     * Tests the persistence type translation.
     */
    @Test
    public void testPersistenceType() {
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Assert.assertEquals(LocalPersistenceType.INMEMORY, rcp.toPersistenceType("")); // fallback
        Assert.assertEquals(LocalPersistenceType.INMEMORY, rcp.toPersistenceType(LocalPersistenceType.INMEMORY.name()));
        Assert.assertEquals(ServerPersistenceType.MONGO, 
            rcp.toPersistenceType(ServerPersistenceType.MONGO.name()));
        Assert.assertEquals(ServerPersistenceType.MONGO, 
            rcp.toPersistenceType(ServerPersistenceType.MONGO.name().toLowerCase()));
    }

    
}
