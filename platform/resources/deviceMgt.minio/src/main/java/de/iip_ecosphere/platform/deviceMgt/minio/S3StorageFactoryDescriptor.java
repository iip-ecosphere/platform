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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.deviceMgt.DeviceMgtSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactoryDescriptor;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
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
    public Storage createRuntimeStorage(DeviceMgtSetup configuration) {
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
    public Storage createConfigStorage(DeviceMgtSetup configuration) {
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

        MinioClient.Builder minioClientBuilder = MinioClient.builder()
            .endpoint(storageSetup.getEndpoint())
            .region(storageSetup.getRegion());
        String authKey = storageSetup.getAuthenticationKey();
        if (null != authKey && authKey.length() > 0) {
            IdentityToken tok = IdentityStore.getInstance().getToken(authKey);
            if (null != tok) {
                if (TokenType.USERNAME == tok.getType()) {
                    minioClientBuilder.credentials(tok.getUserName(), tok.getTokenDataAsString());
                } else {
                    LoggerFactory.getLogger(getClass()).warn("Identity token for key {} is of type {}. Only USERNAME "
                        + "tokens are supported. Trying without authentication.", authKey, tok.getType());
                }
            } else {
                LoggerFactory.getLogger(getClass()).warn("No identity token for key {} found. "
                    + "Trying without authentication.", authKey);
            }
        }
        return new S3PackageStorage(minioClientBuilder.build(),
            storageSetup.getBucket(),
            storageSetup.getPrefix(),
            storageSetup.getPackageDescriptor(),
            storageSetup.getPackageFilename());
    }
}
