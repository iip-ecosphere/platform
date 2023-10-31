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

package de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.text.StringTokenizer;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import de.iip_ecosphere.platform.support.setup.AbstractSetup;
import de.iip_ecosphere.platform.support.setup.YamlFile;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Reads information from the Spring YAML application setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlSetup {
    
    /**
     * Reads {@code in} as YAML file and searches for the Spring definition of the "external" binder. 
     * Returns, if available the Transport setup from the "external" binder environment.
     * 
     * @param args potentially overriding command line arguments
     * @param in the input stream containing the YAML setup
     * @return the transport setup for the "external" binder, may be <b>null</b> for none
     */
    public static TransportSetup getExternalTransportSetup(InputStream in, String[] args) {
        return getSetup(in, "external", args);
    }

    /**
     * Reads the default "application.yml" file and searches for the Spring definition of the "external" binder. 
     * Returns, if available the Transport setup from the "external" binder environment.
     * 
     * @param args potentially overriding command line arguments
     * @return the transport setup for the "external" binder, may be <b>null</b> for none
     * @see Starter#getApplicationSetupAsStream()
     */
    public static TransportSetup getExternalTransportSetup(String[] args) {
        return getExternalTransportSetup(Starter.getApplicationSetupAsStream(), args);
    }

    /**
     * Reads {@code in} as YAML file and searches for the Spring definition of the "internal" binder. 
     * Returns, if available the Transport setup from the "internal" binder environment.
     * 
     * @param args potentially overriding command line arguments
     * @param in the input stream containing the YAML setup
     * @return the transport setup for the "internal" binder, may be <b>null</b> for none
     */
    public static TransportSetup getInternalTransportSetup(InputStream in, String[] args) {
        return getSetup(in, "internal", args);
    }

    /**
     * Reads the default "application.yml" file and searches for the Spring definition of the "internal" binder. 
     * Returns, if available the Transport setup from the "internal" binder environment.
     * 
     * @param args potentially overriding command line arguments
     * @return the transport setup for the "internal" binder, may be <b>null</b> for none
     * @see Starter#getApplicationSetupAsStream()
     */
    public static TransportSetup getInternalTransportSetup(String[] args) {
        return getInternalTransportSetup(Starter.getApplicationSetupAsStream(), args);
    }

    /**
     * Reads {@code in} as YAML file and searches for the Spring definition of binders. Returns, if available
     * the Transport setup from the respective binder environment.
     * 
     * @param args potentially overriding command line arguments
     * @param in the input stream containing the YAML setup
     * @param binder the binder name, usually "internal" or "external" by IIP generation conventions
     * @return the transport setup for the binder, may be <b>null</b> for none
     */
    private static TransportSetup getSetup(InputStream in, String binder, String[] args) {
        TransportSetup result = null;
        if (null != in) {
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, Object> setup = (Map<String, Object>) yaml.load(in);
                substArgs(setup, args);
                Map<String, Object> env = YamlFile.getMap(setup, 
                    "spring", "cloud", "stream", "binders", "properties", binder, "environment");
                if (null != env && env.size() > 0) {
                    String binderEnvKey = env.keySet().iterator().next();
                    Map<String, Object> tmp = YamlFile.getMap(env, binderEnvKey);
                    String tmpYml = yaml.dump(tmp);
                    result = AbstractSetup
                        .createYaml(TransportSetup.class)
                        .loadAs(tmpYml, TransportSetup.class);
                } else {
                    LoggerFactory.getLogger(YamlSetup.class).warn(
                         "Cannot read transport setup '{}': Path/structure in Yaml not found", binder);
                }
                in.close();
            } catch (IOException e) {
                LoggerFactory.getLogger(YamlSetup.class).warn("Cannot read transport setup '{}': {}", 
                     binder, e.getMessage());
            }
        }
        return result;
    }
    
    /**
     * Substitutes values in {@code setup} by values in {@code args}.
     * 
     * @param setup the setup
     * @param args the command line arguments
     */
    private static void substArgs(Map<String, Object> setup, String[] args) {
        for (String a : args) {
            if (a.startsWith("--")) {
                int pos = a.indexOf("=");
                if (pos > 0) {
                    String key = a.substring(2, pos);
                    String val = a.substring(pos + 1);
                    String[] keys = new StringTokenizer(key, ".").getTokenArray();
                    if (keys.length > 1) {
                        String[] path = new String[keys.length - 1];
                        System.arraycopy(keys, 0, path, 0, path.length);
                        Map<String, Object> parent = YamlFile.getMap(setup, path);
                        if (null != parent) {
                            parent.put(keys[keys.length - 1], val);
                        }
                    }
                }
            }
        }
    }

}
