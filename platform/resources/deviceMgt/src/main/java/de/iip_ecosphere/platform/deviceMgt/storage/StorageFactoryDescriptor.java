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

import de.iip_ecosphere.platform.deviceMgt.Configuration;

/**
 * A service provider interface which can create different kinds of storages.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface StorageFactoryDescriptor {

    /**
     * Create a runtime storage.
     *
     * @param configuration the configuration
     * @return the runtime storage
     */
    Storage createRuntimeStorage(Configuration configuration);

    /**
     * Create a config storage.
     *
     * @param configuration the configuration
     * @return the runtime storage
     */
    Storage createConfigStorage(Configuration configuration);
}
