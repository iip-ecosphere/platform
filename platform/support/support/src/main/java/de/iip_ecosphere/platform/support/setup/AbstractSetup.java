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

package de.iip_ecosphere.platform.support.setup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.resources.MultiResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import de.iip_ecosphere.platform.support.yaml.YamlFile;

/**
 * Basic class for a YAML-based component setup. Implementing classes must have a public no-arg constructor.
 * Each property requires a public getter and setter in Java Bean Style. 
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSetup {
    
    public static final String PARAM_PLUGINS = "iip.plugins";
    
    /**
     * The name of the default setup file, no extension, no path.
     */
    public static final String DEFAULT_NAME = "iipecosphere"; // TODO turn to oktoflow, VTL -> both
    
    /**
     * Name of the default setup file with extension (no path).
     */
    public static final String DEFAULT_FNAME = DEFAULT_NAME + ".yml";

    /**
     * Name of the default setup override file with extension (no path).
     */
    public static final String DEFAULT_OVERRIDE_FNAME = "oktoflow-local" + ".yml"; // TODO use DEFAULT_NAME 
    
    private File pluginsFolder = new File(OsUtils.getPropertyOrEnv(PARAM_PLUGINS, "plugins"));

    /**
     * Changes the folder where the oktoflow plugins are located. [yaml convention]
     * 
     * @param pluginsFolder the plugins folder
     */
    public void setPluginsFolder(String pluginsFolder) { // file causes exception in snakeyaml
        setPluginsFolderFile(null == pluginsFolder ? null : new File(pluginsFolder));
    }

    /**
     * Changes the folder where the oktoflow plugins are located. [yaml convention]
     * 
     * @param pluginsFolder the plugins folder
     */
    public void setPluginsFolderFile(File pluginsFolder) {
        this.pluginsFolder = pluginsFolder;
    }

    /**
     * Returns the folder where the oktoflow plugins are located.
     * 
     * @return the folder, by default taken from {@link #PARAM_PLUGINS} (env or sys property), fallback "plugins"
     */
    public File getPluginsFolder() {
        return pluginsFolder;
    }
    
    /**
     * Reads a configurationfrom {@link #DEFAULT_FNAME} in the  root folder of the JAR/classpath. Unknown properties 
     * are ignored.
     *
     * @param <C> the specific type of configuration to read
     * @param cls the class of configuration to read
     * @return the configuration instance
     * @throws IOException if the file cannot be read/found, the configuration class cannot be instantiated
     */
    public static <C> C readFromYaml(Class<C> cls) throws IOException {
        return readFromYaml(cls, DEFAULT_FNAME, DEFAULT_OVERRIDE_FNAME);
    }
    
    /**
     * Reads a configuration from the root folder of the JAR/classpath. Unknown properties are ignored.
     *
     * @param <C> the specific type of configuration to read
     * @param cls the class of configuration to read
     * @param filename the filename (a leading "/" is added if missing for JAR/classpath resource access)
     * @return the configuration instance
     * @throws IOException if the file cannot be read/found, the configuration class cannot be instantiated
     */
    public static <C> C readFromYaml(Class<C> cls, String filename) throws IOException {
        return readFromYaml(cls, filename, null);
    }

    /**
     * Reads a configuration from the root folder of the JAR/classpath. Unknown properties are ignored.
     *
     * @param <C> the specific type of configuration to read
     * @param cls the class of configuration to read
     * @param filename the filename (a leading "/" is added if missing for JAR/classpath resource access)
     * @param overwrite the name of an optional file (a leading "/" is added if missing for JAR/classpath 
     *     resource access, using {@link MultiResourceResolver#SETUP_RESOLVER}) overwriting values in 
     *     {@code filename}, may be <b>null</b> for none, does not lead to an exception if file does not exist
     * @return the configuration instance
     * @throws IOException if the file cannot be read/found, the configuration class cannot be instantiated
     */
    public static <C> C readFromYaml(Class<C> cls, String filename, String overwrite) throws IOException {
        InputStream in = ResourceLoader.getResourceAsStream(filename);
        if (null == in) {
            throw new IOException("Cannot read " + filename);
        }
        InputStream over = null;
        if (null != overwrite) {
            over = ResourceLoader.getResourceAsStream(overwrite, MultiResourceResolver.SETUP_RESOLVER);
        }
        return readFromYaml(cls, in, over);  
    }

    /**
     * Returns if a string is valid, i.e. not <b>null</b> and not empty.
     *
     * @param str the string
     * @return {@code true} for valid and not empty, {@code false} else
     */
    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }
    
    /**
     * Reads a instance from {@code in}. Unknown properties are ignored.
     *
     * @param <C> the specific type of configuration to read
     * @param cls the class of configuration to read
     * @param in the stream to read from (ignored if <b>null</b>, else being closed)
     * @return the configuration instance
     * @throws IOException if the data cannot be read, the configuration class cannot be instantiated
     */
    public static <C> C readFromYaml(Class<C> cls, InputStream in) throws IOException {
        return readFromYaml(cls, in, null);
    }

    /**
     * Reads a instance from {@code in}. Unknown properties are ignored.
     *
     * @param <C> the specific type of configuration to read
     * @param cls the class of configuration to read
     * @param in the stream to read from (ignored if <b>null</b>, else being closed)
     * @param overwrite optional stream to overwrite values taken from {@code in} 
     * @return the configuration instance
     * @throws IOException if the data cannot be read, the configuration class cannot be instantiated
     */
    public static <C> C readFromYaml(Class<C> cls, InputStream in, InputStream overwrite) throws IOException {
        C result = null;
        if (in != null) {
            try {
                Iterator<Object> it = Yaml.getInstance().loadAll(in, cls);
                if (it.hasNext()) {
                    Object o = it.next(); // ignore the other sub-documents here
                    if (cls.isInstance(o)) {
                        result = cls.cast(o);
                    }
                }
                in.close();
            } catch (IOException e) {
                in.close();
                throw new IOException(e);
            }
        }
        if (null == result) {
            LoggerFactory.getLogger(AbstractSetup.class).info("No input YAML file, falling back to "
                + "default instance for " + cls.getName());
            try {
                result = cls.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException 
                | InvocationTargetException e) {
                throw new IOException(e);
            }
        } 
        if (null != overwrite) {
            boolean hasAnnotation = false;
            Class<?> iter = cls;
            while (iter != null && iter != Object.class && !hasAnnotation) {
                hasAnnotation = iter.getAnnotation(EnableSetupMerge.class) != null;
                iter = iter.getSuperclass();
            }
            if (hasAnnotation) {
                try {
                    Map<String, Object> data = Yaml.getInstance().loadMapping(overwrite);
                    result = YamlFile.overwrite(result, cls, data);
                    overwrite.close();
                } catch (IOException e) {
                    overwrite.close();
                    LoggerFactory.getLogger(AbstractSetup.class).error("Cannot overwrite setup: {} Ignoring.", 
                        e.getMessage());
                }
            }
        }
        return result;
    }

}
