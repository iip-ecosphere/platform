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

import java.util.List;

import org.springframework.cloud.deployer.spi.app.AppDeployer;

import de.iip_ecosphere.platform.services.ServiceFactory;

/**
 * Allows bridging the Spring world with components loaded via JSL without that Spring creates the instance. 
 * Must be initialized before use of {@link SpringCloudServiceManager}, e.g., through 
 * {@link StartupApplicationListener}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringInstances {

    private static AppDeployer deployer;
    private static SpringCloudServiceSetup config;
    private static List<String> serviceCmdArgs;

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
    public static SpringCloudServiceSetup getConfig() {
        return config;
    }
    
    /**
     * Defines the deployer instance.
     * 
     * @param depl the deployer instance
     */
    static void setDeployer(AppDeployer depl) {
        deployer = depl;
        ClasspathJavaCommandBuilder.installInto(deployer);
    }

    /**
     * Defines the configuration instance.
     * 
     * @param cfg the configuration instance
     */
    public static void setConfig(SpringCloudServiceSetup cfg) {
        config = cfg;
        if (null != cfg) {
            ServiceFactory.setAasSetup(cfg.getAas());
            ServiceFactory.setNetworkManagerSetup(cfg.getNetMgr());
            if (null != serviceCmdArgs) {
                config.setServiceCmdArgs(serviceCmdArgs);
            }
        }
    }
    
    /**
     * Caching helper methods to allow early setting the additional service command arguments, e.g., for testing.
     * May be set later when {@code #setConfig(SpringCloudServiceSetup)} is called.
     * 
     * @param arguments the arguments
     */
    public static void setServiceCmdArgs(List<String> arguments) {
        if (null == config) {
            serviceCmdArgs = arguments;
        } else {
            config.setServiceCmdArgs(arguments);
        }
    }

}
