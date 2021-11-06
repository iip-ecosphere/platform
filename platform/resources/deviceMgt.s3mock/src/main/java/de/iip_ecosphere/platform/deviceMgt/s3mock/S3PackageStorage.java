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

import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.AmazonS3;

/**
 * A S3PackageStorage grants access to the package storages through S3.
 * For this purpose it uses an Amazon S3 Client to communicate with the S3 storage.
 *
 * @author Dennis Pidun, University of Hildesheim
 * @author Holger Eichelberger, SSE
 */
public class S3PackageStorage extends S3Storage {
    
    private final String packageDescriptor;
    private final String packageFilename;

    /**
     * Creates a new S3PackageStorage.
     * 
     * @param client the connected MinioClient
     * @param bucket the bucket
     * @param prefix the storage prefix
     * @param packageDescriptor the packageDescriptor name (e.g. package.yml)
     * @param packageFilename the packageFilename (e.g., package.zip)
     */
    public S3PackageStorage(AmazonS3 client, String bucket,
            String prefix, String packageDescriptor,
            String packageFilename) {
        super(prefix, client, bucket);
        this.packageDescriptor = packageDescriptor;
        this.packageFilename = packageFilename;
    }

    @Override
    public Set<String> list() {
        return super.list().stream()
            .filter(key -> key.endsWith(packageDescriptor))
            .map(key -> key.replace("/" + packageDescriptor, ""))
            .collect(Collectors.toSet());
    }

    @Override
    public String generateDownloadUrl(String packageName) {
        String key = this.getPrefix() + packageName + "/" + packageFilename;
        return super.generateDownloadUrl(key);
    }
}
