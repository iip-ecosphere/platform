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

package de.iip_ecosphere.platform.support.iip_aas;

/**
 * Artifact or service version. The format in terms of a pseudo "regular expression" number is {@code ("." number)*}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Version extends de.iip_ecosphere.platform.support.Version { // migrate to support.Version

    /**
     * Creates a new version by parsing a string.
     * 
     * @param version the version string in form empty or i(.i)* with i integer numbers 
     * @throws IllegalArgumentException in case of format problems
     */
    public Version(String version) throws IllegalArgumentException {
        super(version);
    }
    
    /**
     * Version created from version segments.
     *  
     * @param version version number segments (from left to right), without {@link #SEPARATOR}
     */
    public Version(int... version) {
        super(version);
    }

}