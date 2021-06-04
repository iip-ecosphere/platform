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

package test.de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Tests the test AAS pretending that there is an environment to test against. The AAS is used in 
 * {@link AbstractEnvironmentTest} to test the real environment implementations.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasTest {

    private static ServiceState state = ServiceState.CREATED; 
    
    /**
     * Tests the AAS.
     * 
     * @throws IOException if accessing the AAS fails
     * @throws ExecutionException if accessing AAS property values or performing operation invocations fails
     */
    @Test
    public void testAas() throws IOException, ExecutionException {
        ServerAddress vabServer = new ServerAddress(Schema.HTTP);
        ServerAddress aasServer = new ServerAddress(Schema.HTTP); 
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

        Aas aas = AasCreator.createAas(vabServer);
        
        ProtocolServerBuilder pBuilder = AasFactory.getInstance()
            .createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, vabServer.getPort());
        pBuilder.defineProperty(AasCreator.AAS_SUBMODEL_PROPERTY_NAME, () -> "MyService", null);
        pBuilder.defineProperty(AasCreator.AAS_SUBMODEL_PROPERTY_VERSION, () -> "1.2.3", null);
        pBuilder.defineProperty(AasCreator.AAS_SUBMODEL_PROPERTY_STATE, () -> state.name(), null);
        pBuilder.defineProperty(AasCreator.AAS_SUBMODEL_PROPERTY_DESCRIPTION, () -> "Some Service", null);
        pBuilder.defineOperation(AasCreator.AAS_SUBMODEL_OPERATION_ACTIVATE, params -> activate());
        pBuilder.defineOperation(AasCreator.AAS_SUBMODEL_OPERATION_PASSIVATE, params -> passivate());
        pBuilder.defineOperation(AasCreator.AAS_SUBMODEL_OPERATION_SETSTATE, params -> setState(params));
        Server server = pBuilder.build();
        server.start();
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
        
        AbstractEnvironmentTest.testAas(aasServerRegistry);

        httpServer.stop(true);
        server.stop(true);
    }
    
    /**
     * Activates the service.
     * 
     * @return <b>null</b> for convenience
     */
    private static Object activate() {
        if (state == ServiceState.PASSIVATED) {
            state = ServiceState.RUNNING;
        }
        return null;
    }

    /**
     * Passivates the service.
     * 
     * @return <b>null</b> for convenience
     */
    private static Object passivate() {
        if (state == ServiceState.RUNNING) {
            state = ServiceState.PASSIVATED;
        }
        return null;
    }

    /**
     * Changes the service state.
     * 
     * @param params the call parameters, only the first is evaluated
     * @return if the operation was successful
     */
    private static Boolean setState(Object[] params) {
        boolean result = false;
        if (params.length > 0 && params[0] != null) {
            try {
                state = ServiceState.valueOf(params[0].toString());
                result = true;
            } catch (IllegalArgumentException e) {
                // result = false;
            }
        }
        return result;
    }

}
