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

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Basic class for a YAML-based component configuration. Implementing classes must have a public no-arg constructor.
 * Each property requires a public getter and setter in Java Bean Style. 
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractConfiguration {

    /**
     * Reads a {@link Configuration} instance from the root folder of the JAR/classpath. Unknown properties are ignored.
     *
     * @param <C> the specific type of configuration to read
     * @param cls the class of configuration to read
     * @param filename the filename (a leading "/" is added if missing for JAR/classpath resource access)
     * @return the configuration instance
     * @throws IOException if the file cannot be read/found, the configuration class cannot be instantiated
     */
    public static <C> C readFromYaml(Class<C> cls, String filename) throws IOException {
        C result = null;
        String fname = filename;
        if (!fname.startsWith("/")) {
            fname = "/" + fname; 
        }
        InputStream in = AbstractConfiguration.class.getResourceAsStream(fname);
        if (in != null) {
            try {
                Representer representer = new Representer();
                representer.getPropertyUtils().setSkipMissingProperties(true);
                Yaml yaml = new Yaml(new Constructor(cls), representer);
                result = yaml.load(in);
                in.close();
            } catch (YAMLException e) {
                throw new IOException(e);
            }
        } else {
            throw new IOException("Cannot read " + fname);
        }
        if (null == result) {
            try {
                result = cls.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException 
                | InvocationTargetException e) {
                throw new IOException(e);
            }
        } 
        return result;
    }
    
}
