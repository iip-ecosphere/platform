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

package test.de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Tests the configuration component, in particular the IVML models.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlTests {

    /**
     * Asserts and returns an instance of the configuration lifecycle descriptor.
     * 
     * @return the configuration lifecycle descriptor instance 
     */
    private ConfigurationLifecycleDescriptor assertLifecycleDescriptor() {
        // check that the registration works, but do not execute all descriptors
        ServiceLoader<LifecycleDescriptor> loader = ServiceLoader.load(LifecycleDescriptor.class);
        Optional<LifecycleDescriptor> first = ServiceLoaderUtils
            .stream(loader)
            .filter(s -> s instanceof ConfigurationLifecycleDescriptor)
            .findFirst();
        Assert.assertTrue(first.isPresent());
        ConfigurationLifecycleDescriptor lcd = (ConfigurationLifecycleDescriptor) first.get(); 
        Assert.assertNotNull(lcd);
        return lcd;
    }
    
    /**
     * Tests loading the meta model.
     */
    @Test
    public void testMetaModel() {
        ConfigurationLifecycleDescriptor lcd = assertLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        Assert.assertNotNull(ConfigurationManager.getIvmlConfiguration());
        // not much to do, no configuration, shall work anyway
        Assert.assertFalse(ConfigurationManager.validateAndPropagate().hasConflict());
        lcd.shutdown();
    }

    /**
     * Tests loading, reasoning and instantiating "SerializerConfig1".
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testSerializerConfig1() throws ExecutionException {
        ConfigurationSetup setup = ConfigurationSetup.getConfiguration();
        setup.setIvmlModelName("SerializerConfig1");
        setup.setIvmlConfigFolder(new File("src/test/easy"));
        File gen = FileUtils.createTmpFolder("gen-SerializerConfig1", true);
        setup.setGenTarget(gen);
        ConfigurationLifecycleDescriptor lcd = assertLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        Assert.assertNotNull(ConfigurationManager.getIvmlConfiguration());
        Assert.assertFalse(ConfigurationManager.validateAndPropagate().hasConflict());
        // throws exception if it fails
        ConfigurationManager.instantiate();
        lcd.shutdown();
        setup.reset();
        assertFile(gen, "src/main/java/iip/datatypes/Rec1.java");
        assertFile(gen, "src/main/java/iip/serializers/Rec1Serializer.java");
        FileUtils.deleteQuietly(gen);
    }
    
    /**
     * Asserts that the specified file exists and has contents.
     * 
     * @param base the base folder
     * @param name the name/path to the file
     */
    private static void assertFile(File base, String name) {
        File f = new File(base, name);
        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.length() > 0);
    }
    
}
