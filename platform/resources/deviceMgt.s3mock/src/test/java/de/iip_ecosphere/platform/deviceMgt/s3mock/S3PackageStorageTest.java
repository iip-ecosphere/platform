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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import de.iip_ecosphere.platform.support.NetUtils;
import io.findify.s3mock.S3Mock;

/**
 * Tests the S3Mock package storage.
 * 
 * @author Dennis Pidun, University of Hildesheim
 * @author Holger Eichelberger, SSE
 */
public class S3PackageStorageTest {

    public static final String A_BUCKET = "abucket";
    public static final String PREFIX = "prefix/";
    public static final String A_PATH = PREFIX + "A_PATH";
    private static final String PACKAGE_NAME = "packageName.zip";
    public static final String PACKAGE_PATH = A_PATH + "/" + PACKAGE_NAME;
    private static final String PACKAGE_DESCRIPTOR = "packageDescriptor.yml";
    private static S3Mock api;
    private static AmazonS3 client;
    private static String connUrl;

    /**
     * Creates a mock storage.
     */
    @BeforeClass
    public static void startup() {
        int port = NetUtils.getEphemeralPort();
        api = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
        connUrl = "http://localhost:" + port;
        api.start();
        EndpointConfiguration ep = new EndpointConfiguration(connUrl, "us-west-2");
        client = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(ep)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        client.createBucket(A_BUCKET);
        client.putObject(A_BUCKET, PACKAGE_PATH, "abc");
        client.putObject(A_BUCKET, PREFIX + "/" + PACKAGE_DESCRIPTOR, "<xml>abc</xml>");
    }

    /**
     * Destroys the mock storage.
     */
    @AfterClass
    public static void teardown() {
        client.shutdown();
        api.shutdown();
    }
    
    /**
     * Tests whether the prefix is correct.
     */
    @Test
    public void getPrefix_shouldBeSetToPrefix() {
        S3PackageStorage storage = new S3PackageStorage(null, null, PREFIX, null, null);
        Assert.assertEquals(PREFIX, storage.getPrefix());
    }

    /**
     * Tests listing mixed content.
     */
    @Test
    public void listContent() {
        S3PackageStorage storage = new S3PackageStorage(client, A_BUCKET,
            PREFIX, PACKAGE_DESCRIPTOR, PACKAGE_NAME);
        Assert.assertEquals(1, storage.list().size());
    }

    /**
     * Tests that getting a download URL with a valid URL returns an URL.
     */
    @Test
    public void getDownloadUrl_withValidUrl_returnsUrl() {
        S3PackageStorage storage = new S3PackageStorage(client, A_BUCKET, PREFIX, PACKAGE_DESCRIPTOR, PACKAGE_NAME);
        String downloadUrl = storage.generateDownloadUrl("A_PATH");
        Assert.assertEquals(connUrl + "/" + A_BUCKET + "/" + PACKAGE_PATH, downloadUrl);
    }
    
}