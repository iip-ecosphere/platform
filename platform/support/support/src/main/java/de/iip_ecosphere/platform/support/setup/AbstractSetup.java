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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import de.iip_ecosphere.platform.support.resources.MultiResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Basic class for a YAML-based component setup. Implementing classes must have a public no-arg constructor.
 * Each property requires a public getter and setter in Java Bean Style. 
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSetup {
    
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
     * Creates a tolerant YAML object to read objects of type {@code cls}.
     * 
     * @param cls the type to read
     * @return the yamp object
     */
    public static Yaml createYaml(Class<?> cls) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return new Yaml(new Constructor(cls), representer);
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
                Iterator<Object> it = createYaml(cls).loadAll(in).iterator();
                if (it.hasNext()) {
                    Object o = it.next(); // ignore the other sub-documents here
                    if (cls.isInstance(o)) {
                        result = cls.cast(o);
                    }
                }
                in.close();
            } catch (YAMLException e) {
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
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) yaml.load(in);
                result = YamlFile.overwrite(result, cls, data);
                overwrite.close();
            } catch (IOException e) {
                overwrite.close();
                throw new IOException(e);
            }
        }
        return result;
    }

}
