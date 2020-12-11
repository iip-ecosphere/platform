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

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.DeploymentBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

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
    public SubmodelBuilder createSubModelBuilder(String idShort) {
        return new FakeSubmodel.FakeSubmodelBuilder(null, idShort);
    }

    @Override
    public Aas retrieveAas(String host, int port, String endpointPath, String urn) throws IOException {
        return null;
    }

    @Override
    public DeploymentBuilder createDeploymentBuilder(String host, int port) {
        return null;
    }

    @Override
    public DeploymentBuilder createDeploymentBuilder(String contextPath, String host, int port) {
        return null;
    }

}
