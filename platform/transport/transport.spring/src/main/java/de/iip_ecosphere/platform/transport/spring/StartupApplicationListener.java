/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Implements operations to be carried out at application start, e.g., setting up configured serializers or the
 * transport factory. In some contexts, we experienced that {@code SpringBootApplication.scanBasePackageClasses} on 
 * this package disturbs the spring initialization, in particular spring cloud multi-binder settings. Then the 
 * underlying functionality can be utilized directly through {@link RegistrationHelper} or this class can be subclassed
 * in the application and marked as component.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
@EnableConfigurationProperties({SerializerConfiguration.class, TransportFactoryConfiguration.class})
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    
    @Autowired
    private SerializerConfiguration cfg;
    @Autowired
    private TransportFactoryConfiguration transportCfg;
    private boolean done = false;
 
    @Override 
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!done) {
            RegistrationHelper.registerSerializers(cfg);
            RegistrationHelper.configureTransportFactory(transportCfg);
            done = true;
        }
    }
    
}
