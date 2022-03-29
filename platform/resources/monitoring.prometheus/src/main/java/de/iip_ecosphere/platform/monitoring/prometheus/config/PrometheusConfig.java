package de.iip_ecosphere.platform.monitoring.prometheus.config;
/** 
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

import java.io.File;

/** Java-Representation of prometheus.yml config-file.
 * 
 * @author bettelsc
 *
 */
public class PrometheusConfig {

    private String fileName;
    private String path;
    private File file;
    
    /**
     * Default constructor.
     */
    public PrometheusConfig() {
    }
    /**
     * Constructor.
     * @param fileName
     * @param path
     * @param file
     */
    public PrometheusConfig(String fileName, String path, File file) {
        this.fileName = fileName;
        this.path = path;
        this.file = file;
    }
    /** Copies the default prometheus.yml
     *  desired output-file should be something like "iip-ecosphere-prometheus.yml".
     *  
     * @param pathToDefaultFile
     * @param defaultFileName
     * 
     * @return file
     */
    public File copyDefault(String pathToDefaultFile, String defaultFileName) {
        //copy the default file from
        return file;
    }
    /** getter for the Filename of the Prometheus config-File.
     * 
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * setter for the Filename of the Prometheus config-File.
     * 
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * getter for the Prometheus config-File.
     * 
     * @return file
     */
    public File getFile() {
        return file;
    }
    /**
     * setter for the prometheus config-file.
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }
    /**
     * getter for the path of prometheus config-file.
     * @return path
     */
    public String getPath() {
        return path;
    }
    /**
     * setter for the path of prometheus config-file.
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }
    
}