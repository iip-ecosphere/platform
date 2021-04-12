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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Performs instance initialization work with {@link SpringInstances}. Must be executed before first use 
 * of {@link SpringCloudServiceManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private AppDeployer deployer;
    
    @Autowired
    private SpringCloudServiceConfiguration config;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        SpringInstances.setDeployer(deployer);
        SpringInstances.setConfig(config);
    }

}
