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
import io.minio.ObjectWriteResponse;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Contents;
import io.minio.messages.Item;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import de.iip_ecosphere.platform.deviceMgt.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the S3 storage.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
@RunWith(MockitoJUnitRunner.class)
public class S3StorageTest {

    public static final String A_BUCKET = "abucket";
    public static final String A_PREFIX = "A_PREFIX";
    public static final String A_PATH = A_PREFIX + "/A_PATH";
    public static final String AN_URL = "AN_URL";
    public static final String AN_INVALID_PATH = "AN_INVALID_PATH";
    private static final int EXPIRE_TIME = 60;

    /**
     * Listing shall be handled by MinIO.
     */
    @Test
    public void list_shouldAskMinio() {
        MinioClient minioMock = mock(MinioClient.class);
        Storage storage = new S3Storage(A_PREFIX, minioMock, A_BUCKET);
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(A_BUCKET)
                .prefix(A_PREFIX)
                .recursive(true)
                .build();

        Result<Item> result = new Result<>(new Contents(A_PATH));

        when(minioMock.listObjects(eq(listObjectsArgs))).thenReturn(Collections.singleton(result));

        Set<String> listing = storage.list();
        Assert.assertEquals(1, listing.size());
        Assert.assertEquals(A_PATH, listing.stream().findFirst().get());
    }

    // checkstyle: stop exception type check

    /**
     * Generating a download IO shall be handled by MinIO.
     * 
     * @throws Exception shall not occur
     */
    @Test
    public void generateDownloadUrl_shouldAskMinio() throws Exception {
        MinioClient minioMock = mock(MinioClient.class);
        Storage storage = new S3Storage(A_PREFIX, minioMock, A_BUCKET);

        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .bucket(A_BUCKET)
                .object(A_PATH)
                .expiry(EXPIRE_TIME)
                .method(Method.GET)
                .build();
        when(minioMock.getPresignedObjectUrl(eq(getPresignedObjectUrlArgs))).thenReturn(AN_URL);

        Assert.assertEquals(AN_URL, storage.generateDownloadUrl(A_PATH));
    }

    /**
     * Generating a download IO for an invalid key shall return <b>null</b>.
     * 
     * @throws Exception shall not occur
     */
    @Test
    public void generateDownloadUrl_forInvalidKey_returnsNull() throws Exception {
        MinioClient minioMock = mock(MinioClient.class);
        Storage storage = new S3Storage(A_PREFIX, minioMock, A_BUCKET);

        when(minioMock.getPresignedObjectUrl(any())).thenThrow(new InvalidKeyException());

        Assert.assertNull(storage.generateDownloadUrl(AN_INVALID_PATH));
    }
    
    // checkstyle: resume exception type check

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
     * @throws XmlParserException shall not occur
     * @throws ServerException shall not occur
     * @throws NoSuchAlgorithmException shall not occur
     * @throws IOException shall not occur
     * @throws InvalidResponseException shall not occur
     * @throws InvalidKeyException shall not occur
     * @throws InternalException shall not occur
     * @throws InsufficientDataException shall not occur
     * @throws ErrorResponseException shall not occur
     */
    @Test
    public void uploadFile_successful() throws IOException, XmlParserException, ServerException, 
        NoSuchAlgorithmException, IOException, InvalidResponseException, InvalidKeyException, InternalException, 
        InsufficientDataException, ErrorResponseException {
        MinioClient minioMock = mock(MinioClient.class);
        // String bucket, String region, String object, String etag, String versionId
        ObjectWriteResponse resp = new ObjectWriteResponse(null, A_BUCKET, "", "", "", "");
        when(minioMock.uploadObject(any())).thenReturn(resp);
        Storage storage = new S3Storage(A_PREFIX, minioMock, A_BUCKET);
        storage.storeFile("upload", new File("./src/test/resources/ExampleUpload.txt"));
    }

    /**
     * Tests that uploading a file correctly fails. 
     * 
     * @throws IOException shall not occur
     * @throws XmlParserException shall not occur
     * @throws ServerException shall not occur
     * @throws NoSuchAlgorithmException shall not occur
     * @throws IOException shall not occur
     * @throws InvalidResponseException shall not occur
     * @throws InvalidKeyException shall not occur
     * @throws InternalException shall not occur
     * @throws InsufficientDataException shall not occur
     * @throws ErrorResponseException shall not occur
     */
    @Test
    public void uploadFile_fileNotFound() throws XmlParserException, ServerException, NoSuchAlgorithmException, 
        IOException, InvalidResponseException, InvalidKeyException, InternalException, InsufficientDataException, 
        ErrorResponseException {
        MinioClient minioMock = mock(MinioClient.class);
        // no functional mocking here, caught already in argument builder 
        try {
            Storage storage = new S3Storage(A_PREFIX, minioMock, A_BUCKET);
            storage.storeFile("uploadFnf", new File("./src/test/resources/ExampleUpload1.txt"));
            Assert.fail("There shall be an IOException");
        } catch (IOException e) {
            // this is intended
        }
    }
    
}