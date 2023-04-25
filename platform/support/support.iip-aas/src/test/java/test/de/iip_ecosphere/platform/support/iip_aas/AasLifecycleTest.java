/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.util.function.Supplier;

import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Tests {@link AbstractAasLifecycleDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasLifecycleTest {

    /**
     * A test lifecycle descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyAasLifecycle extends AbstractAasLifecycleDescriptor {

        /**
         * Creates a descriptor instance.
         * 
         * @param name the name of the AAS to build for logging
         * @param setupSupplier the {@link AasSetup} supplier
         */
        protected MyAasLifecycle(String name, Supplier<AasSetup> setupSupplier) {
            super(name, setupSupplier);
        }
        
    }
    
    /**
     * Tests a basic (empty) AAS lifecycle descriptor.
     */
    @Test
    public void testLifecycle() {
        boolean waitFor = AbstractAasLifecycleDescriptor.setWaitForIipAas(false);
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(aasSetup);

        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        Server registryServer = rcp
            .createRegistryServer(regEndpoint, ServerRecipe.LocalPersistenceType.INMEMORY)
            .start();
        AasServer aasServer = rcp
            .createAasServer(aasSetup.getServerEndpoint(), ServerRecipe.LocalPersistenceType.INMEMORY, regEndpoint)
            .start();

        MyAasLifecycle lifecycleDesc = new MyAasLifecycle("test", () -> aasSetup);
        lifecycleDesc.startup(new String[0]);
        lifecycleDesc.shutdown();

        aasServer.stop(true);
        registryServer.stop(true);

        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
        AbstractAasLifecycleDescriptor.setWaitForIipAas(waitFor);
    }

}
