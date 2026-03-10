/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.tools.lib.loader;

/**
 * Basic common constants for oktoflow plugin loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Constants {

    public static final String KEY_PREFIX = "# prefix: ";
    public static final String KEY_UNPACK_MODE = "# unpackMode: ";
    public static final String KEY_SETUP_DESCRIPTOR = "# setupDescriptor: ";
    public static final String KEY_PLUGIN_IDS = "# pluginIds: ";
    public static final String KEY_SEQUENCE_NR = "# sequenceNr: ";
    public static final String KEY_ARTIFACTS = "# artifacts: ";
    public static final String KEY_BASE_DIR = "# baseDir: ";
    public static final String VAL_BASE_DIR_MVN = "M2_REPO";

    public enum UnpackMode {
        JARS,
        SNAPSHOTS,
        RESOLVE
    }

    /**
     * Turns a string to an unpack mode.
     * 
     * @param mode the string, may be <b>null</b> 
     * @param dflt the default mode to return if <code>mode</code> is <b>null</b>
     * @return the unpack mode
     */
    public static UnpackMode toUnpackMode(String mode, UnpackMode dflt) {
        UnpackMode result;
        if (null == mode) {
            mode = dflt.name();
        }
        try {
            result = UnpackMode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            result = UnpackMode.JARS;
        }
        return result;
    }

}
