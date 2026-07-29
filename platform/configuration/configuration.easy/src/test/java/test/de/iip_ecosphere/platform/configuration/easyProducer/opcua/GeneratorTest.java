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

package test.de.iip_ecosphere.platform.configuration.easyProducer.opcua;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor.ExecutionMode;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlUtils;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.parser.DomParser;
import de.iip_ecosphere.platform.support.FileUtils;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import test.de.iip_ecosphere.platform.configuration.easyProducer.AbstractIvmlTests;

/**
 * Tests the OPC UA connector settings generator.
 *
 * @author Codex, SSE
 */
public class GeneratorTest extends AbstractIvmlTests {

    /**
     * Tests that generated connector settings comply with the current IVML meta model.
     *
     * @throws IOException if handling the generated files fails
     */
    @Test
    public void testGeneratedConnectorSettings() throws IOException {
        File output = new File("target/generated-opcua-connector-test");
        FileUtils.deleteDirectory(output);
        Assert.assertTrue(output.mkdirs());
        DomParser.setUsingIvmlFolder(output.getPath());
        ConfigurationLifecycleDescriptor lifecycle = null;
        try {
            File input = new File("src/test/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
            DomParser.process(input, "Woodworking", new File(output, "OpcWoodworking.ivml"), false);

            TestConfigurer configurer = new TestConfigurer("VDW", output, TEST_BASE_FOLDER);
            ConfigurationSetup setup = ConfigurationSetup.getSetup(false);
            configurer.configure(setup);
            lifecycle = configurer.obtainLifecycleDescriptor();
            lifecycle.startup(ExecutionMode.IVML_QUIET, new String[0]);
            ConfigurationManager.reInit();
            ReasoningResult result = ConfigurationManager.validateAndPropagate();
            Assert.assertNotNull("Generated VDW model was not loaded", result);
            Assert.assertFalse(IvmlUtils.analyzeReasoningResult(result, false, true));

            String generated = FileUtils.readFileToString(new File(output, "VDW.ivml"),
                StandardCharsets.UTF_8);
            Assert.assertFalse(generated.contains("RecordType opcOut = {\n        path ="));
            Assert.assertTrue(generated.contains("inInterface = {{type=refBy(opcIn)}}"));
            Assert.assertTrue(generated.contains(
                "outInterface = {{type=refBy(opcOut), path=\"PLACEHOLDER\"}}"));
        } finally {
            if (lifecycle != null) {
                lifecycle.shutdown();
            }
            ConfigurationSetup.getSetup(false).getEasyProducer().reset();
            DomParser.setUsingIvmlFolder("target/tmp");
            FileUtils.deleteQuietly(output);
        }
    }

}
