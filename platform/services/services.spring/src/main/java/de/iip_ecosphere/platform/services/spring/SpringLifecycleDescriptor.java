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

package de.iip_ecosphere.platform.services.spring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * The lifecycle descriptor for the spring cloud service manager. Requires service management implementation and AAS 
 * implementation to be hooked in properly via JSL.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootApplication(scanBasePackageClasses = SpringLifecycleDescriptor.class)
public class SpringLifecycleDescriptor implements LifecycleDescriptor {

    @Override
    public void startup(String[] args) {
        SpringApplication.run(SpringLifecycleDescriptor.class, args);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Thread getShutdownHook() {
        return null; // not needed
    }
    
    /**
     * Brings the startup application listener into this context and fills {@link SpringInstances}.
     * 
     * @author Holger Eichelberger, SSE
     */
    @Component
    class Startup extends StartupApplicationListener {
        // brings the startup application listener in this context an
    }
    
    /**
     * Basic execution of the service manager. 
     * 
     * @param ctx the application context
     * @return the runner instance
     */
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("IIP-Ecosphere Service Manager (Spring Cloud Streams).");
            System.out.println("Configuration: " + SpringInstances.getConfig().getClass().getName());
            System.out.println("Deployer: " + SpringInstances.getDeployer().getClass().getName());
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
        };
    }
    
    /**
     * Just for testing.
     * 
     * @param args command line arguments, may be implementation specific
     */
    public static void main(String[] args) {
        SpringLifecycleDescriptor desc = new SpringLifecycleDescriptor();
        desc.startup(args);
    }

}
