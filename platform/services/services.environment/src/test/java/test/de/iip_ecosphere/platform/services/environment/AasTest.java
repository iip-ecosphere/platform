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

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceMapper;
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
        MyService service = new MyService();
        pBuilder.defineProperty(ServiceMapper.NAME_PROP_NAME, () -> service.getName(), null);
        pBuilder.defineProperty(ServiceMapper.NAME_PROP_VERSION, () -> service.getVersion().toString(), null);
        pBuilder.defineProperty(ServiceMapper.NAME_PROP_STATE, () -> service.getState().name(), null);
        pBuilder.defineProperty(ServiceMapper.NAME_PROP_DESCRIPTION, () -> service.getDescription(), null);
        pBuilder.defineOperation(ServiceMapper.NAME_OP_ACTIVATE, params -> activate(service));
        pBuilder.defineOperation(ServiceMapper.NAME_OP_PASSIVATE, params -> passivate(service));
        pBuilder.defineOperation(ServiceMapper.NAME_OP_SET_STATE, params -> setState(service, params));
        Server server = pBuilder.build();
        server.start();
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
        
        AbstractEnvironmentTest.testAas(aasServerRegistry, service);

        httpServer.stop(true);
        server.stop(true);
    }
    
    /**
     * Activates the service.
     * 
     * @param service the service instance
     * @return <b>null</b> for convenience
     */
    private static Object activate(Service service) {
        try {
            service.activate();
        } catch (ExecutionException e) {
            // ignore for now, will disappear when aligned with Python
        }
        return null;
    }

    /**
     * Passivates the service.
     * 
     * @param service the service instance
     * @return <b>null</b> for convenience
     */
    private static Object passivate(Service service) {
        try {
            service.passivate();
        } catch (ExecutionException e) {
            // ignore for now, will disappear when aligned with Python
        }
        return null;
    }

    /**
     * Changes the service state.
     *
     * @param service the service instance
     * @param params the call parameters, only the first is evaluated
     * @return if the operation was successful
     */
    private static Object setState(Service service, Object[] params) {
        if (params.length > 0 && params[0] != null) {
            try {
                service.setState(ServiceState.valueOf(params[0].toString()));
            } catch (IllegalArgumentException e) {
                // result = false;
            } catch (ExecutionException e) {
                // ignore for now, will disappear when aligned with Python
            }
        }
        return null;
    }

}
