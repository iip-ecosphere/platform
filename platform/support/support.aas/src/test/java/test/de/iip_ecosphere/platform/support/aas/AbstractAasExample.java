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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe.FileResource;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Base class for AAS examples/tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAasExample {

    private List<Aas> aasList = new ArrayList<Aas>();
    private Map<String, Aas> parts = new TreeMap<>();
    private List<FileResource> resources = new ArrayList<FileResource>();
    private boolean createOperations = true;
    private boolean createMultiLanguageProperties = true;
    private File tempFolder = new File(FileUtils.getTempDirectory(), getFolderName());

    /**
     * Returns the temporary folder.
     * 
     * @return the temporary folder
     */
    protected File getTempFolder() {
        return tempFolder;
    }
    
    /**
     * Indicates the name of the resources/temporary folder for this example.
     * 
     * @return the folder name
     */
    protected abstract String getFolderName();

    /**
     * Returns the resource folder (prefix).
     * 
     * @return the resource folder
     */
    protected String getResourceFolder() {
        return getFolderName() + "/";
    }
    
    /**
     * Returns the target files for persisting the AAS.
     * 
     * @return the target files
     */
    public abstract File[] getTargetFiles();

    /**
     * Enables/disables creating operations.
     * 
     * @param createOperations shall we create operations
     */
    public void setCreateOperations(boolean createOperations) {
        this.createOperations = createOperations;
    }

    /**
     * Enables/disables creating multi-language properties.
     * 
     * @param createMultiLanguageProperties shall we create multi-language properties
     */
    public void setCreateMultiLanguageProperties(boolean createMultiLanguageProperties) {
        this.createMultiLanguageProperties = createMultiLanguageProperties;
    }

    /**
     * Returns whether creating operations is enabled/disabled.
     * 
     * @return shall we create operations
     */
    protected boolean isCreateOperations() {
        return this.createOperations;
    }

    /**
     * Returns whether creating multi-language properties is enabled/disabled.
     * 
     * @return shall we create multi-language properties
     */
    protected boolean isCreateMultiLanguageProperties() {
        return this.createMultiLanguageProperties;
    }

    /**
     * Registers a created AAS (for persisting it).
     * 
     * @param aasBuilder the AAS builder
     * @return the AAS
     * @see #registerAas(Aas)
     */
    protected Aas registerAas(AasBuilder aasBuilder) {
        return registerAas(aasBuilder.build());
    }

    /**
     * Registers a created AAS (for persisting it).
     * 
     * @param aas the AAS
     * @return {@code aas}
     */
    protected Aas registerAas(Aas aas) {
        aasList.add(aas);
        parts.put("node_" + aas.getIdShort(), aas);
        return aas;
    }
    
    /**
     * Registers a resource (once).
     * 
     * @param resource the resource to be registered
     * @return {@code resource}
     */
    protected FileResource registerResource(FileResource resource) {
        if (!resources.stream().anyMatch(r -> resource.getPath().equals(r.getPath()))) {
            resources.add(resource);
        }
        return resource;
    }
    
    /**
     * Iterates over all parts.
     * 
     * @param consumer the iterator consumer
     */
    protected void forEachPart(BiConsumer<? super String, ? super Aas> consumer) {
        parts.forEach(consumer);
    }

    /**
     * Returns a resource as a file. As we store resources on the class path and test execution happens in the
     * specific AAS implementations, we need store a copy in the temporary folder.
     * 
     * @param name the name of the resource
     * @return the file or <b>null</b> if the ressource cannot be found/stored temporarily
     * @see #getResourceFolder()
     */
    protected File getFileResource(String name) {
        File result = null;
        InputStream in = ResourceLoader.getResourceAsStream(getResourceFolder() + name);
        if (null != in) {
            File parent = getTempFolder();
            parent.mkdirs();
            File tmp = new File(parent, name);
            if (tmp.exists()) { // we assume it's the right one then
                result = tmp;
            } else {
                try {
                    FileUtils.copyInputStreamToFile(in, tmp);
                    result = tmp;
                    result.deleteOnExit();
                } catch (IOException e) {
                    System.err.println("Cannot write resource to temporary folder. Ignoring resource " + name);
                }
            }
        } else {
            System.err.println("Cannot find resource on classpath. Ignoring resource " + name);
        }
        return result;
    }
    
    /**
     * Tests creating and storing the AAS.
     * 
     * @throws IOException if persisting does not work.
     */
    @Test
    public void testCreateAndStore() throws IOException {
        testCreateAndStore(true);
    }
    
    /**
     * Tests creating and storing the AAS.
     * 
     * @param compare compare against the stored AAS spec by {@link AasSpecVisitor}
     * @throws IOException if persisting does not work.
     * @see #createAas()
     * @see #getThumbnail()
     * @see #assertAllAas()
     */
    public void testCreateAndStore(boolean compare) throws IOException {
        FileUtils.deleteQuietly(getTempFolder());
        createAas();
        File thumbnail = getThumbnail();
        for (File aasx: getTargetFiles()) {
            aasx.getParentFile().mkdirs();
            AasFactory.getInstance().createPersistenceRecipe().writeTo(aasList, thumbnail, resources, aasx);
        }
        FileUtils.deleteQuietly(getTempFolder());
        if (compare) {
            assertAllAas();
        }
    }

    /**
     * Returns the thumbnail for persisting AAS.
     * 
     * @return the thumbnail
     */
    protected abstract File getThumbnail();
    
    /**
     * Creates the example/test AAS. Call {@link #registerAas(Aas)} on each created AAS,
     * {@link #registerResource(FileResource)} on each created resource.
     */
    protected abstract void createAas();
    
    /**
     * Asserts the structure of all created AAS.
     * 
     * @see #getResourceFolder()
     */
    protected void assertAllAas() {
        for (Aas aas : aasList) {
            AasSpecVisitor.assertEquals(aas, getResourceFolder());
        }
    }

    /**
     * Asserts properties on enum values.
     * 
     * @param <T> the enum type
     * @param values the values
     * @param asserter the asserter function
     */
    public static <T extends Enum<T>> void assertEnum(T[] values, Predicate<T> asserter) {
        for (T value: values) {
            Assert.assertTrue(asserter.test(value));
        }
    }
    
}
