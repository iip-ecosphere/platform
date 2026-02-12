/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.support.plugins.ChildFirstIndexedClassloader;
import de.iip_ecosphere.platform.support.plugins.ChildFirstIndexedClassloader.LoaderIndex;
import org.junit.Assert;

/**
 * Tests {@link ChildFirstIndexedClassloader}.
 * 
 * @author Holger Eichelberger, SSE
 *
 */
public class ChildFirstIndexedClassLoaderTest {

    
    /**
     * Tests {@link ChildFirstIndexedClassloader}.
     * 
     * @throws IOException if files are not found
     * @throws ClassNotFoundException the the expected class is not found
     */
    @Test
    public void testLoader() throws IOException, ClassNotFoundException {
        final File indexFile = new File("src/test/resources/index.idx");
        final String clsName = "indexLoaderTest.ClassToLoad";
        final String resName = "testResource.txt";
        final String location = "src/test/resources/index.jar";

        LoaderIndex index = new ChildFirstIndexedClassloader.LoaderIndex();
        LoaderIndex.addToIndex(index, true, clsName, location);
        LoaderIndex.addToIndex(index, false, resName, location);
        LoaderIndex.toFile(index, indexFile);
        System.out.println("index.idx written");

        ChildFirstIndexedClassloader loader = new ChildFirstIndexedClassloader(indexFile, null);
        Class<?> cls = loader.loadClass(clsName);
        Assert.assertNotNull(cls);
        Assert.assertEquals(clsName, cls.getName());
        Assert.assertNotNull(loader.findResource(resName));
        Assert.assertNotNull(loader.getResource(resName));
        Assert.assertNotNull(loader.getResourceAsStream(resName));
    }
    
}
