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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Information about an artifact containing services. The artifact is to be deployed. We assume that the underlying
 * yaml file is generated, i.e., repeated information such as relations can be consistently specified.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlArtifact extends AbstractYamlArtifact {

    private List<YamlService> services;
    private List<YamlServer> servers = new ArrayList<>();

    /**
     * Returns the services.
     * 
     * @return the services
     */
    public List<YamlService> getServices() {
        return services;
    }

    /**
     * Sets the service instances. [required by SnakeYaml]
     * 
     * @param services the services
     */
    public void setServices(List<YamlService> services) {
        this.services = services;
    }

    /**
     * Returns the server specification instances. [required by SnakeYaml]
     * 
     * @return the servers
     */
    public List<YamlServer> getServers() {
        return servers;
    }

    /**
     * Sets the server specification instances. [required by SnakeYaml]
     * 
     * @param servers the servers
     */
    public void setServers(List<YamlServer> servers) {
        this.servers = servers;
    }
    
    /**
     * Returns a YAML service information object based on the given service id.
     * 
     * @param id the service id
     * @return the YamlService, <b>null</b> if none was found
     */
    public YamlService getService(String id) {
        YamlService result = null;
        for (int s = 0; null == result && s < services.size(); s++) {
            YamlService tmp = services.get(s);
            if (tmp.getId().equals(id)) {
                result = tmp;
            }
        }
        return result;
    }
    
    /**
     * Returns a YAML service information object based on the given service id.
     * 
     * @param id the service id
     * @return the YamlService, a default instance if none was found
     */
    public YamlService getServiceSafe(String id) {
        YamlService result = getService(id);
        if (null == result) {
            result = new YamlService();
        }
        return result;
    }

    /**
     * Reads an {@link YamlArtifact} from a YAML input stream. The returned artifact may be invalid.
     * 
     * @param in the input stream (may be <b>null</b>)
     * @return the artifact info
     * @throws IOException if the data cannot be read, the configuration class cannot be instantiated
     */
    public static YamlArtifact readFromYaml(InputStream in) throws IOException {
        YamlArtifact result = AbstractSetup.readFromYaml(YamlArtifact.class, in);
        if (null == result.services) {
            result.services = new ArrayList<>();
        }
        return result;
    }
    
    /**
     * Reads from the given YAML input stream, closes the stream. Logs errors and returns a default instance in case 
     * of failures.
     * 
     * @param in the input stream (may be <b>null</b>)
     * @return the YAML artifact
     */
    public static YamlArtifact readFromYamlSafe(InputStream in) {
        YamlArtifact result; 
        try {
            result = readFromYaml(in);
            if (null != in) {
                in.close();
            }
        } catch (IOException e) {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
            result = new YamlArtifact();
            LoggerFactory.getLogger(YamlArtifact.class).warn("Cannot read deployment.yml: " + e.getMessage());
        }
        return result;
    }

}
