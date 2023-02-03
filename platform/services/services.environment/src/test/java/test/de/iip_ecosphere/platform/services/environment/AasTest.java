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

import de.iip_ecosphere.platform.services.environment.ServiceMapper;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import test.de.iip_ecosphere.platform.services.environment.AasCreator.AasResult;

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

        MyService service = new MyService();
        AasResult res = new AasResult();
        Aas aas = AasCreator.createAas(vabServer, service, AasFactory.DEFAULT_PROTOCOL, res);
        
        ProtocolServerBuilder pBuilder = AasFactory.getInstance()
            .createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, vabServer.getPort());

        ServiceMapper mapper = new ServiceMapper(pBuilder);
        mapper.mapService(service);
        Server server = pBuilder.build();
        server.start();
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry)
            .deploy(aas)
            .createServer()
            .start();
        
        AbstractEnvironmentTest.testAas(aasServerRegistry, service);
        AbstractEnvironmentTest.testAasResult(res, service);

        httpServer.stop(true);
        server.stop(true);
    }

}
