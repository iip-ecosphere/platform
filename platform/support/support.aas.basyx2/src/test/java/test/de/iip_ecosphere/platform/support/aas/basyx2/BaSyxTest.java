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

package test.de.iip_ecosphere.platform.support.aas.basyx2;

import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxAasFactory;
import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxLocalServer;
import de.iip_ecosphere.platform.support.aas.Type;
import test.de.iip_ecosphere.platform.support.aas.AasTest;

/**
 * Tests the AAS abstraction implementation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTest extends AasTest {

    @Override
    public String[] getServerProtocols() {
        return new String[] {"", BaSyxAasFactory.PROTOCOL_AAS_REST}; 
        // TODO SSL, override getAasServerAddressSchema and getKeyStoreDescriptor
    }
    
    @Override
    protected Endpoint createDependentEndpoint(ServerAddress address, String endpoint) {
        return new Endpoint(address.getSchema(), endpoint); // turn to emphemeral, v3 requires 4 servers
    }
    
    /**
     * Tests lazy bindings. For BaSyx2, same behavior as non-lazy as getters/setters do not exist anymore
     * and operations are bound by qualifier rather than serializable functors.
     */
    @Test
    public void testLazy() { 
        AasBuilder aas = AasFactory.getInstance().createAasBuilder(NAME_AAS, null);
        SubmodelBuilder sm = aas.createSubmodelBuilder(NAME_SUBMODEL, null);
        
        sm.createPropertyBuilder("prop").setType(Type.INT32).bindLazy(
            Invokable.createSerializableInvokable(() -> null), null).build();
        
        sm.createPropertyBuilder("prop").setType(Type.BOOLEAN)
            .bindLazy(null, Invokable.createSerializableInvokable(v -> { })).build();

        sm.createOperationBuilder("op").setInvocableLazy(Invokable.createSerializableInvokable(p -> null)).build();

        sm.build();
        aas.build();
    }
    
    @Override
    @Test
    public void testIllegalShortId() {
        // So far, none known in contrast to BaSyX 1. Overriding behavior.
    }
    
    /**
     * Tests starting/stopping the BaSyx servers.
     */
    @Ignore("Just for development")
    @Test
    public void testServers() {
        BasicSetupSpec spec = new BasicSetupSpec(new Endpoint(Schema.HTTP, ""), new Endpoint(Schema.HTTP, ""), 
            new Endpoint(Schema.HTTP, ""), new Endpoint(Schema.HTTP, ""));
        BaSyxLocalServer server = new BaSyxLocalServer(spec, BaSyxLocalServer.ServerType.COMBINED, 
            LocalPersistenceType.INMEMORY);
        server.start();
        TimeUtils.sleep(3000);
        server.stop(false);
    }

}
