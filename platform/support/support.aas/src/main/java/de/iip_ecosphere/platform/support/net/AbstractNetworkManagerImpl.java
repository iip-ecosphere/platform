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

package de.iip_ecosphere.platform.support.net;

import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Basic network manager implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractNetworkManagerImpl implements NetworkManager {

    /**
     * Checks the key for structural validity.
     * 
     * @param key the key
     * @throws IllegalArgumentException if {@code key} is not structurally valid
     */
    public static void checkKey(String key) {
        if (null == key) {
            throw new IllegalArgumentException("Key must be given");
        }
    }

    /**
     * Checks the address for structural validity.
     * 
     * @param address the address
     * @throws IllegalArgumentException if {@code address} is not structurally valid
     */
    public static void checkAddress(ServerAddress address) {
        if (null == address) {
            throw new IllegalArgumentException("Address must be given");
        }
    }

}
