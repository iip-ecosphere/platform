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

package de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Provides access to installed dependencies on the actual resource, e.g., the location of a specific Java version
 * or of Python. The instantiation process shall provide a YamlFile called {@code installedDependencies.yml} linking
 * symbolic program/dependency names to installation folders where the respective binary can be found. The information
 * shall be taken from service dependencies in the configuration model.
 * 
 * Java versions shall be mapped to keys like "JAVA<i>x</i>" where <i>x</i> represents the Java major version, older 
 * just without the leading "1.", newer as the major version, see {@link #getJavaKey()} for the actual version. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class InstalledDependenciesSetup extends AbstractSetup {

    public static final String PROPERTY_PATH = "iip.installedDeps";
    
    /**
     * Just the name of the default configuration file, no extension, no path.
     */
    public static final String DEFAULT_NAME = "installedDependencies";
    
    /**
     * Name of the default configuration file with extension (no path).
     */
    public static final String DEFAULT_FNAME = DEFAULT_NAME + ".yml";
    
    /**
     * The prefix for Java program/dependency keys.
     */
    public static final String KEY_PREFIX_JAVA = "JAVA";
    
    /**
     * The prefix for Conda program/dependency keys.
     */
    public static final String KEY_PREFIX_PYTHON = "PYTHON";
    
    /**
     * The prefix for Conda program/dependency keys.
     */
    public static final String KEY_PREFIX_CONDA = "CONDA";
    
    public static final String KEY_JAVA_8 = KEY_PREFIX_JAVA + 8;
    public static final String KEY_JAVA_11 = KEY_PREFIX_JAVA + 11;
    
    private static InstalledDependenciesSetup instance;
    private Map<String, File> locations = new HashMap<String, File>();
    
    /**
     * Sets up default values before overriding.
     */
    public InstalledDependenciesSetup() {
        setupDefaults();
    }
    
    /**
     * Sets up the default values.
     */
    private void setupDefaults() {
        String exeSuffix = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            exeSuffix = ".exe";
        }
        File javaPath = new File(SystemUtils.getJavaHome(), "bin/java" + exeSuffix);
        addDefaultEntry(getJavaKey(), javaPath);
        addDefaultEntry(KEY_PREFIX_JAVA, javaPath);
        addDefaultEntry(KEY_PREFIX_PYTHON, new File("python")); // generic python in path
        addDefaultEntry(KEY_PREFIX_CONDA, new File("conda")); // generic conda in path
    }
    
    /**
     * Adds a default location entry. Does not override existing values.
     * 
     * @param key the key
     * @param value the value
     */
    private void addDefaultEntry(String key, File value) {
        if (!locations.containsKey(key)) {
            locations.put(key, value);
        }
    }

    /**
     * Returns the program/dependency key for the actual Java version.
     * 
     * @return the key
     */
    public static String getJavaKey() {
        String ver = SystemUtils.JAVA_SPECIFICATION_VERSION;
        if (ver.startsWith("1.")) {
            ver = ver.substring(2);
        }
        return KEY_PREFIX_JAVA + ver;
    }
    
    /**
     * Returns the locations.
     * 
     * @return the locations
     */
    public Map<String, File> getLocations() {
        return locations;
    }
    
    /**
     * Returns the location for a given program/dependency key.
     * 
     * @param key the key
     * @return the location, may be <b>null</b> for none
     */
    public File getLocation(String key) {
        return null == key ? null : locations.get(key);
    }
    
    /**
     * Changes the locations. [required by SnakeYaml]
     * 
     * @param locations the locations
     */
    public void setLocations(Map<String, File> locations) {
        this.locations = locations;
        setupDefaults(); // ensure defaults but do not overwrite values
    }
    
    /**
     * Sets a single location, but ensures the default values.
     * 
     * @param key the key to set
     * @param location the new location
     */
    public void setLocation(String key, File location) {
        locations.put(key, location);
        setupDefaults(); // ensure defaults but do not overwrite values
    }
    
    /**
     * Reads the given yaml file via the {@link ResourceLoader}, taking into account the system property 
     * {@link #PROPERTY_PATH}, the current directory as additional resource folder and the file system root.
     * 
     * @param fileName the file to read
     * @return the setup instance, if not found the default instance
     */
    public static InstalledDependenciesSetup readFromYaml(String fileName) {
        InstalledDependenciesSetup result = null;
        InputStream in = ResourceLoader.getResourceAsStream(fileName, 
            new FolderResourceResolver(System.getProperty(PROPERTY_PATH, ".")),
            new FolderResourceResolver()); // also look in system root
        if (null != in) {
            try {
                result = readFromYaml(InstalledDependenciesSetup.class, in);
            } catch (IOException e) {
                LoggerFactory.getLogger(InstalledDependenciesSetup.class).warn(
                    "Cannot read '{}': {}. Falling back to default instance", fileName, e.getMessage());
            }
        }
        if (null == result) {
            result = new InstalledDependenciesSetup();
        }
        return result;
    }
    
    /**
     * Reads the default yaml file, taking into account the system property 
     * {@link #PROPERTY_PATH}, the current directory as additional resource folder and the file system root.
     * 
     * @return the setup instance, if not found the default instance
     */
    public static InstalledDependenciesSetup readFromYaml() {
        return readFromYaml(DEFAULT_FNAME);
    }
    
    /**
     * Returns a singleton instance via {@link #readFromYaml()}.
     *  
     * @return the instance
     */
    public static InstalledDependenciesSetup getInstance() {
        if (null == instance) {
            instance = readFromYaml();
        }
        return instance;
    }
    
    /**
     * Returns a location from {@link #getInstance()} via {@link #getLocation(String)}, throws an exception if no
     * such location is present.
     * 
     * @param key the key to look for
     * @return the location
     * @throws ExecutionException if the key cannot be found
     */
    public static File location(String key) throws ExecutionException {
        File location = getInstance().getLocation(key);
        if (null == location) {
            throw new ExecutionException("No installed dependency for key '" + key + "'", null);
        }
        return location;
    }

}
