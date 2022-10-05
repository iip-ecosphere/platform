/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.vdw;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasyLogLevel;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.examples.vdw.GeneratedConnector;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import test.de.iip_ecosphere.platform.configuration.IvmlTests;

/**
 * OPC UA test with generated platform/connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcUaModelTest {

    /**
     * Reusable test configuration/setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestConfigurer extends InstantiationConfigurer {

        /**
         * Creates a configurer instance.
         * 
         * @param ivmlModelName the name of the IVML model representing the topmost platform configuration
         * @param modelFolder the folder where the model is located (ignored if <b>null</b>)
         * @param outputFolder the output folder for code generation
         */
        public TestConfigurer(String ivmlModelName, File modelFolder, File outputFolder) {
            super(ivmlModelName, modelFolder, outputFolder);
        }

        
        @Override
        protected void validateConfiguration(Configuration conf) throws ExecutionException {
            Assert.assertNotNull(conf);
        }
        
        @Override
        protected void validateReasoningResult(ReasoningResult res) throws ExecutionException {
            Assert.assertFalse(res.hasConflict());
        }
        
        @Override
        protected void handleExecutionException(ExecutionException ex) throws ExecutionException {
            throw ex;
        }

        @Override
        public void configure(ConfigurationSetup setup) {
            super.configure(setup);
            EasySetup easySetup = setup.getEasyProducer();
            easySetup.setLogLevel(EasyLogLevel.VERBOSE); // override for debugging
            File modelFolder;
            String folder = System.getProperty("easyModelFolder", null);
            if (null == folder) {
                modelFolder = new File("src/main/easy").getAbsoluteFile();
            } else {
                modelFolder = new File(folder);
            }
            try {            
                modelFolder = modelFolder.getCanonicalFile();
            } catch (IOException e) {
                Assert.fail("Cannot create canonical file name for " + modelFolder);
            }
            easySetup.setIvmlMetaModelFolder(modelFolder);
        }

    }
    
    /**
     * Tests the VDW platform/connector configuration.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testVdw() throws ExecutionException, IOException {
        File gen = new File("gen/vdw");
        PlatformInstantiator.instantiate(
            IvmlTests.genApps(new TestConfigurer("VDW", new File("src/test/easy"), gen)));
    }
    
    /**
     * Runs the generated connector. Currently not integrated with test as the VDW OPC server must be online 
     * (external, not guaranteed).
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the VDW server cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        GeneratedConnector.main(args);
    }

}
