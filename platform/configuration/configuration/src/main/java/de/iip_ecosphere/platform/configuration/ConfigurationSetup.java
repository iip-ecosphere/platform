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

package de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractConfiguration;

/**
 * The setup for the configuration component. As there are also EASy-Producer configuration classes with different 
 * meaning, we did not call this class "Configuration" or "ConfigurationConfiguration".
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationSetup extends AbstractConfiguration {

    public static final String PLATFORM_META_MODEL_NAME = "IIPEcosphere";
    private static ConfigurationSetup instance;
    private File base;
    private File genTarget;
    private File ivmlMetaModelFolder; 
    private File ivmlConfigFolder;
    private String ivmlModelName;

    /**
     * Creates an instance.
     */
    public ConfigurationSetup() {
        reset();
    }
    
    /**
     * Resets the setup to default values, e.g., for testing.
     */
    public void reset() {
        base = new File(".");
        genTarget = new File("gen");
        ivmlMetaModelFolder = new File("src/main/easy"); 
        ivmlConfigFolder = null;
        ivmlModelName = PLATFORM_META_MODEL_NAME;
    }
    
    /**
     * Returns the IVML model name.
     * 
     * @return the IVML model name, by default "IIPEcosphere"
     */
    public String getIvmlModelName() {
        return ivmlModelName;
    }
    
    /**
     * Returns the target base for making relative artifact paths absolute.
     * 
     * @return the base folder, by default {@code .}.
     */
    public File getBase() {
        return base;
    }
    
    /**
     * Returns the target folder for artifact generation. Shall be within {@link #getBase()}.
     * 
     * @return the target folder, by default {@code gen}.
     */
    public File getGenTarget() {
        return genTarget;
    }

    /**
     * Returns the IVML folder containing the platform meta model.
     * 
     * @return the IVML folder folder, by default {@code src/main/easy}.
     */
    public File getIvmlMetaModelFolder() {
        return ivmlMetaModelFolder;
    }

    /**
     * Returns the IVML configuration folder containing the platform configuration.
     * 
     * @return the IVML configuration folder if different from {@link #getIvmlMetaModelFolder()}, 
     *     by default <b>null</b>.
     */
    public File getIvmlConfigFolder() {
        return ivmlConfigFolder;
    }

    /**
     * Returns the IVML model name. [required by SnakeYaml]
     * 
     * @param ivmlModelName the IVML model name
     */
    public void setIvmlModelName(String ivmlModelName) {
        this.ivmlModelName = ivmlModelName;
    }
    
    /**
     * Defines the base folder for making relative paths absolute. [required by SnakeYaml]
     * 
     * @param base the base folder.
     */
    public void setBase(File base) {
        this.base = base;
    }

    /**
     * Defines the target folder for artifact generation. [required by SnakeYaml]
     * 
     * @param genTarget the target folder.
     */
    public void setGenTarget(File genTarget) {
        this.genTarget = genTarget;
    }

    /**
     * Defines the IVML meta model folder containing the platform meta model. [required by SnakeYaml]
     * 
     * @param ivmlMetaModelFolder the IVML meta model folder
     */
    public void setIvmlMetaModelFolder(File ivmlMetaModelFolder) {
        this.ivmlMetaModelFolder = ivmlMetaModelFolder;
    }

    /**
     * Defines the IVML configuration folder containing the platform configuration. [required by SnakeYaml]
     * 
     * @param ivmlConfigFolder the IVML configuration folder, shall be <b>null</b> if it is the same 
     * as {@link #getIvmlMetaModelFolder()}, ignored if given and the same as {@link #getIvmlMetaModelFolder()}   
     */
    public void setIvmlConfigFolder(File ivmlConfigFolder) {
        if (null == ivmlConfigFolder || (null != ivmlConfigFolder && !ivmlMetaModelFolder.equals(ivmlConfigFolder))) {
            this.ivmlConfigFolder = ivmlConfigFolder;
        }
    }

    /**
     * Returns the configuration instance, may read it the first time from a default "configuration.yml" file in the 
     * root folder of the containing jar. 
     *
     * @return the configuration instance
     */
    public static ConfigurationSetup getConfiguration() {
        if (null == instance) {
            try {
                instance = readFromYaml(ConfigurationSetup.class, "/configuration.yml");
            } catch (IOException e) {
                LoggerFactory.getLogger(ConfigurationSetup.class).error(e.getMessage(), e);
                instance = new ConfigurationSetup();
            }
        }
        return instance;
    }

}
