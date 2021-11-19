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

import io.findify.s3mock.S3Mock;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.support.NetUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Tests the S3Mock storage.
 * 
 * @author Dennis Pidun, University of Hildesheim
 * @author Holger Eichelberger, SSE
 */
public class S3StorageTest {

    public static final String A_BUCKET = "abucket";
    public static final String A_PREFIX = "A_PREFIX";
    public static final String A_PATH = A_PREFIX + "/A_PATH";
    public static final String AN_URL = "AN_URL";
    public static final String AN_INVALID_PATH = "AN_INVALID_PATH";
    
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
        client.putObject(A_BUCKET, A_PATH, "abc");
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
     * Listing shall be handled by MinIO.
     */
    @Test
    public void list_shouldAskStorage() {
        Storage storage = new S3Storage(A_PREFIX, client, A_BUCKET);
        Set<String> listing = storage.list();
        Assert.assertEquals(1, listing.size());
        Assert.assertEquals(A_PATH, listing.stream().findFirst().get());
    }

    /**
     * Generating a download IO shall be handled by MinIO.
     */
    @Test
    public void generateDownloadUrl_shouldAskStorage() {
        Storage storage = new S3Storage(A_PREFIX, client, A_BUCKET);
        Assert.assertEquals(connUrl + "/" + A_BUCKET + "/" + A_PATH, 
            storage.generateDownloadUrl(A_PATH));
    }

    /**
     * Generating a download IO for an invalid key shall return <b>null</b>.
     */
    @Test
    public void generateDownloadUrl_forInvalidKey_returnsNull() {
        Storage storage = new S3Storage(A_PREFIX, client, A_BUCKET);
        Assert.assertNull(storage.generateDownloadUrl(AN_INVALID_PATH));
    }
    
    /**
     * Tests that requesting the prefix works.
     */
    @Test
    public void getPrefix_isSet() {
        Storage storage = new S3Storage(A_PREFIX, null, null);
        Assert.assertEquals(A_PREFIX, storage.getPrefix());
    }

    /**
     * Tests that uploading a file works. 
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void uploadFile_successful() throws IOException {
        Storage storage = new S3Storage(A_PREFIX, client, A_BUCKET);
        storage.storeFile("upload", new File("./src/test/resources/ExampleUpload.txt"));
    }

    /**
     * Tests that uploading a file correctly fails. 
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void uploadFile_fileNotFound() {
        try {
            Storage storage = new S3Storage(A_PREFIX, client, A_BUCKET);
            storage.storeFile("uploadFnf", new File("./src/test/resources/ExampleUpload1.txt"));
            Assert.fail("There shall be an IOException");
        } catch (IOException e) {
            // this is intended
        }
    }

}