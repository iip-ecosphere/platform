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

package de.iip_ecosphere.platform.support.setup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;

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

    public enum Mode {
        PRODUCTION,
        TEST
    }
    
    private static InstalledDependenciesSetup instance;
    private Mode mode = Mode.PRODUCTION;
    private Map<String, File> locations = new HashMap<>();
    private Map<String, String> envMappings = new HashMap<>();
    
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
        if (OsUtils.isWindows()) {
            exeSuffix = ".exe";
        }
        File javaPath = new File(OsUtils.getJavaHome(), "bin/java" + exeSuffix);
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
        String ver = OsUtils.getJavaSpecificationVersion();
        if (ver.startsWith("1.")) {
            ver = ver.substring(2);
        }
        return KEY_PREFIX_JAVA + ver;
    }

    /**
     * Returns the mode.
     * 
     * @return the mode
     */
    public Mode getMode() {
        return mode;
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
     * @return the (resolved) location, may be <b>null</b> for none
     * @see FileUtils#resolve(File)
     */
    public File getLocation(String key) {
        return null == key ? null : FileUtils.resolve(locations.get(key));
    }

    /**
     * Returns an environment mapping for the given environment {@code key}.
     * Environment mappings are generic environment key/name mappings and could, e.g.,
     * be used to map a service-specific Python Conda environment to an implemented environment
     * produced during container generation. 
     * 
     * @param key the key
     * @param dflt the default value to return if there is no mapping
     * @return the mapped value or {@code dflt}
     */
    public String getEnvironmentMapping(String key, String dflt) {
        String result = dflt;
        if (null != key) {
            String env = envMappings.get(key);
            if (null != env) {
                result = env;
            }
        }
        return result;
    }
    
    /**
     * Returns the number of environment mappings.
     * 
     * @return the number of environment mappings
     */
    public int getEnvironmentMappingsSize() {
        return envMappings.size();
    }
    
    /**
     * Changes the mode. [required by Yaml]
     * 
     * @param mode the new mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    /**
     * Changes the locations. [required by Yaml]
     * 
     * @param locations the locations
     */
    public void setLocations(Map<String, File> locations) {
        this.locations = locations;
        setupDefaults(); // ensure defaults but do not overwrite values
    }

    /**
     * Changes the locations. [required by Yaml]
     * 
     * @param envMappings the mappings
     */
    public void setEnvMappings(Map<String, String> envMappings) {
        this.envMappings = envMappings;
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
        final ResourceResolver[] resolvers = {
            new FolderResourceResolver(System.getProperty(PROPERTY_PATH, ".")),
            new FolderResourceResolver() // also look in system root
        };
        InputStream in = ResourceLoader.getResourceAsStream(fileName, resolvers); 
        if (null != in) {
            try {
                result = readFromYaml(InstalledDependenciesSetup.class, in);
                if (Mode.TEST == result.getMode()) {
                    in = ResourceLoader.getResourceAsStream(fileName, true, resolvers); 
                    if (null != in) {
                        result = readFromYaml(InstalledDependenciesSetup.class, in);
                    }                    
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(InstalledDependenciesSetup.class).warn(
                    "Cannot read '{}': {}. Falling back to default instance", fileName, e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(InstalledDependenciesSetup.class).warn(
                "Installed dependencies file '{}' not found by ResourceLoader. Using default setup.", fileName);
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
     * Merges the information of {@code setup} into this instance. Overrides existing information.
     * 
     * @param setup the setup instance to use
     */
    public void merge(InstalledDependenciesSetup setup) {
        this.locations.putAll(setup.locations);
        this.envMappings.putAll(setup.envMappings);
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
