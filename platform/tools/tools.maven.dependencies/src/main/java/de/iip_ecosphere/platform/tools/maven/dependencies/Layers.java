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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.maven.plugin.logging.Log;

/**
 * Knows plugin artifact-layer-dependencies/exclusions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Layers {

    private static final String[] LAYERS = {
        "tools.lib",
        "support.boot",
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
        "configuration.interface",
        "configuration",
        "platform"
    };
    
    private static final String[] MAIN_ARTID_PATTERNS = {
        "tools.lib-", 
        "maven-python-", // legacy, shall be replaced by tools.lib-
        "support.boot-",
        "support-", 
        "support.aas-", 
        "support.iip-aas-", 
        "transport-", 
        "connectors-", 
        "services.environment-", 
        "services.spring.loader-"            
    };
    
    /**
     * Adds the main plugin artifactId name patterns to {@code #mainPatterns}.
     * 
     * @param mainPatterns the collection to add the patterns to
     * @param includeNonPlugins also include (preliminary) patterns needed during migration
     */
    public static void addMainPatterns(Collection<String> mainPatterns, boolean includeNonPlugins) {
        Collections.addAll(mainPatterns, MAIN_ARTID_PATTERNS);
        if (includeNonPlugins) {
            // preliminary, to become plugins // TODO clean up!!!
            Collections.addAll(mainPatterns, "commons-io", "commons-lang3", "jackson-", "joda-", "jsoniter");
        }
    }
    
    /**
     * Determines the exclude artifact ids.
     * 
     * @param artifactId the actual artifact id
     * @param excludeIds the given excludeArtifactids
     * @return {@code excludeIds} or the known exclude ids
     */
    public static String getExcludeArtifactIds(String artifactId, String excludeIds, boolean asTest, Log log) {
        String result = excludeIds;
        if (null == result || result.length() == 0) {
            int last = -1; // e.g. test has no excludes
            for (int l = 0; l < LAYERS.length; l++) {
                if (artifactId.startsWith(LAYERS[l] + ".")) {
                    last = l;
                }
            }
            if (last >= 0) {
                result = String.join(", ", Arrays.copyOfRange(LAYERS, 0, last + 1));
            }
            result = append(result, "maven-python"); // through support
        }
        if (asTest) {
            result = append(result, "junit", "junit-jupiter-api", "junit-jupiter-engine", "junit-jupiter-params", 
                "junit-platform-commons", "junit-platform-engine");
        }
        if (log != null && result != null && result.length() > 0) {
            log.info("Using excludeArtifactIds: " + result);
        }
        return result;
    }

    /**
     * Appends {@code addition} to {@code text} using "," as separator.
     * 
     * @param text
     * @param addition
     * @return
     */
    private static String append(String text, String... addition) {
        String result = text;
        if (result == null) {
            result = "";
        }
        for (String a: addition) {
            if (result.length() > 0) {
                result += ", ";
            }
            result += a;
        }
        return result;
    }

    /**
     * Returns whether a classpath {@code file} is operating system specific.
     * 
     * @param file the file to check for
     * @return {@code true} for OS specific, {@code false} else
     */
    static boolean isOsCpFile(File file) {
        String path = file.toString();
        return path.endsWith("-win") || path.endsWith("-linux");
    }
    
}
