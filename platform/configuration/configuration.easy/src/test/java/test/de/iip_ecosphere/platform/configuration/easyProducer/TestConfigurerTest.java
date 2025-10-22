/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.easyProducer.EasySetup;
import test.de.iip_ecosphere.platform.configuration.easyProducer.AbstractIvmlTests.TestConfigurer;

/**
 * Tests the {@link TestConfigurer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestConfigurerTest {

    /**
     * Tests the test configurer "serialization" into command line parameters.
     */
    @Test
    public void testConfigurer() {
        TestConfigurer t = new TestConfigurer("ContainerCreation", 
            new File(EasySetup.getTestingEasyModelParent(), "src/test/easy/single"), new File("."));
        File ivmlMeta = t.getIvmlMetaModelFolder();
        List<File> addIvml = t.getAdditionalIvmlFolders();
        String[] args = t.toArgs(true);
        System.out.println(Arrays.toString(args));
        TestConfigurer t1 = new TestConfigurer(args);
        Assert.assertTrue(t1.getProperties().isEmpty());
        Assert.assertEquals(ivmlMeta, t1.getIvmlMetaModelFolder());
        Assert.assertEquals(addIvml, t1.getAdditionalIvmlFolders());
        Assert.assertEquals(t.getIvmlModelName(), t1.getIvmlModelName());
        Assert.assertEquals(t.getModelFolder(), t1.getModelFolder());
        Assert.assertEquals(t.getMetaModelFolder(), t1.getMetaModelFolder());
        Assert.assertEquals(t.getStartRuleName(), t1.getStartRuleName());
        Assert.assertEquals(t.getModelFolder(), t1.getModelFolder());
        Assert.assertEquals(t.getOutputFolder(), t1.getOutputFolder());
        
        t = new TestConfigurer("ContainerCreation", 
            new File(EasySetup.getTestingEasyModelParent(), "src/test/easy/single"), new File("."))
            .setProperty("a", "b");
        ivmlMeta = t.getIvmlMetaModelFolder();
        addIvml = t.getAdditionalIvmlFolders();
        args = t.toArgs(true);
        System.out.println(Arrays.toString(args));        
        t1 = new TestConfigurer(args);
        Assert.assertFalse(t1.getProperties().isEmpty());
        Assert.assertEquals(1, t1.getProperties().size());
        Assert.assertEquals(ivmlMeta, t1.getIvmlMetaModelFolder());
        Assert.assertEquals(addIvml, t1.getAdditionalIvmlFolders());
        Assert.assertEquals(t.getIvmlModelName(), t1.getIvmlModelName());
        Assert.assertEquals(t.getModelFolder(), t1.getModelFolder());
        Assert.assertEquals(t.getMetaModelFolder(), t1.getMetaModelFolder());
        Assert.assertEquals(t.getStartRuleName(), t1.getStartRuleName());
        Assert.assertEquals(t.getModelFolder(), t1.getModelFolder());
        Assert.assertEquals(t.getOutputFolder(), t1.getOutputFolder());
    }
    
}
