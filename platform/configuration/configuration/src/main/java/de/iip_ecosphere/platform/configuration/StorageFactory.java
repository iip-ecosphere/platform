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

package de.iip_ecosphere.platform.configuration;

import de.iip_ecosphere.platform.deviceMgt.storage.Storage;

/**
 * Factory for global artifact storages. To be used to upload automatically generated application artifacts and 
 * container images.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StorageFactory {

    private static Storage serviceArtifactStorage;
    private static Storage containerImageStorage;

    /**
     * Returns the service artifact storage instance.
     * 
     * @return the storage, may be <b>null</b> if no setup for the storage is known
     */
    public static Storage createServiceArtifactStorage() {
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        if (null != setup.getServiceArtifactStorage() && null == serviceArtifactStorage) {
            serviceArtifactStorage = de.iip_ecosphere.platform.deviceMgt.storage.StorageFactory.getInstance()
                .createStorage(setup.getServiceArtifactStorage());
        }
        return serviceArtifactStorage;
    }
    
    /**
     * Returns the container image storage instance.
     * 
     * @return the storage, may be <b>null</b> if no setup for the storage is known
     */
    public static Storage createContainerImageStorage() {
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        if (null != setup.getContainerImageStorage() && null == containerImageStorage) {
            containerImageStorage = de.iip_ecosphere.platform.deviceMgt.storage.StorageFactory.getInstance()
                .createStorage(setup.getServiceArtifactStorage());
        }
        return containerImageStorage;
    }

}
