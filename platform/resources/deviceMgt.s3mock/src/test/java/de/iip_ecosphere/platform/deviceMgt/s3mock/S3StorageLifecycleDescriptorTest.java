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
import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.Test;

import de.iip_ecosphere.platform.deviceMgt.DeviceMgtSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactory;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageServerSetup;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.junit.Assert;

/**
 * Tests the storage factory lifecycle descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class S3StorageLifecycleDescriptorTest {

    /**
     * Tests the lifecycle descriptor no server setup.
     */
    @Test
    public void testLifecycleDescriptor_nullServerSetup() {
        DeviceMgtSetup setup = new DeviceMgtSetup();
        setup.setStorageServer(null);
        StorageFactory.getInstance().setSetup(setup);
        
        assertLifecycleDescriptor();
    }

    /**
     * Tests the lifecycle descriptor no port setup.
     */
    @Test
    public void testLifecycleDescriptor_noPortServerSetup() {
        StorageServerSetup serverSetup = new StorageServerSetup();
        serverSetup.setPort(-1);
        
        DeviceMgtSetup setup = new DeviceMgtSetup();
        setup.setStorageServer(serverSetup);
        StorageFactory.getInstance().setSetup(setup);
        
        assertLifecycleDescriptor();
    }

    /**
     * Tests the lifecycle descriptor null file path setup.
     */
    @Test
    public void testLifecycleDescriptor_inMemServerSetupNull() {
        StorageServerSetup serverSetup = new StorageServerSetup();
        serverSetup.setPort(NetUtils.getEphemeralPort());
        serverSetup.setPath(null);
        
        DeviceMgtSetup setup = new DeviceMgtSetup();
        setup.setStorageServer(serverSetup);
        StorageFactory.getInstance().setSetup(setup);
        
        assertLifecycleDescriptor();
    }

    /**
     * Tests the lifecycle descriptor empty file path setup.
     */
    @Test
    public void testLifecycleDescriptor_inMemServerSetupEmpty() {
        StorageServerSetup serverSetup = new StorageServerSetup();
        serverSetup.setPort(NetUtils.getEphemeralPort());
        serverSetup.setPath(new File(""));
        
        DeviceMgtSetup setup = new DeviceMgtSetup();
        setup.setStorageServer(serverSetup);
        StorageFactory.getInstance().setSetup(setup);
        
        assertLifecycleDescriptor();
    }

    /**
     * Tests the lifecycle descriptor with file path setup.
     */
    @Test
    public void testLifecycleDescriptor_inMemServerSetupFile() {
        File f = FileUtils.createTmpFolder("s3mock");
        FileUtils.deleteQuietly(f);
        f.mkdirs();
        
        StorageServerSetup serverSetup = new StorageServerSetup();
        serverSetup.setPort(NetUtils.getEphemeralPort());
        serverSetup.setPath(f);
        
        DeviceMgtSetup setup = new DeviceMgtSetup();
        setup.setStorageServer(serverSetup);
        StorageFactory.getInstance().setSetup(setup);
        
        assertLifecycleDescriptor();
        FileUtils.deleteQuietly(f);
    }

    /**
     * Asserts and executes the lifecycle descriptor of this extension.
     */
    private static void assertLifecycleDescriptor() {
        Optional<LifecycleDescriptor> result = ServiceLoaderUtils.stream(ServiceLoader.load(LifecycleDescriptor.class))
            .filter(l -> l instanceof S3StorageLifecycleDescriptor)
            .findFirst();
        Assert.assertTrue(result.isPresent());
        
        LifecycleDescriptor desc = result.get();
        desc.startup(new String[] {});
        desc.shutdown();
    }

}
