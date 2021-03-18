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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

import java.io.IOException;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;

/**
 * A faked factory that does nothing - just for testing. Do not rename, this class is referenced in 
 * {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeAasFactory extends AasFactory {

    /**
     * The factory descriptor for the Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    @ExcludeFirst
    public static class Descriptor implements AasFactoryDescriptor {

        @Override
        public AasFactory createInstance() {
            return new FakeAasFactory();
        }
        
    }
    
    @Override
    public String getName() {
        return "fake";
    }
    
    @Override
    public AasBuilder createAasBuilder(String idShort, String urn) {
        return new FakeAas.FakeAasBuilder(idShort, urn);
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort, String urn) {
        return new FakeSubmodel.FakeSubmodelBuilder(null, idShort, urn);
    }

    @Override
    public Server createRegistryServer(Endpoint endpoint, String... options) {
        return null;
    }

    @Override
    public Registry obtainRegistry(Endpoint regEndpoint) throws IOException {
        return null;
    }

    @Override
    public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint) {
        return null;
    }

    @Override
    public PersistenceRecipe createPersistenceRecipe() {
        return null;
    }

    @Override
    public String[] getProtocols() {
        return new String[]{DEFAULT_PROTOCOL};
    }

    @Override
    public InvocablesCreator createInvocablesCreator(String protocol, String host, int port) {
        return new FakeInvocablesCreator();
    }

    @Override
    public ProtocolServerBuilder createProtocolServerBuilder(String protocol, int port) {
        return new FakeProtocolServerBuilder();
    }

}
