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
import java.util.List;

/**
 * Settings for EASy-Producer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EasySetup {

    public static final String PLATFORM_META_MODEL_NAME = "IIPEcosphere";

    private File base;
    private File genTarget;
    private File ivmlMetaModelFolder; 
    private File ivmlConfigFolder;
    private List<File> additionalIvmlFolders;
    private String ivmlModelName;
    private EasyLogLevel logLevel = EasyLogLevel.NORMAL;
    
    /**
     * Resets the setup to default values, e.g., for testing.
     */
    public void reset() {
        base = new File(".");
        genTarget = new File("gen");
        ivmlMetaModelFolder = new File("model");
        if (!ivmlMetaModelFolder.exists()) {
            ivmlMetaModelFolder = new File("src/main/easy");
        }
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
     * Returns the IVML folder containing the platform meta model as well as the VIL and VTL scripts.
     * 
     * @return the IVML folder folder, by default {@code model} if it exists or {@code src/main/easy}.
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
     * Defines the IVML meta model folder containing the platform meta model. This folder is not
     * interpreted relative to {@link #getBase()}. [required by SnakeYaml]
     * 
     * @param ivmlMetaModelFolder the IVML meta model folder
     */
    public void setIvmlMetaModelFolder(File ivmlMetaModelFolder) {
        this.ivmlMetaModelFolder = ivmlMetaModelFolder;
    }

    /**
     * Defines the IVML configuration folder containing the platform configuration. This folder is not
     * interpreted relative to {@link #getBase()}. [required by SnakeYaml]
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
     * Returns whether EASy-Producer verbose output, in particular during startup, shall be emitted.
     * 
     * @return {@code true} for verbose output, {@code false} else   
     */
    public EasyLogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Defines whether EASy-Producer verbose output, in particular during startup, shall be emitted. 
     * [required by SnakeYaml]
     * 
     * @param logLevel the easy logging level   
     */
    public void setLogLevel(EasyLogLevel logLevel) {
        this.logLevel = logLevel;
    }
    
    /**
     * Returns additional IVML folders to be considered when loading the configuration model. Usually, additional IVML
     * folders are only used in very specialized (test) setups, e.g., to reuse a part of the model while the remainder 
     * of the (managed) configuration is for separation purposes in a different folder on the same level.
     * 
     * @return the additional IVML folders, may be <b>null</b> for undefined
     */
    public List<File> getAdditionalIvmlFolders() {
        return additionalIvmlFolders;
    }
    
    /**
     * Returns additional IVML folders to be considered when loading the configuration model. Usually, additional IVML
     * folders are only used in very specialized (test) setups, e.g., to reuse a part of the model while the remainder 
     * of the (managed) configuration is for separation purposes in a different folder on the same level.
     * 
     * @param additionalIvmlFolders the additional IVML folders (may be <b>null</b> for none, or empty)
     */
    public void setAdditionalIvmlFolders(List<File> additionalIvmlFolders) {
        this.additionalIvmlFolders = additionalIvmlFolders;
    }

}
