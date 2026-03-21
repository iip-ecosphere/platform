/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.cfg;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Maps the specified configuration to a dashboard specification.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DashboardMapper {
    
    /**
     * Represents the mapper parameters.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MapperParams {
        
        private String mainModelName;
        private File modelFolder;
        private File metaModelFolder; 
        private File outputFile; 
        private String pluginId; 
        private String postUrl;
        private boolean inContainer = true;

        /**
         * Creates a params instance.
         * 
         * @param mainModelName the name of the configuration model to load/parse
         * @param modelFolder the model folder
         */
        public MapperParams(String mainModelName, File modelFolder) {
            setMainModelName(mainModelName);
            setModelFolder(modelFolder);
        }

        /**
         * Returns the main model name.
         * 
         * @return the name of the configuration model to load/parse
         */
        public String getMainModelName() {
            return mainModelName;
        }

        /**
         * Defines the main model name.
         * 
         * @param mainModelName the name of the configuration model to load/parse
         * @return <b>this</b> for chaining
         */
        public MapperParams setMainModelName(String mainModelName) {
            this.mainModelName = mainModelName;
            return this;
        }

        /**
         * Returns the model folder.
         * 
         * @return the folder where the configuration model can be found
         */
        public File getModelFolder() {
            return modelFolder;
        }

        /**
         * Defines the model folder.
         * 
         * @param modelFolder the folder where the configuration model can be found
         * @return <b>this</b> for chaining
         */
        public MapperParams setModelFolder(File modelFolder) {
            this.modelFolder = modelFolder;
            return this;
        }

        /**
         * Returns the metamodel folder.
         * 
         * @return the optional folder where the meta model can be found, may be <b>null</b> for none
         */
        public File getMetaModelFolder() {
            return metaModelFolder;
        }

        /**
         * Defines the metamodel folder.
         * 
         * @param metaModelFolder optional folder where the meta model can be found, may be <b>null</b> for none
         * @return <b>this</b> for chaining
         */
        public MapperParams setMetaModelFolder(File metaModelFolder) {
            this.metaModelFolder = metaModelFolder;
            return this;
        }

        /**
         * Returns the output file.
         * 
         * @return optional output file where the dashboard specification shall be wrote to, if <b>null</b> a 
         *     default location will be assumed
         */
        public File getOutputFile() {
            return outputFile;
        }

        /**
         * Defines the output file.
         * @param outputFile optional output file where the dashboard specification shall be wrote to, if <b>null</b> a 
         *     default location will be assumed
         * @return <b>this</b> for chaining
         */
        public MapperParams setOutputFile(File outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        /**
         * Returns the plugin id.
         * 
         * @return the optional plugin id to use (may be <b>null</b> for the default, must be loaded before)
         */
        public String getPluginId() {
            return pluginId;
        }

        /**
         * Sets the plugin id.
         * 
         * @param pluginId the optional plugin id to use (may be <b>null</b> for the default, must be loaded before)
         * @return <b>this</b> for chaining
         */
        public MapperParams setPluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        /**
         * Returns whether a pluginId was defined.
         * 
         * @return {@code true} for there is a pluginId, {@code false} else 
         */
        public boolean hasPluginId() {
            return null != pluginId && pluginId.length() > 0;
        }

        /**
         * Returns the REST API URL.
         * 
         * @return the URL for posting the result directly to a HTTP/REST API, may be <b>null</b> or empty for none
         */
        public String getPostUrl() {
            return postUrl;
        }

        /**
         * Sets the REST API URL.
         * 
         * @param postUrl the URL for posting the result directly to a HTTP/REST API, may be <b>null</b> or empty 
         *     for none
         * @return <b>this</b> for chaining
         */
        public MapperParams setPostUrl(String postUrl) {
            this.postUrl = postUrl;
            return this;
        }
        
        /**
         * Returns whether a postURL was defined.
         * 
         * @return {@code true} for there is a postUrl, {@code false} else 
         */
        public boolean hasPostUrl() {
            return null != postUrl && postUrl.length() > 0;
        }

        /**
         * Returns  whether the mapper shall produce a mapping for processes running in containers.
         * 
         * @return whether container configuration values are preferred
         */
        public boolean isInContainer() {
            return inContainer;
        }

        /**
         * Defines whether the mapper shall produce a mapping for processes running in containers.
         * 
         * @param inContainer whether container configuration values are preferred
         * @return <b>this</b> for chaining
         */
        public MapperParams setInContainer(boolean inContainer) {
            this.inContainer = inContainer;
            return this;
        }
        
    }

    /**
     * Maps the specified configuration to a dashboard specification.
     * 
     * @param params the mapper parameters
     * @throws ExecutionException if mapping fails
     */
    public File mapConfigurationToDashboard(MapperParams params) throws ExecutionException;
    
    /**
     * Maps the specified configuration to a dashboard specification as process.
     * 
     * @param params the mapper parameters
     * @param plugins the folders of the plugins to load
     */
    public void mapConfigurationToDashboardAsProcess(MapperParams params, List<File> plugins) 
            throws ExecutionException;

}
