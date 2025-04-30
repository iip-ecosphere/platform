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

package test.de.iip_ecosphere.platform.support.aas.basyx;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxAasFactory;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import test.de.iip_ecosphere.platform.support.TestUtils;
import test.de.iip_ecosphere.platform.support.aas.AasTest;

/**
 * Tests the AAS abstraction implementation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTest extends AasTest {

    @Override
    protected KeyStoreDescriptor getKeyStoreDescriptor(String protocol) {
        KeyStoreDescriptor result = null;
        if (BaSyxAasFactory.PROTOCOL_VAB_HTTPS.equals(protocol)) {
            File f = new File("./src/test/resources/keystore.jks");
            System.out.println("Using Keystore: " + f.getAbsolutePath() + " " + f.exists());
            result = new KeyStoreDescriptor(f, "a1234567", "tomcat"); // tomcat required by BaSyx
        }
        return result;
    }
    
    @Override
    protected Schema getAasServerAddressSchema(String serverProtocol) {
        return serverProtocol.length() > 0 ? Schema.HTTPS : Schema.HTTP; // works here, but too much assumption
    }    

    @Override
    protected boolean excludeProtocol(String protocol) {
        boolean exclude = false;
        if (BaSyxAasFactory.PROTOCOL_VAB_HTTPS.equals(protocol)) {
            // currently it's unclear why VAB-HTTPS works on Windows but not on Linux while HTTPS-AAS works
            exclude = TestUtils.isSseCI();
            System.out.println("Checking exclusion: " + NetUtils.getOwnHostname() + " " + exclude);            
        }
        return exclude;
    }

    @Override
    public String[] getServerProtocols() {
        return new String[] {"", BaSyxAasFactory.PROTOCOL_VAB_HTTPS};
    }

    /**
     * Tests lazy bindings.
     */
    @Test
    public void testLazy() {
        AasBuilder aas = AasFactory.getInstance().createAasBuilder(NAME_AAS, null);
        SubmodelBuilder sm = aas.createSubmodelBuilder(NAME_SUBMODEL, null);
        
        try {
            sm.createPropertyBuilder("prop").setType(Type.INT32)
                .bind(Invokable.createInvokable(() -> null), null).build();
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        sm.createPropertyBuilder("prop").setType(Type.INT32)
            .bindLazy(Invokable.createInvokable(() -> null), null).build();
        
        try {
            sm.createPropertyBuilder("prop").setType(Type.BOOLEAN)
                .bind(null, Invokable.createInvokable(v -> { })).build();
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        sm.createPropertyBuilder("prop").setType(Type.BOOLEAN)
            .bindLazy(null, Invokable.createInvokable(v -> { })).build();

        try {
            sm.createPropertyBuilder("prop").bind(null, null).build();
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }

        try {
            sm.createOperationBuilder("op").setInvocable(Invokable.createInvokable((p) -> null)).build();
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        sm.createOperationBuilder("op").setInvocableLazy(Invokable.createInvokable((p) -> null)).build();

        sm.build();
        aas.build();
    }

}
