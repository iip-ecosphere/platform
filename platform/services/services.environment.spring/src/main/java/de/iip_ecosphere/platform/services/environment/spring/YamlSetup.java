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

import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
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
     * @param in the input stream containing the YAML setup
     * @return the transport setup for the "external" binder, may be <b>null</b> for none
     */
    public static TransportSetup getExternalTransportSetup(InputStream in) {
        return getSetup(in, "external");
    }

    /**
     * Reads the default "application.yml" file and searches for the Spring definition of the "external" binder. 
     * Returns, if available the Transport setup from the "external" binder environment.
     * 
     * @return the transport setup for the "external" binder, may be <b>null</b> for none
     * @see Starter#getApplicationSetupAsStream()
     */
    public static TransportSetup getExternalTransportSetup() {
        return getExternalTransportSetup(Starter.getApplicationSetupAsStream());
    }

    /**
     * Reads {@code in} as YAML file and searches for the Spring definition of the "internal" binder. 
     * Returns, if available the Transport setup from the "internal" binder environment.
     * 
     * @param in the input stream containing the YAML setup
     * @return the transport setup for the "internal" binder, may be <b>null</b> for none
     */
    public static TransportSetup getInternalTransportSetup(InputStream in) {
        return getSetup(in, "internal");
    }

    /**
     * Reads the default "application.yml" file and searches for the Spring definition of the "internal" binder. 
     * Returns, if available the Transport setup from the "internal" binder environment.
     * 
     * @return the transport setup for the "internal" binder, may be <b>null</b> for none
     * @see Starter#getApplicationSetupAsStream()
     */
    public static TransportSetup getInternalTransportSetup() {
        return getInternalTransportSetup(Starter.getApplicationSetupAsStream());
    }

    /**
     * Reads {@code in} as YAML file and searches for the Spring definition of binders. Returns, if available
     * the Transport setup from the respective binder environment.
     * 
     * @param in the input stream containing the YAML setup
     * @param binder the binder name, usually "internal" or "external" by IIP generation conventions
     * @return the transport setup for the binder, may be <b>null</b> for none
     */
    private static TransportSetup getSetup(InputStream in, String binder) {
        TransportSetup result = null;
        if (null != in) {
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, Object> env = getMap((Map<String, Object>) yaml.load(in), 
                    "spring", "cloud", "stream", "binders", "properties", binder, "environment");
                if (null != env && env.size() > 0) {
                    String binderEnvKey = env.keySet().iterator().next();
                    Map<String, Object> tmp = getMap(env, binderEnvKey);
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
     * Returns a map from a YAML structure given as map object.
     * 
     * @param yaml the YAML structure
     * @param path the key-name path into the YAML structure
     * @return the found YAML sub-structure or <b>null</b> if not found
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> yaml, String... path) {
        Map<String, Object> result = null;
        for (String n: path) {
            if (null != yaml) {
                Object tmp = yaml.get(n);
                if (tmp instanceof Map) {
                    result = (Map<String, Object>) tmp;
                }
            }
            yaml = result;
        }
        return result;
    }

}
