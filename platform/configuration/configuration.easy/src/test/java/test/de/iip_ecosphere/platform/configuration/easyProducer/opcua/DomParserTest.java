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

package test.de.iip_ecosphere.platform.configuration.easyProducer.opcua;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.easy.loader.ManifestLoader;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.parser.DomParser;
import de.iip_ecosphere.platform.support.FileUtils;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * Tests {@link DomParser}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DomParserTest {

    /**
     * Tests processing one explicitly supplied companion specification.
     */
    @Test
    public void testDomParserSingleInput() {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
        Assert.assertTrue(in.isFile());
        File out = new File("target/gen/OpcMachineTool.ivml");
        out.getParentFile().mkdirs();
        if (out.exists()) {
            Assert.assertTrue(out.delete());
        }
        DomParser.setDefaultVerbose(false);
        DomParser.setUsingIvmlFolder("target/tmp");

        DomParser.main(new String[] {in.toString()});

        Assert.assertTrue(out.isFile());
    }

    /**
     * Tests deriving technical IVML model names.
     *
     * @throws ReflectiveOperationException shall not occur
     */
    @Test
    public void testDomParserModelNameDerivation() throws ReflectiveOperationException {
        Method method = DomParser.class.getDeclaredMethod("getModelName", String.class);
        method.setAccessible(true);
        String[][] names = {
            {"Opc.Ua.MachineTool.NodeSet2.xml", "MachineTool"},
            {"Machine-Tool.xml", "Machine_Tool"},
            {"Machine_Tool.xml", "Machine_Tool"},
            {"Machine.Tool.v1.xml", "MachineToolv1"},
            {"Machine  \t Tool.xml", "MachineTool"}
        };
        for (String[] name : names) {
            Assert.assertEquals(name[1], method.invoke(null, name[0]));
        }
    }

    /**
     * Tests processing and loading a NodeSet with whitespace in its file name.
     *
     * @throws IOException shall not occur
     * @throws ModelManagementException shall not occur
     */
    @Test
    public void testDomParserWhitespaceModelName() throws IOException, ModelManagementException {
        File nodeSets = new File("src/test/resources/NodeSets");
        File testFolder = new File("target/tmp/domParserWhitespaceModelName");
        File output = new File("target/gen/OpcMachineTool.ivml");
        File invalidOutput = new File("target/gen/OpcMachine Tool.ivml");
        if (testFolder.exists()) {
            FileUtils.deleteDirectory(testFolder);
        }
        Assert.assertTrue(testFolder.mkdirs());
        FileUtils.copyDirectory(new File(nodeSets, "RequiredModels"), new File(testFolder, "RequiredModels"));
        File sourceFile = new File(testFolder, "Machine Tool.xml");
        FileUtils.copyFile(new File(nodeSets, "Opc.Ua.MachineTool.NodeSet2.xml"), sourceFile);
        output.delete();
        invalidOutput.delete();
        DomParser.setDefaultVerbose(false);
        DomParser.setUsingIvmlFolder(new File(testFolder, "connector").getPath());

        try {
            DomParser.main(new String[] {sourceFile.getPath()});

            Assert.assertTrue(output.isFile());
            String contents = FileUtils.readFileToString(output, Charset.forName("UTF-8"));
            Assert.assertTrue(contents.startsWith("project OpcMachineTool {"));
            Assert.assertFalse(invalidOutput.exists());
            assertModelLoads(output.getParentFile(), "OpcMachineTool");
        } finally {
            output.delete();
            invalidOutput.delete();
            FileUtils.deleteDirectory(testFolder);
            DomParser.setUsingIvmlFolder("target/tmp");
        }
    }

    /**
     * Asserts that {@code modelName} can be loaded from {@code modelFolder}.
     *
     * @param modelFolder the model folder
     * @param modelName the model name
     * @throws IOException shall not occur
     * @throws ModelManagementException shall not occur
     */
    private static void assertModelLoads(File modelFolder, String modelName)
        throws IOException, ModelManagementException {
        File metaModelFolder = new File("src/main/easy");
        ManifestLoader loader = new ManifestLoader(false, DomParserTest.class.getClassLoader());
        loader.startup();
        EasyExecutor executor = new EasyExecutor(new File("."), metaModelFolder, modelName);
        executor.prependIvmlFolder(modelFolder);
        try {
            executor.setupLocations();
            executor.loadIvmlModel();
            Configuration configuration = executor.getConfiguration();
            Assert.assertNotNull(configuration);
            Assert.assertEquals(modelName, configuration.getProject().getName());
        } finally {
            executor.discardLocations();
            executor.clearModels();
            loader.shutdown();
        }
    }
    
    /**
     * Tests {@link DomParser} on the machine tool companion spec XML.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserMachineTool() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
        Assert.assertTrue(in.exists());
        File tmp = new File("target/tmp");
        tmp.mkdirs();
        File out = new File(tmp, "OpcMachineTool.ivml");
        // implicit from in to out
        DomParser.setDefaultVerbose(false); // reduce output
        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.main(new String[] {in.toString()});
        DomParser.process(in, "MachineTool", out, false);
        
        Charset charset = Charset.forName("UTF-8");
        File expected = new File("src/test/resources/OpcMachineTool.ivml");
        String exContents = normalize(FileUtils.readFileToString(expected, charset));
        String outContents = normalize(FileUtils.readFileToString(out, charset));
        Assert.assertEquals(exContents, outContents);
    }

    /**
     * Tests {@link DomParser} on the woodworking companion spec XML.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserWoodworking() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
        Assert.assertTrue(in.exists());
        File tmp = new File("target/tmp");
        tmp.mkdirs();
        File out = new File(tmp, "OpcWoodworking.ivml");
        // implicit from in to out
        DomParser.setDefaultVerbose(false); // reduce output
        new File("target/ivml").mkdirs();
        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.process(in, "Woodworking", out, false);
        
        Charset charset = Charset.forName("UTF-8");
        File expected = new File("src/test/resources/OpcWoodworking.ivml");
        String exContents = normalize(FileUtils.readFileToString(expected, charset));
        String outContents = normalize(FileUtils.readFileToString(out, charset));
        Assert.assertEquals(exContents, outContents);
    }

    /**
     * Helper function to indicate char differences to apply when string comparison fails.
     * 
     * @param exContents the expected contents
     * @param outContents the actual contents
     */
    static void printCharDiff(String exContents, String outContents) {
        for (int i = 0; i < Math.min(exContents.length(), outContents.length()); i++) {
            if (exContents.charAt(i) != outContents.charAt(i)) {
                System.out.println(((int) exContents.charAt(i)) + " " + ((int) outContents.charAt(i)));
            }
        }
    }

    /**
     * Normalizes unicode/UTF-8 strings for comparison (heuristics). This is just a hack. Any normalization solution 
     * solving that problem is welcome.
     * 
     * @param text the text to be normalized
     * @return the normalized text
     */
    private static String normalize(String text) {
        StringBuilder tmp = new StringBuilder(text);
        for (int i = 0; i < tmp.length(); i++) {
            int c = (int) tmp.charAt(i);
            if (c == 172) {
                tmp.setCharAt(i, (char) 45);
            } else if (c == 8211 || c == 65533) {
                tmp.setCharAt(i, '-');
            } else if (c == 8804) {
                tmp.setCharAt(i, (char) 63);
            } else if (c == 8217 || c == 8222 || c == 8220 || c == 8230) {
                tmp.setCharAt(i, (char) 45);
            }
        }
        return tmp.toString();
    }

}
