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

package de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.IOException;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * The setup for the configuration component. As there are also EASy-Producer configuration classes with different 
 * meaning, we did not call this class "Configuration" or "ConfigurationConfiguration".
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationSetup extends de.iip_ecosphere.platform.configuration.cfg.ConfigurationSetup {

    private static ConfigurationSetup instance;
    private EasySetup easyProducer = new EasySetup();

    /**
     * Creates an instance.
     */
    public ConfigurationSetup() {
        easyProducer.reset();
    }
    
    /**
     * Returns the EASy-Producer setup.
     * 
     * @return the setup
     */
    public EasySetup getEasyProducer() {
        return easyProducer;
    }

    /**
     * Returns the EASy-Producer setup. [snakyaml]
     * 
     * @param easyProducer the storage setup
     */
    public void setEasyProducer(EasySetup easyProducer) {
        this.easyProducer = easyProducer;
    }

    /**
     * Returns the configuration instance, may read it the first time from a default "configuration.yml" file in the 
     * root folder of the containing jar. 
     *
     * @return the configuration instance
     */
    public static ConfigurationSetup getSetup() {
        return getSetup(true);
    }

    /**
     * Returns the configuration instance, may read it the first time from a default "configuration.yml" file in the 
     * root folder of the containing jar. 
     *
     * @param log do log or stay quiet
     * @return the configuration instance
     */
    public static ConfigurationSetup getSetup(boolean log) {
        if (null == instance) {
            try {
                instance = readFromYaml(ConfigurationSetup.class);
            } catch (IOException e) {
                if (log) {
                    LoggerFactory.getLogger(ConfigurationSetup.class).warn(
                        "Fallback to default config: {}", e.getMessage());
                }
                instance = new ConfigurationSetup();
            }
        }
        return instance;
    }

}
