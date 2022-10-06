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

package de.iip_ecosphere.platform.deviceMgt.s3mock;

import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactoryDescriptor;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;

/**
 * A S3StorageFactoryDescriptor is a service provider for
 * {@code StorageFactoryDescriptor}, which provides a factory
 * for {@link S3Storage S3Storages}.
 *
 * @author Dennis Pidun, University of Hildesheim
 * @author Holger Eichelberger, SSE
 */
public class S3StorageFactoryDescriptor implements StorageFactoryDescriptor {

    @Override
    public Storage createPackageStorage(PackageStorageSetup storageSetup) {
        if (null == storageSetup) {
            return null;
        }

        // TODO make region part of storageSetup
        EndpointConfiguration endCfg = new EndpointConfiguration(storageSetup.getEndpoint(), storageSetup.getRegion());
        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endCfg);

        String authKey = storageSetup.getAuthenticationKey();
        if (null != authKey && authKey.length() > 0) {
            IdentityToken tok = IdentityStore.getInstance().getToken(authKey, true);
            if (null != tok) {
                if (TokenType.USERNAME == tok.getType()) {
                    clientBuilder.withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(tok.getUserName(), tok.getTokenDataAsString())));
                } else if (TokenType.ANONYMOUS == tok.getType()) {
                    clientBuilder.withCredentials(new AWSStaticCredentialsProvider(
                            new AnonymousAWSCredentials()));
                } else { // more might be supported...
                    LoggerFactory.getLogger(getClass()).warn("Identity token for key {} is of type {}. Only USERNAME "
                        + "tokens are supported. Trying without authentication.", authKey, tok.getType());
                }
            } else {
                LoggerFactory.getLogger(getClass()).warn("No identity token for key {} found. "
                    + "Trying without authentication.", authKey);
            }
        }

        
        return new S3PackageStorage(clientBuilder.build(),
            storageSetup.getBucket(),
            storageSetup.getPrefix(),
            storageSetup.getPackageDescriptor(),
            storageSetup.getPackageFilename());
    }
    
}
