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

package de.iip_ecosphere.platform.deviceMgt.storage;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.iip_ecosphere.platform.deviceMgt.Configuration;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Tests the storage factory.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StorageFactoryTest {

    private Configuration configuration;

    /**
     * Configures the test.
     */
    @Before
    public void setUp() {
        configuration = new Configuration();
        PackageStorageSetup packageStorageSetup = new PackageStorageSetup();
        packageStorageSetup.setRegion("us-west-2");
        packageStorageSetup.setEndpoint("endpoint");
        packageStorageSetup.setBucket("bucket");
        packageStorageSetup.setAccessKey("access_key");
        packageStorageSetup.setSecretAccessKey("secret_access_key");
        packageStorageSetup.setPackageDescriptor("runtime.yml");
        packageStorageSetup.setPackageFilename("runtime.zip");
        packageStorageSetup.setPrefix("runtimes/");

        configuration.setRuntimeStorage(packageStorageSetup);
        PackageStorageSetup configsStorageSetup = new PackageStorageSetup();
        configsStorageSetup.setEndpoint("endpoint");
        configsStorageSetup.setRegion("us-west-1");
        configsStorageSetup.setBucket("bucket");
        configsStorageSetup.setAccessKey("access_key");
        configsStorageSetup.setSecretAccessKey("secret_access_key");
        configsStorageSetup.setPackageDescriptor("config.yml");
        configsStorageSetup.setPackageFilename("config.zip");
        configsStorageSetup.setPrefix("configs/");
        configuration.setConfigStorage(configsStorageSetup);
    }
    
    /**
     * Tests that creating a runtime storage without service provider leads to <b>null</b> as storage implementation. 
     */
    @Test
    public void createStorages_withoutServiceProvider_isNull() {
        MockedStatic<ServiceLoaderUtils> serviceLoaderMock = Mockito.mockStatic(ServiceLoaderUtils.class);
        serviceLoaderMock.when(() -> ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class))
                .thenReturn(Optional.empty());

        StorageFactory storageFactory = new StorageFactory();
        storageFactory.setConfiguration(configuration);
        Storage runtimeStorage = storageFactory.createRuntimeStorage();
        Storage configStorage = storageFactory.createConfigStorage();

        Assert.assertNull(runtimeStorage);
        Assert.assertNull(configStorage);

        serviceLoaderMock.close();
    }

}
