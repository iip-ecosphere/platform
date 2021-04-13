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

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * The basic ECS lifecycle descriptor for powering up the AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsLifecycleDescriptor implements LifecycleDescriptor {

    @Override
    public void startup(String[] args) {
        System.out.println("IIP-Ecosphere ECS Runtime.");
        if (AasFactory.isFullInstance()) {
            AasPartRegistry.setAasEndpoint(new Endpoint(Schema.HTTP, AasPartRegistry.DEFAULT_ENDPOINT));
            AasPartRegistry.setProtocolAddress(new ServerAddress(Schema.TCP));
            AasPartRegistry.AasBuildResult res = AasPartRegistry.build();
            
            // active AAS require two server instances and a deployment
            Server implServer = res.getProtocolServerBuilder().build();
            implServer.start();
            // TODO remote deployment, destination to be defined via JAML/AasPartRegistry
            Server aasServer = AasPartRegistry.deploy(res.getAas()); 
            aasServer.start();
        } else {
            System.out.println("No full AAS implementation registered. Cannot build up Services AAS. Please add an "
                + "appropriate dependency.");
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }

}
