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

package de.iip_ecosphere.platform.deviceMgt.storage;

import java.util.Set;

/**
 * A Storage grants access to different files/objects.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface Storage {

    /**
     * Get the prefix the storage is locked on.
     * In filesystem terminology this would be the folder the data is lying in.
     *
     * @return the prefix
     */
    String getPrefix();

    /**
     * Lists the content under the desired prefix.
     *
     * @return a set of paths
     */
    Set<String> list();

    /**
     * Generate a pre-signed url, so one can download (GET) the desired data.
     * 
     * @param key the key, comparable with a file in a folder.
     * @return pre-signed Download-URL (http-protocol)
     */
    String generateDownloadUrl(String key);

}
