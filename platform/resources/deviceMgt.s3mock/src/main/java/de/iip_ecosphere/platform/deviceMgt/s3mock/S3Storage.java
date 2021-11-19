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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import de.iip_ecosphere.platform.deviceMgt.storage.Storage;

/**
 * A S3Storage grants access to the storage through S3. For this purpose it uses
 * an Amazon S3 Client to communicate with the S3 storage. The storage can be set to a
 * fixed prefix, so it is only working with a subset of the bucket.
 *
 * @author Dennis Pidun, University of Hildesheim
 * @author Holger Eichelberger, SSE
 */
public class S3Storage implements Storage {

    private String prefix;
    private AmazonS3 client;
    private String bucket;

    /**
     * Creates a S3Storage.
     * 
     * @param prefix      the prefix to lock on
     * @param client      the connected client
     * @param bucket      the bucket
     */
    public S3Storage(String prefix, AmazonS3 client, String bucket) {
        this.prefix = prefix;
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Set<String> list() {
        return StreamSupport.stream(client
            .listObjects(bucket, prefix)
            .getObjectSummaries()
            .spliterator(), false)
            .map(r->r.getKey())
            .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public String generateDownloadUrl(String key) {
        String result = null;
        if (client.doesObjectExist(bucket, key)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 60);
            result = client.generatePresignedUrl(bucket, key, cal.getTime(), HttpMethod.GET).toString();
        }
        return result;
    }

    @Override
    public void storeFile(String key, File file) throws IOException {
        try {
            TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(client)
                .build();
            Upload upload = tm.upload(bucket, key, file);
            upload.waitForCompletion();
        } catch (InterruptedException | AmazonClientException e) {
            throw new IOException(e);
        }
    }
    
}
