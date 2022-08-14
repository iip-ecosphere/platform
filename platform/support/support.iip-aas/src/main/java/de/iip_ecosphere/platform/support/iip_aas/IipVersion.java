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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Provides access to version and buildId of the platform. The data is stored in a file but modified accordingly by 
 * the CI for the deployed artifacts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IipVersion {

    public static final String MAVEN_SNAPSHOT_POSTFIX = "-SNAPSHOT";

    private static IipVersion instance;
    private String version = "";
    private String buildId = "??";
    private boolean isRelease = false;
    
    /**
     * Initializes this instance. Prevents external creation.
     */
    private IipVersion() {
        InputStream is = ResourceLoader.getResourceAsStream("iip-version.properties");
        if (null != is) {
            Properties prop = new Properties();
            try {
                prop.load(is);
                is.close();
            } catch (IOException e) {
            }
            version = prop.getOrDefault("version", version).toString();
            if (version.endsWith(MAVEN_SNAPSHOT_POSTFIX)) {
                version = version.substring(0, version.length() - MAVEN_SNAPSHOT_POSTFIX.length());
            } else {
                isRelease = true;
            }
            buildId = prop.getOrDefault("buildId", buildId).toString();
        }
    }

    /**
     * Returns the singleton instance of this class.
     * 
     * @return the singleton instance
     */
    public static IipVersion getInstance() {
        if (null == instance) {
            instance = new IipVersion();
        }
        return instance;
    }
    
    /**
     * Returns the version of the IIP-Ecosphere platform.
     * 
     * @return the version, may be empty if unknown
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the build id of the IIP-Ecosphere platform.
     * 
     * @return the build id, may be "??" if unknown
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * Returns whether this instance of the IIP-Ecosphere platform is a release version.
     * 
     * @return {@code true} for release version, {@code false} else
     */
    public boolean isRelease() {
        return isRelease;
    }
    
    /**
     * Formats version and build id for printout.
     * 
     * @return the formatted version
     */
    public String getVersionInfo() {
        String result;
        if (version.length() == 0) {
            result = "??";
        } else {
            result = "v " + version + "(" + buildId + ")";
        }
        return result;
    }

}
