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

/**
 * A stub storage factory for testing.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class StubStorageFactoryDescriptor implements StorageFactoryDescriptor {

    private static Storage storage;

    @Override
    public Storage createPackageStorage(PackageStorageSetup configuration) {
        if (storage != null) {
            return storage;
        }

        return null;
    }

    /**
     * Defines the runtime storage.
     * 
     * @param storage the storage
     */
    public static void setRuntimeStorage(Storage storage) {
        StubStorageFactoryDescriptor.storage = storage;
    }
    
}
