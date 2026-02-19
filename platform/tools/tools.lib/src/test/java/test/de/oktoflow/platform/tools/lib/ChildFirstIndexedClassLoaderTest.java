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

package test.de.oktoflow.platform.tools.lib;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.oktoflow.platform.tools.lib.loader.IndexClassloader;
import de.oktoflow.platform.tools.lib.loader.LoaderIndex;

import org.junit.Assert;

/**
 * Tests {@link IndexClassloader}.
 * 
 * @author Holger Eichelberger, SSE
 *
 */
public class ChildFirstIndexedClassLoaderTest {

    
    /**
     * Tests {@link IndexClassloader}.
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

        LoaderIndex index = new LoaderIndex();
        LoaderIndex.addToIndex(index, true, clsName, location);
        LoaderIndex.addToIndex(index, false, resName, location);
        LoaderIndex.toFile(index, indexFile);
        System.out.println("index.idx written");
        
        Assert.assertTrue(index.getClassesCount() > 0);
        Assert.assertTrue(index.getResourcesCount() > 0);
        Assert.assertTrue(index.getLocationsCount() > 0);
        Assert.assertNotNull(index.getClassLocation(clsName));
        Assert.assertNull(index.getClassLocation("abc"));
        Assert.assertNotNull(index.getResourceLocations(resName));
        Assert.assertNull(index.getResourceLocations("abc"));

        IndexClassloader loader = new IndexClassloader(indexFile, null);
        Class<?> cls = loader.loadClass(clsName);
        Assert.assertNotNull(cls);
        Assert.assertEquals(clsName, cls.getName());
        Assert.assertNotNull(loader.findResource(resName));
        Assert.assertNotNull(loader.getResource(resName));
        Assert.assertNotNull(loader.getResourceAsStream(resName));
        loader.close();
    }
    
}
