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

import de.iip_ecosphere.platform.deviceMgt.DeviceMgtSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactory;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactoryDescriptor;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import de.oktoflow.platform.support.yaml.snakeyaml.SnakeYaml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the storage factory descriptor.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class StorageFactoryDescriptorTest {

    private DeviceMgtSetup configuration;

    /**
     * Configures the test.
     */
    @Before
    public void setUp() {
        Yaml.setInstance(new SnakeYaml()); // whyever, mocking?
        
        configuration = new DeviceMgtSetup();
        PackageStorageSetup packageStorageSetup = new PackageStorageSetup();
        packageStorageSetup.setEndpoint("endpoint");
        packageStorageSetup.setRegion("us-west-2");
        packageStorageSetup.setBucket("bucket");
        packageStorageSetup.setAuthenticationKey("minio");
        packageStorageSetup.setPackageDescriptor("runtime.yml");
        packageStorageSetup.setPackageFilename("runtime.zip");
        packageStorageSetup.setPrefix("runtimes/");

        configuration.setRuntimeStorage(packageStorageSetup);
        PackageStorageSetup configsStorageSetup = new PackageStorageSetup();
        configsStorageSetup.setEndpoint("endpoint");
        configsStorageSetup.setRegion("us-west-2");
        configsStorageSetup.setBucket("bucket");
        configsStorageSetup.setAuthenticationKey("minio");
        configsStorageSetup.setPackageDescriptor("config.yml");
        configsStorageSetup.setPackageFilename("config.zip");
        configsStorageSetup.setPrefix("configs/");
        configuration.setConfigStorage(configsStorageSetup);
    }

    /**
     * Tests that creating a runtime storage with configuration also creates a storage. 
     */
    @Test
    public void createRuntimeStorage_withConfiguration_createsStorage() {
        S3StorageFactoryDescriptor s3StorageFactoryDescriptor = new S3StorageFactoryDescriptor();
        Storage storage = s3StorageFactoryDescriptor.createRuntimeStorage(configuration);
        Assert.assertNotNull(storage);
    }

    /**
     * Tests that creating a runtime storage with invalid configuration does not create a storage. 
     */
    @Test
    public void createRuntimeStorage_withInvalidConfiguration_returnsNull() {
        S3StorageFactoryDescriptor s3StorageFactoryDescriptor = new S3StorageFactoryDescriptor();
        Storage storage = s3StorageFactoryDescriptor.createRuntimeStorage(null);
        Assert.assertNull(storage);
    }

    /**
     * Tests that creating a runtime storage with service provide uses the service provider. 
     */
    @Test
    public void createRuntimeStorage_withServiceProvider_usesServiceProvider() {
        MockedStatic<ServiceLoaderUtils> serviceLoaderMock = Mockito.mockStatic(ServiceLoaderUtils.class);
        StorageFactoryDescriptor storageFactoryDescriptor = mock(StorageFactoryDescriptor.class);
        S3Storage storage = new S3Storage(null, null, null);
        when(storageFactoryDescriptor.createPackageStorage(any())).thenReturn(storage);

        serviceLoaderMock.when(() -> ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class))
                .thenReturn(Optional.of(storageFactoryDescriptor));

        StorageFactory storageFactory = new StorageFactory();
        Storage runtimeStorage = storageFactory.createRuntimeStorage();
        Assert.assertEquals(storage, runtimeStorage);

        serviceLoaderMock.close();
    }
    
    /**
     * Tests that creating a runtime storage leads to the JSL-configured storage. 
     */
    @Test
    public void createStorages_withoutServiceProvider_isJSL() {
        StorageFactory storageFactory = new StorageFactory();
        storageFactory.setSetup(configuration);
        Storage runtimeStorage = storageFactory.createRuntimeStorage();
        Storage configStorage = storageFactory.createConfigStorage();

        Assert.assertTrue(runtimeStorage instanceof S3Storage);
        Assert.assertTrue(configStorage instanceof S3Storage);
    }

}