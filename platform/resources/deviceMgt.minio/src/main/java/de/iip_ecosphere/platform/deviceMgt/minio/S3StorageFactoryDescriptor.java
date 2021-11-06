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

package de.iip_ecosphere.platform.deviceMgt.minio;

import de.iip_ecosphere.platform.deviceMgt.Configuration;
import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactoryDescriptor;
import io.minio.MinioClient;

/**
 * A S3StorageFactoryDescriptor is a service provider for
 * {@code StorageFactoryDescriptor}, which provides a factory
 * for {@link S3Storage S3Storages}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class S3StorageFactoryDescriptor implements StorageFactoryDescriptor {

    /**
     * Creates and configures a runtime storage with the help of the provided configuration.
     *
     * @param configuration the configuration
     * @return a runtime storage
     */
    public Storage createRuntimeStorage(Configuration configuration) {
        if (null == configuration) {
            return null;
        }

        return createPackageStorage(configuration.getRuntimeStorage());
    }

    /**
     * Creates and configures a configuration storage with the help of the provided configuration.
     *
     * @param configuration the configuration
     * @return a runtime storage
     */
    public Storage createConfigStorage(Configuration configuration) {
        if (null == configuration) {
            return null;
        }

        return createPackageStorage(configuration.getConfigStorage());
    }

    /**
     * Creates and configures a package storage with the help of the provided configuration.
     *
     * @param storageSetup the package storage setup
     * @return a runtime storage
     */
    public Storage createPackageStorage(PackageStorageSetup storageSetup) {
        if (null == storageSetup) {
            return null;
        }

        MinioClient minioClient = MinioClient.builder()
            .endpoint(storageSetup.getEndpoint())
            .region(storageSetup.getRegion())
            .credentials(storageSetup.getAccessKey(),
                storageSetup.getSecretAccessKey())
            .build();
        return new S3PackageStorage(minioClient,
            storageSetup.getBucket(),
            storageSetup.getPrefix(),
            storageSetup.getPackageDescriptor(),
            storageSetup.getPackageFilename());
    }
}
