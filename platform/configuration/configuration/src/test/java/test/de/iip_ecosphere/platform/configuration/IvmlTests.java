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
import java.io.IOException;
import java.nio.charset.Charset;
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
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;

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
     * Depending on Maven setup/exclusions, this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSerializerConfig1() throws ExecutionException, IOException {
        ConfigurationSetup setup = ConfigurationSetup.getConfiguration();
        setup.setIvmlModelName("SerializerConfig1");
        setup.setIvmlConfigFolder(new File("src/test/easy"));
        File gen = new File("gen/tests/SerializerConfig1");
        FileUtils.deleteQuietly(gen);
        gen.mkdirs();
        setup.setGenTarget(gen);
        ConfigurationLifecycleDescriptor lcd = assertLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        Assert.assertNotNull(ConfigurationManager.getIvmlConfiguration());
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        EasyExecutor.printReasoningMessages(rRes);
        Assert.assertFalse(rRes.hasConflict());
        ConfigurationManager.instantiate(); // throws exception if it fails
        lcd.shutdown();
        setup.reset();
        
        assertFile(gen, "app/src/main/java/iip/datatypes/Rec1.java");
        assertFile(gen, "app/src/main/java/iip/serializers/Rec1Serializer.java");
        assertFileContains(gen, "app/pom.xml", "transport.spring.amqp", "transport.amqp");
        
        assertFileContains(gen, "ecsRuntime/pom.xml", "ecsRuntime.docker", "transport.amqp", "support.aas.basyx");
        assertFile(gen, "ecsRuntime/src/main/resources/ecsRuntime.yml");
        
        assertFileContains(gen, "serviceMgr/pom.xml", "services.spring", "transport.amqp", "support.aas.basyx");
        assertFile(gen, "serviceMgr/src/main/resources/application.yml");
        
        assertFileContains(gen, "platform/pom.xml", "support.aas.basyx.server", "support.aas.basyx", 
            "configuration.configuration", "transport.amqp");
    }
    
    /**
     * Asserts that the specified file exists and has contents.
     * 
     * @param base the base folder
     * @param name the name/path to the file
     * @return the actual asserted file ({@code base} + {@code name})
     */
    private static File assertFile(File base, String name) {
        File f = new File(base, name);
        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.length() > 0);
        return f;
    }

    /**
     * Asserts that the specified file exists, has contents and contains the specified {@code search} string(s).
     * 
     * @param base the base folder
     * @param name the name/path to the file
     * @param search the content/search strings to assert
     * @throws IOException if the file cannot be read
     */
    private static void assertFileContains(File base, String name, String... search) throws IOException {
        File f = assertFile(base, name);
        String contents = org.apache.commons.io.FileUtils.readFileToString(f, Charset.defaultCharset());
        for (String s : search) {
            Assert.assertTrue("File " + f + " must contain '" + s + "'", contents.contains(s));
        }
    }
    
}
