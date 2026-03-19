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

package test.de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.easyProducer.EasySetup;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiatorExecutor;
import de.iip_ecosphere.platform.support.FileUtils;

/**
 * Tests the SimpleMesh model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlSimpleMeshTests extends AbstractIvmlTests {

    /**
     * Tests loading, reasoning and instantiating "SimpleMesh", a simple, generated service chain for testing. Here, we 
     * do not instantiate the full platform rather than only the configured apps. Depending on Maven setup/exclusions, 
     * this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSimpleMesh() throws ExecutionException, IOException {
        // mvn: stdout now in target/surefire-reports/<qualifiedClassName>-output.txt
        File tmp = new File(FileUtils.getTempDirectory(), "oktoflow2grafana.cfg");
        FileUtils.deleteQuietly(tmp);
        Properties prop = new Properties();
        prop.put("modelFolder", new File(MODEL_BASE_FOLDER, "simpleMesh").getAbsolutePath());
        prop.put("additionalIvmlFolders", new File(MODEL_BASE_FOLDER, "common").getAbsolutePath());
        prop.put("metamodelFolder", new File(EasySetup.getTestingEasyModelParent(), "src/main/easy").getAbsolutePath());
        try (PrintStream out = new PrintStream(new FileOutputStream(tmp))) {
            prop.store(out, "Temporarily created by testSimpleMesh");
        }
        
        File gen = new File(TEST_BASE_FOLDER, "SimpleMesh");
        PlatformInstantiatorExecutor.instantiate(
            genApps(new TestConfigurer("PlatformConfiguration", new File(MODEL_BASE_FOLDER, "simpleMesh"), gen)));
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.SimpleMeshTestingApp");
    }
    
}
