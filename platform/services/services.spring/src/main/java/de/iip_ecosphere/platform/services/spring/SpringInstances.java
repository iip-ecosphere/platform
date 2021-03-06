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

import org.springframework.cloud.deployer.spi.app.AppDeployer;

import de.iip_ecosphere.platform.services.ServiceFactory;

/**
 * Allows bridging the Spring world with components loaded via JSL without that Spring creates the instance. 
 * Must be initialized before use of {@link SpringCloudServiceManager}, e.g., through 
 * {@link StartupApplicationListener}.
 * 
 * @author Holger Eichelberger, SSE
 */
class SpringInstances {

    private static AppDeployer deployer;
    
    private static SpringCloudServiceConfiguration config;

    /**
     * Returns the deployer instance.
     * 
     * @return the deployer instance
     */
    static AppDeployer getDeployer() {
        return deployer;
    }
 
    /**
     * Returns the configuration instance.
     * 
     * @return the configuration instance
     */
    static SpringCloudServiceConfiguration getConfig() {
        return config;
    }

    /**
     * Defines the deployer instance.
     * 
     * @param depl the deployer instance
     */
    static void setDeployer(AppDeployer depl) {
        deployer = depl;
    }

    /**
     * Defines the configuration instance.
     * 
     * @param cfg the configuration instance
     */
    static void setConfig(SpringCloudServiceConfiguration cfg) {
        config = cfg;
        ServiceFactory.setAasSetup(cfg.getAas());
    }

}
