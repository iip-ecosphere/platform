/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.tools.maven.dependencies;

import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;

/**
 * Knows plugin artifact-layer-dependencies/exclusions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Layers {

    private static final String[] LAYERS = {
        "support", 
        "support.aas", 
        "support.iip-aas", 
        "transport", 
        "connectors",
        "services.environment", 
        "services", 
        "deviceMgt",
        "ecsRuntime",
        "monitoring",
        "configuration",
        "platform"
    };
    
    /**
     * Determines the exclude artifact ids.
     * 
     * @param artifactId the actual artifact id
     * @param excludeIds the given excludeArtifactids
     * @return {@code excludeIds} or the known exclude ids
     */
    public static String getExcludeArtifactIds(String artifactId, String excludeIds, Log log) {
        String result = excludeIds;
        if (null == result || result.length() == 0) {
            int last = 0;
            for (int l = 0; l < LAYERS.length; l++) {
                if (artifactId.startsWith(LAYERS[l] + ".")) {
                    last = l;
                }
            }
            result = String.join(", ", Arrays.copyOfRange(LAYERS, 0, last + 1));
/*            if (artifactId.startsWith("support.aas.")) {
                result = "support, support.aas";
            } else if (artifactId.startsWith("support.")) {
                result = "support";
            } else if (artifactId.startsWith("transport.")) {
                result = "support, support.aas, support.iip-aas, transport";
            } else if (artifactId.startsWith("connectors.")) {
                result = "support, support.aas, support.iip-aas, transport, connectors";
            } else if (artifactId.startsWith("services.")) {
                result = "support, support.aas, support.iip-aas, transport, services.environment, services";
            } else if (artifactId.startsWith("deviceMgt.")) {
                result = "support, support.aas, support.iip-aas, transport, services.environment, services, deviceMgt";
            } // further cases*/
        }
        if (log != null && result != null && result.length() > 0) {
            log.info("Using excludeArtifactIds: " + result);
        }
        return result;
    }
    
}
