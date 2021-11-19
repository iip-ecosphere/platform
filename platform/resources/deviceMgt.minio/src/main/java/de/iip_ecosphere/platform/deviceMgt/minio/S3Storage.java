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

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.deviceMgt.storage.Storage;

/**
 * A S3Storage grants access to the storage through s3. For this purpose it uses
 * MinioClient to communicate with the s3 storage. The storage can be set to a
 * fixed prefix, so it is only working with a subset of the bucket.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class S3Storage implements Storage {

    private String prefix;
    private MinioClient minioClient;
    private String bucket;

    /**
     * Creates a S3Storage.
     * 
     * @param prefix      the prefix to lock on
     * @param minioClient the connected MinioClient
     * @param bucket      the bucket
     */
    public S3Storage(String prefix, MinioClient minioClient, String bucket) {
        this.prefix = prefix;
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    // checkstyle: stop boolean complexity check
    
    @Override
    public Set<String> list() {
        return StreamSupport.stream(this.minioClient
            .listObjects(ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(true).build())
            .spliterator(), false).map(r -> {
                try {
                    return r.get().objectName();
                } catch (ErrorResponseException | InsufficientDataException | InternalException
                        | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException
                        | ServerException | XmlParserException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    // checkstyle: resume boolean complexity check

    @Override
    public String generateDownloadUrl(String key) {
        try {
            return this.minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .object(key)
                    .bucket(bucket)
                    .method(Method.GET)
                    .expiry(60)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                | InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException
                | ServerException e) {
            LoggerFactory.getLogger(getClass()).error(e.getClass().getSimpleName() + " " + e.getMessage());
        }
        return null;
    }

    @Override
    public void storeFile(String key, File file) throws IOException {
        try {
            this.minioClient.uploadObject(
                UploadObjectArgs.builder()
                    .object(key)
                    .bucket(bucket)
                    .filename(file.getAbsolutePath())
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException 
            | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException 
            | IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
    
}
