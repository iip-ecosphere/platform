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
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Contents;
import io.minio.messages.Item;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the S3 package storage.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class S3PackageStorageTest {

    public static final String A_BUCKET = "abucket";
    public static final String PREFIX = "prefix/";
    public static final String A_PATH = PREFIX + "A_PATH";
    private static final String PACKAGE_NAME = "packageName.zip";
    public static final String PACKAGE_PATH = A_PATH + "/" + PACKAGE_NAME;
    private static final String PACKAGE_DESCRIPTOR = "packageDescriptor.yml";

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
    public void list_withMixedContent_onlyListsPackages() {
        Set<String> listing = validPackageListing();
        listing.add(PREFIX + "jkl/wrong-file.yml");
        MinioClient mc = mock(MinioClient.class);
        when(mc.listObjects(any())).thenReturn(setToResultIterable(listing));
        S3PackageStorage storage = new S3PackageStorage(mc, A_BUCKET,
                PREFIX, PACKAGE_DESCRIPTOR, PACKAGE_NAME);

        Assert.assertEquals(validPackageReducedListing(), storage.list());
    }

    // checkstyle: stop exception type check
    
    /**
     * Tests that getting a download URL with a valid URL returns an URL.
     * 
     * @throws Exception shall not occur
     */
    @Test
    public void getDownloadUrl_withValidUrl_returnsUrl() throws Exception {
        MinioClient mc = mock(MinioClient.class);
        ArgumentCaptor<GetPresignedObjectUrlArgs> requestCaptor = 
            ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        when(mc.getPresignedObjectUrl(any())).thenReturn(PACKAGE_PATH);

        S3PackageStorage storage = new S3PackageStorage(mc, A_BUCKET, PREFIX, PACKAGE_DESCRIPTOR, PACKAGE_NAME);
        String downloadUrl = storage.generateDownloadUrl("A_PATH");

        verify(mc).getPresignedObjectUrl(requestCaptor.capture());
        GetPresignedObjectUrlArgs request = requestCaptor.getValue();

        Assert.assertEquals(PACKAGE_PATH, request.object());
        Assert.assertEquals(Method.GET, request.method());
        Assert.assertEquals(PACKAGE_PATH, downloadUrl);
    }

    // checkstyle: resume exception type check
    
    /**
     * Turns a result set to an iterable.
     * 
     * @param objects the objects to be turned to an iterable
     * @return the iterable
     */
    private Iterable<Result<Item>> setToResultIterable(Set<String> objects) {
        return objects.stream().map(o -> new Result<Item>(new Contents(o))).collect(Collectors.toSet());
    }

    /**
     * Returns a set of valid package names.
     * 
     * @return the package names
     */
    private Set<String> validPackageListing() {
        Set<String> listing = new HashSet<>();
        listing.add(PREFIX + "abc/" + PACKAGE_DESCRIPTOR);
        listing.add(PREFIX + "abc/" + PACKAGE_NAME);
        listing.add(PREFIX + "def/" + PACKAGE_DESCRIPTOR);
        listing.add(PREFIX + "def/" + PACKAGE_NAME);
        listing.add(PREFIX + "ghi/" + PACKAGE_DESCRIPTOR);
        listing.add(PREFIX + "ghi/" + PACKAGE_NAME);
        return listing;
    }

    /**
     * Returns a reduced set of valid package names.
     * 
     * @return the package names
     */
    private Set<String> validPackageReducedListing() {
        Set<String> listing = new HashSet<>();
        listing.add(PREFIX + "abc");
        listing.add(PREFIX + "def");
        listing.add(PREFIX + "ghi");
        return listing;
    }
}