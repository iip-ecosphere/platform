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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.BaseType;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.FieldMethodType;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.FieldObjectType;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.FieldType;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.FieldVariableType;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.MethodType;
import de.iip_ecosphere.platform.configuration.easyProducer.opcua.data.ObjectType;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
     * Tests the exact index threshold and verifies that indexed lookup keeps the
     * first-match behavior of the original linear lookup.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRelationIndexThresholdAndDuplicateIds()
            throws ReflectiveOperationException, ParserConfigurationException {
        Assert.assertNull(invokeBuildIndexIfBeneficial(createNodeList(50)));

        NodeList indexedNodes = createNodeList(51);
        Map<String, Element> thresholdIndex = invokeBuildIndexIfBeneficial(indexedNodes);
        Assert.assertNotNull(thresholdIndex);
        Assert.assertEquals(51, thresholdIndex.size());
        Assert.assertSame(indexedNodes.item(50), thresholdIndex.get("node-50"));

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("nodes");
        document.appendChild(root);
        Element first = appendNode(document, root, "duplicate");
        first.setAttribute("marker", "first");
        Element second = appendNode(document, root, "duplicate");
        second.setAttribute("marker", "second");
        appendNode(document, root, "");

        Map<String, Element> duplicateIndex = invokeBuildIndex(root.getChildNodes());
        Assert.assertEquals(1, duplicateIndex.size());
        Assert.assertSame(first, duplicateIndex.get("duplicate"));
        Assert.assertFalse(duplicateIndex.containsKey(""));
    }

    /**
     * Creates a DOM node list with unique NodeIds.
     *
     * @param count the number of nodes
     * @return the created node list
     * @throws ParserConfigurationException shall not occur
     */
    private static NodeList createNodeList(int count) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("nodes");
        document.appendChild(root);
        for (int i = 0; i < count; i++) {
            appendNode(document, root, "node-" + i);
        }
        return root.getChildNodes();
    }

    /**
     * Appends a node to {@code root}.
     *
     * @param document the owning document
     * @param root the parent element
     * @param nodeId the NodeId value
     * @return the appended element
     */
    private static Element appendNode(Document document, Element root, String nodeId) {
        Element node = document.createElement("node");
        if (nodeId != null) {
            node.setAttribute("NodeId", nodeId);
        }
        root.appendChild(node);
        return node;
    }

    /**
     * Invokes the production index builder.
     *
     * @param nodes the nodes to index
     * @return the index
     * @throws ReflectiveOperationException shall not occur
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Element> invokeBuildIndex(NodeList nodes) throws ReflectiveOperationException {
        Method method = DomParser.class.getDeclaredMethod("buildIndex", NodeList.class);
        method.setAccessible(true);
        return (Map<String, Element>) method.invoke(null, nodes);
    }

    /**
     * Invokes the production index-threshold decision.
     *
     * @param nodes the nodes to consider
     * @return the index, or {@code null}
     * @throws ReflectiveOperationException shall not occur
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Element> invokeBuildIndexIfBeneficial(NodeList nodes)
            throws ReflectiveOperationException {
        Method method = DomParser.class.getDeclaredMethod("buildIndexIfBeneficial", NodeList.class);
        method.setAccessible(true);
        return (Map<String, Element>) method.invoke(null, nodes);
    }

    /**
     * Tests creating and using a dedicated parser output folder.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserOutputFolder() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
        File output = new File("target/opcua-parser-test");
        File legacyOutput = new File("src/test/easy/VDW_ToolOutput.ivml");
        FileUtils.deleteDirectory(output);
        Assert.assertFalse(output.exists());
        Assert.assertFalse(legacyOutput.exists());
        DomParser.setUsingIvmlFolder(output.getPath());
        try {
            DomParser.process(in, "ToolOutput", new File(output, "OpcToolOutput.ivml"), false);
            Assert.assertTrue(new File(output, "OpcToolOutput.ivml").isFile());
            Assert.assertTrue(new File(output, "VDW.ivml").isFile());
            Assert.assertTrue(new File(output, "VDW_ToolOutput.ivml").isFile());
            Assert.assertFalse(legacyOutput.exists());
        } finally {
            DomParser.setUsingIvmlFolder("target/tmp");
            FileUtils.deleteDirectory(output);
        }
    }

    /**
     * Tests reporting an invalid parser output folder.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserInvalidOutputFolder() throws IOException {
        File output = new File("target/opcua-parser-output-file");
        FileUtils.deleteQuietly(output);
        Assert.assertTrue(output.createNewFile());
        DomParser.setUsingIvmlFolder(output.getPath());
        try {
            DomParser.process(new File("src/test/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml"),
                "ToolOutput", new File(output, "OpcToolOutput.ivml"), false);
            Assert.fail("Expected an invalid output folder to be rejected");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains(output.getPath()));
        } finally {
            DomParser.setUsingIvmlFolder("target/tmp");
            FileUtils.deleteQuietly(output);
        }
    }

    /**
     * Tests processing one explicitly supplied companion specification.
     */
    @Test
    public void testDomParserSingleInput() {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
        Assert.assertTrue(in.isFile());
        File out = new File("target/tmp/OpcMachineTool.ivml");
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
        File output = new File(testFolder, "connector/OpcMachineTool.ivml");
        File invalidOutput = new File(testFolder, "connector/OpcMachine Tool.ivml");
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
     * Tests processing explicit inputs exactly once in caller order.
     */
    @Test
    public void testDomParserMultipleInputs() {
        File first = new File("src/test/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
        File second = new File("src/test/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
        File firstOut = new File("target/tmp/OpcWoodworking.ivml");
        File secondOut = new File("target/tmp/OpcMachineTool.ivml");
        Assert.assertTrue(first.isFile());
        Assert.assertTrue(second.isFile());
        firstOut.getParentFile().mkdirs();
        if (firstOut.exists()) {
            Assert.assertTrue(firstOut.delete());
        }
        if (secondOut.exists()) {
            Assert.assertTrue(secondOut.delete());
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream previousOut = System.out;
        try {
            System.setOut(new PrintStream(buffer));
            DomParser.setDefaultVerbose(false);
            DomParser.setUsingIvmlFolder("target/tmp");
            DomParser.main(new String[] {first.toString(), second.toString()});
        } finally {
            System.setOut(previousOut);
        }

        Assert.assertTrue(firstOut.isFile());
        Assert.assertTrue(secondOut.isFile());
        String output = new String(buffer.toByteArray(), Charset.defaultCharset());
        String firstMessage = "Processing " + first;
        String secondMessage = "Processing " + second;
        Assert.assertTrue(output.indexOf(firstMessage) >= 0);
        Assert.assertTrue(output.indexOf(secondMessage) >= 0);
        Assert.assertTrue(output.indexOf(firstMessage) < output.indexOf(secondMessage));
        Assert.assertEquals(output.indexOf(firstMessage), output.lastIndexOf(firstMessage));
        Assert.assertEquals(output.indexOf(secondMessage), output.lastIndexOf(secondMessage));
    }
    

    /**
     * Tests propagation of XML parser failures.
     * Tests namespace-aware resolution if a colliding required model is loaded first.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testParserErrorPropagation() throws IOException {
        File tmp = new File("target/tmp");
        tmp.mkdirs();
        File invalid = File.createTempFile("invalid-opcua-input-", ".xml", tmp);
        try (FileWriter writer = new FileWriter(invalid)) {
            writer.write("<invalid>");
        }
        try {
            DomParser.process(invalid, "Invalid", new File(tmp, "OpcInvalid.ivml"), false);
            Assert.fail("Expected malformed XML input to be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains(invalid.toString()));
            Assert.assertTrue(e.getCause() instanceof SAXException);
        } finally {
            Assert.assertTrue(invalid.delete());
        }
    }    

    /**
     * Tests external reference resolution, shall fail first.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testExternalReferenceResolutionWrongFirst() throws IOException {
        assertExternalReferenceResolution("WrongFirst");
    }

    /**
     * Tests namespace-aware resolution if the referenced required model is loaded first.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testExternalReferenceResolutionCorrectFirst() throws IOException {
        assertExternalReferenceResolution("CorrectFirst");
    }

    /**
     * Tests rejecting an external reference with an unknown source namespace index.
     */
    @Test
    public void testUnknownExternalNamespaceIndex() {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.ExternalReferenceUnknownNamespace.NodeSet2.xml");
        Assert.assertTrue(in.isFile());
        File out = new File("target/tmp/ExternalReferenceUnknownNamespace.ivml");

        try {
            DomParser.process(in, "ExternalReferenceUnknownNamespace", out, false);
            Assert.fail("Expected an invalid namespace index to be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("No namespace URI"));
            Assert.assertTrue(e.getMessage().contains("ns=4;i=1002"));
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
        Assert.assertTrue(outContents.contains("UADataType opcNumberType = {"));
        Assert.assertTrue(outContents.contains("UADataType opcLocalizedTextType = {"));
        Assert.assertTrue(outContents.contains("UADataType opcUtcTimeType = {"));
        Assert.assertTrue(outContents.contains("UADataType opcNodeIdType = {"));
    }

    /**
     * Tests parsing the Machinery Jobs companion specification, including its
     * self-referencing ISA95 parameter data type.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserMachineryJobs() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.Machinery.Jobs.Nodeset2.xml");
        Assert.assertTrue(in.isFile());
        File out = new File("target/tmp/OpcMachineryJobs.ivml");
        out.getParentFile().mkdirs();
        DomParser.setDefaultVerbose(false);
        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.process(in, "MachineryJobs", out, false);

        Assert.assertTrue(out.isFile());
        String contents = FileUtils.readFileToString(out, Charset.forName("UTF-8"));
        Assert.assertTrue(contents.contains("UADataType opcISA95ParameterDataTypeType = {"));
        Assert.assertTrue(contents.contains("type = refBy(opcISA95ParameterDataTypeType)"));
    }

    /**
     * Tests parsing Machinery Examples with sibling Machinery models as dependencies.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserMachineryExamples() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.Machinery.Examples.NodeSet2.xml");
        Assert.assertTrue(in.isFile());
        File out = new File("target/tmp/OpcMachineryExamples.ivml");
        out.getParentFile().mkdirs();
        DomParser.setDefaultVerbose(false);
        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.process(in, "MachineryExamples", out, false);

        Assert.assertTrue(out.isFile());
        String contents = FileUtils.readFileToString(out, Charset.forName("UTF-8"));
        Assert.assertTrue(contents.contains("UAObjectTypeType opcMachineryComponentIdentificationType = {"));
        Assert.assertTrue(contents.contains("UAVariableTypeType opcProcessValueSetpointVariableTypeType = {"));
        Assert.assertTrue(contents.contains("UAEnumType opcJobExecutionModeType = {"));
    }

    /**
     * Parses and checks a synthetic external-reference case.
     *
     * @param order the required-model order suffix
     * @throws IOException shall not occur
     */
    private void assertExternalReferenceResolution(String order) throws IOException {
        String name = "ExternalReferenceResolution" + order;
        File in = new File("src/test/resources/NodeSets/Opc.Ua." + name + ".NodeSet2.xml");
        Assert.assertTrue(in.isFile());
        File out = new File("target/tmp", name + ".ivml");
        out.getParentFile().mkdirs();
        if (out.exists()) {
            Assert.assertTrue(out.delete());
        }

        DomParser.setDefaultVerbose(false);
        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.process(in, name, out, false);

        String contents = normalize(FileUtils.readFileToString(out, Charset.forName("UTF-8")));
        Assert.assertTrue(contents.contains("typeDefinition = refBy(opcCorrectTargetType)"));
        Assert.assertTrue(contents.contains("UAObjectTypeType opcCorrectTargetType = {"));
        Assert.assertFalse(contents.contains("opcWrongTargetType"));
    }

    /**
     * Tests resolving an external type definition for root variables.
     *
     * @throws IOException shall not occur
     */
    @Test
    public void testExternalRootVariableTypeDefinition() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.ExternalRootVariable.NodeSet2.xml");
        Assert.assertTrue(in.isFile());
        File tmp = new File("target/tmp");
        tmp.mkdirs();
        File out = new File(tmp, "OpcExternalRootVariable.ivml");
        if (out.exists()) {
            Assert.assertTrue(out.delete());
        }

        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.process(in, "ExternalRootVariable", out, false);

        Assert.assertTrue(out.isFile());
        String contents = FileUtils.readFileToString(out, Charset.forName("UTF-8"));
        Assert.assertTrue(contents.contains("UAVariableTypeType opcExternalMeasurementValueTypeType"));
        Assert.assertTrue(contents.contains("nodeId = {nameSpaceIndex = 2, identifier = 2001}"));
        Assert.assertTrue(contents.contains("UARootVariableType opcSyntheticRootTypePressure"));
        Assert.assertTrue(contents.contains("nodeId = {nameSpaceIndex = 1, identifier = 6001}"));
        Assert.assertTrue(contents.contains("typeDefinition = refBy(opcExternalMeasurementValueTypeType)"));
        Assert.assertTrue(contents.contains("rootParent = refBy(opcSyntheticRootType)"));
        Assert.assertTrue(contents.contains("optional = false,\n\t\ttype = refBy(FloatType)"));
        Assert.assertTrue(contents.contains("UARootVariableType opcSyntheticRootTypeTemperature"));
        Assert.assertTrue(contents.contains("nodeId = {nameSpaceIndex = 1, identifier = 6002}"));
        Assert.assertTrue(contents.contains("optional = true,\n\t\ttype = refBy(DoubleType)"));
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
        Assert.assertTrue(outContents.contains("UADataType opcGuidType = {"));
    }
    
    /**
     * Tests {@link DomParser} on the energy companion spec XML.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParserEnergy() throws IOException {
        File in = new File("src/test/resources/NodeSets/Opc.Ua.Machinery.Energy.NodeSet2.xml");
        Assert.assertTrue(in.exists());
        File tmp = new File("target/tmp");
        tmp.mkdirs();
        File out = new File(tmp, "OpcEnergy.ivml");
        DomParser.setDefaultVerbose(false);
        DomParser.setUsingIvmlFolder("target/tmp");
        DomParser.process(in, "Energy", out, false);

        Charset charset = Charset.forName("UTF-8");
        File expected = new File("src/test/resources/OpcEnergy.ivml");
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
        text = text.replace("\r\n", "\n");
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

    private static final String PARSER_PACKAGE = "de.iip_ecosphere.platform.configuration.easyProducer.opcua.parser";
    private static final String OWN_MODEL_URI = "http://example.org/Own/";
    private static final String RESULT_MODEL_URI = "http://opcfoundation.org/UA/Machinery/Result/";

    private static final List<String> CORE_ELEMENT_TYPES = Arrays.asList("ROOTOBJECT", "ROOTMETHOD", "SUBOBJECT",
        "SUBMETHOD", "FIELDOBJECT", "FIELDMETHOD");
    private static final List<String> OBJECT_ELEMENT_TYPES = Arrays.asList("ROOTOBJECT", "SUBOBJECT");
    private static final List<String> VARIABLE_ELEMENT_TYPES = Arrays.asList("FIELDVARIABLE", "ROOTVARIABLE");
    private static final String[] REFERENCE_TYPES = {"HasModellingRule", "HasTypeDefinition"};

    private static Document newDocument() throws ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    /**
     * Creates the root element of {@code doc}.
     *
     * @param doc the document
     * @param name the element name
     * @return the root element
     */
    private static Element newRoot(Document doc, String name) {
        Element root = doc.createElement(name);
        doc.appendChild(root);
        return root;
    }

    /**
     * Appends an element with a {@code DisplayName} child to {@code root}.
     *
     * @param doc the owning document
     * @param root the parent element
     * @param tag the element name
     * @param nodeId the NodeId attribute
     * @param displayName the display name
     * @return the created element
     */
    private static Element addTyped(Document doc, Element root, String tag, String nodeId, String displayName) {
        Element e = doc.createElement(tag);
        e.setAttribute("NodeId", nodeId);
        Element dn = doc.createElement("DisplayName");
        dn.setTextContent(displayName);
        e.appendChild(dn);
        root.appendChild(e);
        return e;
    }

    private static Element typedWithParent(Document doc, Element root, String tag, String nodeId,
        String displayName, String parentId, String dataType) {
        Element e = addTyped(doc, root, tag, nodeId, displayName);
        e.setAttribute("BrowseName", displayName);
        e.setAttribute("ParentNodeId", parentId);
        if (dataType != null) {
            e.setAttribute("DataType", dataType);
        }
        return e;
    }

    private static Element variableElement(Document doc, String dataType, String dimensions, String parentId) {
        Element e = doc.createElement("UAVariable");
        e.setAttribute("BrowseName", "Var");
        e.setAttribute("DataType", dataType);
        e.setAttribute("ArrayDimensions", dimensions);
        e.setAttribute("ParentNodeId", parentId);
        return e;
    }

    private static void addField(Document doc, Element definition, String name, String dataType) {
        Element f = doc.createElement("Field");
        f.setAttribute("Name", name);
        f.setAttribute("DataType", dataType);
        definition.appendChild(f);
    }

    /**
     * Creates a node set document holding a single {@code UADataType} with some irrelevant children.
     *
     * @param nodeId the NodeId of the data type
     * @param displayName the display name, may be <b>null</b> for none
     * @return the document
     * @throws ParserConfigurationException shall not occur
     */
    private static Document externDocument(String nodeId, String displayName) throws ParserConfigurationException {
        Document doc = newDocument();
        Element root = newRoot(doc, "UANodeSet");
        Element dt = doc.createElement("UADataType");
        dt.setAttribute("NodeId", nodeId);
        dt.setAttribute("BrowseName", "B");
        dt.appendChild(doc.createTextNode(" "));
        dt.appendChild(doc.createElement("References"));
        Element description = doc.createElement("Description");
        description.setTextContent("d");
        dt.appendChild(description);
        if (displayName != null) {
            Element dn = doc.createElement("DisplayName");
            dn.setTextContent(displayName);
            dt.appendChild(dn);
        }
        root.appendChild(dt);
        return doc;
    }

    private static NodeList orEmpty(NodeList nodes, NodeList empty) {
        return nodes != null ? nodes : empty;
    }

    /**
     * Creates a parser via the private constructor, {@code null} lists are replaced by empty ones.
     *
     * @param objectTypes the object types
     * @param objects the objects
     * @param variables the variables
     * @param methods the methods
     * @param dataTypes the data types
     * @return the parser
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    private static DomParser newParser(NodeList objectTypes, NodeList objects, NodeList variables,
        NodeList methods, NodeList dataTypes) throws ReflectiveOperationException, ParserConfigurationException {
        NodeList empty = createNodeList(0);
        Constructor<DomParser> ctor = DomParser.class.getDeclaredConstructor(NodeList.class, NodeList.class,
            NodeList.class, NodeList.class, NodeList.class, NodeList.class, NodeList.class, ArrayList.class);
        ctor.setAccessible(true);
        DomParser parser = ctor.newInstance(orEmpty(objectTypes, empty), orEmpty(objects, empty),
            orEmpty(variables, empty), orEmpty(methods, empty), orEmpty(dataTypes, empty), empty, empty,
            new ArrayList<BaseType>());
        parser.setExternAliasLists(new ArrayList<NodeList>());
        return parser;
    }

    private static DomParser newParser() throws ReflectiveOperationException, ParserConfigurationException {
        return newParser(null, null, null, null, null);
    }

    private static DomParser newParserWithDataTypes(NodeList dataTypes)
        throws ReflectiveOperationException, ParserConfigurationException {
        return newParser(null, null, null, null, dataTypes);
    }

    private static Field parserField(DomParser parser, String name) throws ReflectiveOperationException {
        Field field = DomParser.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<BaseType> getHierarchy(DomParser parser) throws ReflectiveOperationException {
        return (ArrayList<BaseType>) parserField(parser, "hierarchy").get(parser);
    }

    @SuppressWarnings("unchecked")
    private static void registerInHierarchy(DomParser parser, BaseType element) throws ReflectiveOperationException {
        getHierarchy(parser).add(element);
        ((Map<String, BaseType>) parserField(parser, "hierarchyByNodeId").get(parser))
            .put(element.getNodeId(), element);
    }

    /** Reads a private field of an arbitrary object. */
    private static Object fieldOf(Object target, String name) throws ReflectiveOperationException {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static Object call(Object target, String name, Class<?>[] types, Object... args)
        throws ReflectiveOperationException {
        Method m = DomParser.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private static Class<?> elementTypeClass() throws ClassNotFoundException {
        return Class.forName(PARSER_PACKAGE + ".ElementType");
    }

    private static Object enumConst(String name) throws ClassNotFoundException {
        for (Object c : elementTypeClass().getEnumConstants()) {
            if (c.toString().equals(name)) {
                return c;
            }
        }
        throw new IllegalArgumentException(name);
    }

    /** Invokes the private {@code createElement} with default values for the unused arguments. */
    private static void createElement(DomParser parser, String type, Element el, String id, String displayName,
        ArrayList<FieldType> subFields, ArrayList<FieldType> objectFields) throws ReflectiveOperationException {
        Class<?>[] sig = {elementTypeClass(), Element.class, String.class, String.class, String.class,
            String.class, ArrayList.class, ArrayList.class, ArrayList.class, ArrayList.class, String.class,
            boolean.class};
        call(parser, "createElement", sig, enumConst(type), el, id, displayName, "", "", subFields, objectFields,
            null, null, "BaseDataVariableType", false);
    }

    /** Invokes the private {@code retrieveAttributes}. */
    private static void retrieveAttributes(DomParser parser, Element el, Object type, ArrayList<FieldType> sub)
        throws ReflectiveOperationException {
        call(parser, "retrieveAttributes", new Class<?>[] {Element.class, ArrayList.class, elementTypeClass(),
            String.class}, el, sub, type, null);
    }

    private static void invokeAdaptDatatypes(DomParser parser, ObjectType object, MethodType method)
        throws ReflectiveOperationException {
        call(parser, "adaptDatatypesToModel", new Class<?>[] {ObjectType.class, MethodType.class}, object, method);
    }

    private static FieldObjectType fieldObject(String nodeId, String displayName) {
        return new FieldObjectType(nodeId, "FO", displayName, "", "", false);
    }

    private static FieldMethodType fieldMethod(String nodeId, String displayName) {
        return new FieldMethodType(nodeId, "FM", displayName, "", "", false);
    }

    private static FieldVariableType fieldVariable(String nodeId, String displayName, String dataType) {
        FieldVariableType result = new FieldVariableType(nodeId, "FV", displayName, "", dataType, "opcTypeType",
            false, "1", "-1", "");
        result.setDataType(dataType);
        return result;
    }

    /**
     * Tests {@code changeVariableDataTypes} for all simple mappings, the special values and already translated names.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testChangeVariableDataTypesMappings()
        throws ReflectiveOperationException, ParserConfigurationException {
        DomParser parser = newParser();
        Class<?>[] types = {String.class};
        String[][] mappings = {
            {"SByte", "SByteType"}, {"Boolean", "BooleanType"}, {"Byte", "ByteType"},
            {"ByteString", "ByteStringType"}, {"Integer", "IntegerType"}, {"Int16", "Integer16Type"},
            {"UInt16", "UnsignedInteger16Type"}, {"Int32", "Integer32Type"}, {"UInt32", "UnsignedInteger32Type"},
            {"Int64", "Integer64Type"}, {"UInt64", "UnsignedInteger64Type"}, {"Float", "FloatType"},
            {"Double", "DoubleType"}, {"String", "StringType"}, {"DateTime", "DateTimeType"},
            {"UInteger", "opcUnsignedIntegerType"}, {"", "opcUnknownDataType"},
            {"opcAlreadyTranslatedType", "opcAlreadyTranslatedType"}
        };
        for (String[] m : mappings) {
            Assert.assertEquals(m[0], m[1], call(parser, "changeVariableDataTypes", types, m[0]));
        }
    }

    /**
     * Tests that quotes and underscores are removed from internal data type names.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testInternDataTypeNameSanitizing() throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element root = newRoot(doc, "types");
        addTyped(doc, root, "UADataType", "ns=1;i=5", "\u201CFoo_Bar\u201D");
        DomParser parser = newParserWithDataTypes(root.getChildNodes());
        parser.setBaseNameSpace("1");
        Class<?>[] types = {String.class};
        Assert.assertEquals("opcFooBarType", call(parser, "changeVariableDataTypes", types, "ns=1;i=5"));
        Assert.assertEquals("opcType", call(parser, "changeVariableDataTypes", types, "ns=1;i=6"));
    }

    /**
     * Tests {@code checkForInternDataType} with non-element nodes, foreign ids and a missing display name.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testCheckForInternDataType() throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element root = newRoot(doc, "types");
        root.appendChild(doc.createTextNode(" "));
        addTyped(doc, root, "UADataType", "ns=1;i=4", "Other");

        Element match = doc.createElement("UADataType");
        match.setAttribute("NodeId", "ns=1;i=5");
        match.appendChild(doc.createTextNode(" "));
        match.appendChild(doc.createElement("References"));
        Element description = doc.createElement("Description");
        description.setTextContent("d");
        match.appendChild(description);
        Element displayName = doc.createElement("DisplayName");
        displayName.setTextContent("\u201CA_B\u201D");
        match.appendChild(displayName);
        root.appendChild(match);

        Element noName = doc.createElement("UADataType");
        noName.setAttribute("NodeId", "ns=1;i=7");
        noName.appendChild(doc.createElement("References"));
        root.appendChild(noName);

        DomParser parser = newParserWithDataTypes(root.getChildNodes());
        Class<?>[] sig = {String.class};
        Assert.assertEquals("AB", call(parser, "checkForInternDataType", sig, "ns=1;i=5"));
        Assert.assertEquals("", call(parser, "checkForInternDataType", sig, "ns=1;i=7"));
        Assert.assertEquals("", call(parser, "checkForInternDataType", sig, "ns=1;i=404"));
    }

    /**
     * Tests {@code ROOTVARIABLE} with an {@code EnumValueType} whose data type element is missing or present.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRootVariableEnumValueType() throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element root = newRoot(doc, "types");
        addTyped(doc, root, "UADataType", "ns=1;i=77", "MyEnum");

        Element missing = variableElement(doc, "EnumValueType", "", "ns=1;i=999");
        DomParser parser = newParserWithDataTypes(root.getChildNodes());
        createElement(parser, "ROOTVARIABLE", missing, "ns=1;i=1", "Missing", null, new ArrayList<FieldType>());
        Assert.assertEquals(1, getHierarchy(parser).size());
        Assert.assertTrue(getHierarchy(parser).get(0).toString().contains("opcEnumValueTypeType"));

        Element present = variableElement(doc, "EnumValueType", "", "ns=1;i=77");
        parser = newParserWithDataTypes(root.getChildNodes());
        createElement(parser, "ROOTVARIABLE", present, "ns=1;i=2", "Present", null, new ArrayList<FieldType>());
        Assert.assertTrue(getHierarchy(parser).get(0).toString().contains("MyEnum"));
    }

    /**
     * Tests that only the first array dimension of a {@code ROOTVARIABLE} is kept.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRootVariableArrayDimensions() throws ReflectiveOperationException, ParserConfigurationException {
        DomParser parser = newParser();
        Element var = variableElement(newDocument(), "Double", "5,7", "ns=1;i=999");
        createElement(parser, "ROOTVARIABLE", var, "ns=1;i=10", "Dim", null, new ArrayList<FieldType>());
        Assert.assertEquals(1, getHierarchy(parser).size());
        Assert.assertFalse(getHierarchy(parser).get(0).toString().contains("5,7"));
    }

    /**
     * Tests {@code FIELDVARIABLE} with a plain type, repeated creation and an {@code EnumValueType}.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testFieldVariable() throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        DomParser parser = newParser();
        ArrayList<FieldType> sub = new ArrayList<>();
        Element var = variableElement(doc, "Double", "5,7", "ns=1;i=999");
        createElement(parser, "FIELDVARIABLE", var, "ns=1;i=20", "Field", sub, new ArrayList<FieldType>());
        Assert.assertEquals(1, sub.size());
        Assert.assertEquals("DoubleType", sub.get(0).getDataType());
        // same variable name again: not added twice
        createElement(parser, "FIELDVARIABLE", var, "ns=1;i=20", "Field", sub, new ArrayList<FieldType>());
        Assert.assertEquals(1, sub.size());

        Element root = newRoot(newDocument(), "types");
        addTyped(root.getOwnerDocument(), root, "UADataType", "ns=1;i=77", "MyEnum");
        parser = newParserWithDataTypes(root.getChildNodes());
        sub = new ArrayList<>();
        Element enumVar = variableElement(doc, "EnumValueType", "", "ns=1;i=77");
        createElement(parser, "FIELDVARIABLE", enumVar, "ns=1;i=21", "EnumField", sub, new ArrayList<FieldType>());
        Assert.assertEquals(1, sub.size());
        Assert.assertEquals("MyEnum", sub.get(0).getDataType());
    }

    /**
     * Tests {@code ROOTMETHOD} without and with fields.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRootMethod() throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element method = doc.createElement("UAMethod");
        method.setAttribute("BrowseName", "Method");
        method.setAttribute("ParentNodeId", "ns=1;i=999");

        DomParser parser = newParser();
        createElement(parser, "ROOTMETHOD", method, "ns=1;i=30", "Method", null, new ArrayList<FieldType>());
        Assert.assertEquals(1, getHierarchy(parser).size());

        parser = newParser();
        ArrayList<FieldType> fields = new ArrayList<>();
        fields.add(fieldVariable("ns=1;i=31", "Arg", "Int32"));
        createElement(parser, "ROOTMETHOD", method, "ns=1;i=32", "MethodWithArg", null, fields);
        Assert.assertEquals(1, getHierarchy(parser).size());
    }

    /**
     * Tests the field name normalization of data type definitions.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testDataTypeFieldNameNormalization()
        throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element dt = doc.createElement("UADataType");
        dt.setAttribute("NodeId", "ns=1;i=50");
        dt.setAttribute("BrowseName", "Struct");
        Element dn = doc.createElement("DisplayName");
        dn.setTextContent("Struct");
        dt.appendChild(dn);
        Element def = doc.createElement("Definition");
        def.setAttribute("Name", "Struct");
        dt.appendChild(def);
        addField(doc, def, "", "Int32");
        addField(doc, def, "\u00B5A/m\u00B2\u00B3\u00B0", "Double");

        DomParser parser = newParser();
        retrieveAttributes(parser, dt, enumConst("DATATYPE"), null);

        String out = getHierarchy(parser).get(0).toString();
        Assert.assertTrue(out.contains("placeholder_Struct"));
        Assert.assertTrue(out.contains("_muA_per_m_toPowerOf2_toPowerOf3degree_"));
    }

    /**
     * Tests that a modelling rule reference to "Optional" sets the optional flag.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRetrieveAttributesOptionalModellingRule()
        throws ReflectiveOperationException, ParserConfigurationException {
        Document core = newDocument();
        addTyped(core, newRoot(core, "UANodeSet"), "UAObject", "i=80", "Optional");

        Document doc = newDocument();
        Element el = doc.createElement("UAObject");
        el.setAttribute("NodeId", "ns=1;i=61");
        el.setAttribute("BrowseName", "Opt");
        Element dn = doc.createElement("DisplayName");
        dn.setTextContent("Opt");
        el.appendChild(dn);
        Element refs = doc.createElement("References");
        Element ref = doc.createElement("Reference");
        ref.setAttribute("ReferenceType", "HasModellingRule");
        ref.setTextContent("i=80");
        refs.appendChild(ref);
        el.appendChild(refs);

        DomParser parser = newParser();
        parser.setDocuments(new Document[] {core});
        retrieveAttributes(parser, el, enumConst("ROOTOBJECT"), null);

        Assert.assertEquals(1, getHierarchy(parser).size());
        Assert.assertTrue(getHierarchy(parser).get(0).toString().contains("optional = true"));
    }

    /**
     * Tests an empty {@code References} child for every element type.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testRetrieveAttributesEmptyReferences() throws Exception {
        Document doc = newDocument();
        for (Object type : elementTypeClass().getEnumConstants()) {
            Element el = doc.createElement("UAObject");
            el.setAttribute("NodeId", "ns=1;i=60");
            el.setAttribute("BrowseName", "X");
            Element dn = doc.createElement("DisplayName");
            dn.setTextContent("X" + type);
            el.appendChild(dn);
            el.appendChild(doc.createElement("References"));

            // sub objects/methods take their variable name from a field of an already known parent
            DomParser parser = newParser();
            FieldObjectType known = fieldObject("ns=1;i=60", "K");
            known.setVarName("opcK");
            known.setDataType("opcKnownTarget");
            ArrayList<FieldType> knownFields = new ArrayList<>();
            knownFields.add(known);
            ObjectType knownParent = new ObjectType("ns=1;i=59", "P", "P", "", false, "opcPType", knownFields);
            knownParent.setVarName("opcP");
            registerInHierarchy(parser, knownParent);

            retrieveAttributes(parser, el, type, new ArrayList<FieldType>());
        }
    }

    /**
     * Tests the object branch if the targets are not yet in the hierarchy: object and method fields get a predicted
     * name, variable fields stay untouched.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testAdaptDatatypesObjectPredictedNames()
        throws ReflectiveOperationException, ParserConfigurationException {
        FieldObjectType fo = fieldObject("ns=1;i=100", "FieldObj");
        FieldMethodType fm = fieldMethod("ns=1;i=101", "FieldMeth");
        FieldVariableType fv = fieldVariable("ns=1;i=102", "FieldVar", "Double");
        ArrayList<FieldType> fields = new ArrayList<>();
        fields.add(fo);
        fields.add(fm);
        fields.add(fv);
        ObjectType parent = new ObjectType("ns=1;i=1", "Parent", "Parent", "", false, "opcParentType", fields);
        parent.setVarName("opcParent");

        invokeAdaptDatatypes(newParser(), parent, null);

        Assert.assertEquals(BaseType.validateVarName("opcParentFieldObj"), fo.getDataType());
        Assert.assertEquals(BaseType.validateVarName("opcParentFieldMeth"), fm.getDataType());
        Assert.assertEquals("Double", fv.getDataType());
    }

    /**
     * Tests the method branch if the targets are not yet in the hierarchy.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testAdaptDatatypesMethodPredictedNames()
        throws ReflectiveOperationException, ParserConfigurationException {
        FieldObjectType fo = fieldObject("ns=1;i=200", "ArgObj");
        FieldMethodType fm = fieldMethod("ns=1;i=201", "ArgMeth");
        FieldVariableType fv = fieldVariable("ns=1;i=202", "ArgVar", "Int32");
        ArrayList<FieldType> fields = new ArrayList<>();
        fields.add(fo);
        fields.add(fm);
        fields.add(fv);
        MethodType method = new MethodType("ns=1;i=2", "Method", "Method", "", false, fields);
        method.setVarName("opcMethod");

        invokeAdaptDatatypes(newParser(), null, method);

        Assert.assertEquals(BaseType.validateVarName("opcMethodArgObj"), fo.getDataType());
        Assert.assertEquals(BaseType.validateVarName("opcMethodArgMeth"), fm.getDataType());
        Assert.assertEquals("Int32", fv.getDataType());
    }

    /**
     * Tests that targets already contained in the hierarchy keep their canonical name, in the object and in the
     * method branch.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testAdaptDatatypesReusesCanonicalName()
        throws ReflectiveOperationException, ParserConfigurationException {
        DomParser parser = newParser();
        ObjectType sharedObject = new ObjectType("ns=1;i=300", "SharedObj", "SharedObj", "", false,
            "opcSharedType", new ArrayList<FieldType>());
        sharedObject.setVarName("opcCanonicalObject");
        registerInHierarchy(parser, sharedObject);
        MethodType sharedMethod = new MethodType("ns=1;i=301", "SharedMeth", "SharedMeth", "", false,
            new ArrayList<FieldType>());
        sharedMethod.setVarName("opcCanonicalMethod");
        registerInHierarchy(parser, sharedMethod);

        FieldObjectType foInObject = fieldObject("ns=1;i=300", "SharedObj");
        FieldMethodType fmInObject = fieldMethod("ns=1;i=301", "SharedMeth");
        ArrayList<FieldType> objectFields = new ArrayList<>();
        objectFields.add(foInObject);
        objectFields.add(fmInObject);
        ObjectType parent = new ObjectType("ns=1;i=3", "Parent", "Parent", "", false, "opcParentType", objectFields);
        parent.setVarName("opcParent");
        invokeAdaptDatatypes(parser, parent, null);
        Assert.assertEquals("opcCanonicalObject", foInObject.getDataType());
        Assert.assertEquals("opcCanonicalMethod", fmInObject.getDataType());

        FieldObjectType foInMethod = fieldObject("ns=1;i=300", "SharedObj");
        FieldMethodType fmInMethod = fieldMethod("ns=1;i=301", "SharedMeth");
        ArrayList<FieldType> methodFields = new ArrayList<>();
        methodFields.add(foInMethod);
        methodFields.add(fmInMethod);
        MethodType method = new MethodType("ns=1;i=4", "Method", "Method", "", false, methodFields);
        method.setVarName("opcMethod");
        invokeAdaptDatatypes(parser, null, method);
        Assert.assertEquals("opcCanonicalObject", foInMethod.getDataType());
        Assert.assertEquals("opcCanonicalMethod", fmInMethod.getDataType());
    }

    /**
     * Tests {@code checkRedundancy} for field lists and {@code checkRelation} with a {@code null} node list.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRedundancyAndRelationNullList() throws ReflectiveOperationException, ParserConfigurationException {
        DomParser parser = newParser();
        FieldObjectType f = fieldObject("ns=1;i=1", "F");
        f.setVarName("opcF");
        ArrayList<FieldType> list = new ArrayList<>();
        list.add(f);
        Class<?>[] types = {String.class, ArrayList.class};
        Assert.assertEquals(Boolean.TRUE, call(parser, "checkRedundancy", types, "opcF", list));
        Assert.assertEquals(Boolean.FALSE, call(parser, "checkRedundancy", types, "opcOther", list));
        Assert.assertEquals(Boolean.FALSE, call(parser, "checkRedundancy", types, "opcF", null));
        Assert.assertNull(call(null, "checkRelation", new Class<?>[] {String.class, NodeList.class}, "x", null));
    }

    /**
     * Tests that {@code println} prints only in verbose mode.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testPrintlnVerbose() throws ReflectiveOperationException, ParserConfigurationException {
        DomParser parser = newParser();
        Field verbose = parserField(parser, "verbose");
        PrintStream previous = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer));
            verbose.setBoolean(parser, true);
            call(parser, "println", new Class<?>[] {String.class}, "visible");
            verbose.setBoolean(parser, false);
            call(parser, "println", new Class<?>[] {String.class}, "hidden");
        } finally {
            System.setOut(previous);
        }
        String out = buffer.toString();
        Assert.assertTrue(out.contains("visible"));
        Assert.assertFalse(out.contains("hidden"));
    }

    /**
     * Tests the error cases of the required model and namespace index lookup.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testRequiredModelAndNamespaceIndexErrors()
        throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element model = doc.createElement("Model");
        model.setAttribute("ModelUri", "http://example.org/A/");
        doc.appendChild(model);
        DomParser parser = newParser();
        parser.setDocuments(new Document[] {doc});
        Class<?>[] modelSig = {String.class};
        Assert.assertSame(doc, call(parser, "getRequiredModel", modelSig, "http://example.org/A/"));
        try {
            call(parser, "getRequiredModel", modelSig, "http://example.org/B/");
            Assert.fail("Expected missing model to be rejected");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }

        Document other = newDocument();
        Element uris = newRoot(other, "NamespaceUris");
        Element uri = other.createElement("Uri");
        uri.setTextContent("http://example.org/A/");
        uris.appendChild(uri);
        Class<?>[] indexSig = {Document.class, String.class};
        Assert.assertEquals(1, call(null, "getNamespaceIndex", indexSig, other, "http://example.org/A/"));
        try {
            call(null, "getNamespaceIndex", indexSig, other, "http://example.org/B/");
            Assert.fail("Expected missing namespace to be rejected");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    /**
     * Creates a model folder holding the core model in {@code RequiredModels} and the Machinery Result model in the
     * main folder.
     *
     * @param folder the folder name below {@code target/tmp}
     * @return the main folder
     * @throws IOException shall not occur
     */
    private static File prepareModelFolder(String folder) throws IOException {
        File base = new File("target/tmp", folder);
        FileUtils.deleteQuietly(base);
        File required = new File(base, "RequiredModels");
        Assert.assertTrue(required.mkdirs());
        Assert.assertTrue(new File(required, "Opc.Ua.NodeSet2.xml").createNewFile());
        Assert.assertTrue(new File(base, "Opc.Ua.Machinery_Result.NodeSet2.xml").createNewFile());
        return base;
    }

    private static File[] invokeCheckRequiredModels(File base, NodeList namespaceUris)
        throws ReflectiveOperationException, ParserConfigurationException {
        return (File[]) call(null, "checkRequiredModels", new Class<?>[] {DomParser.class, String.class,
            String.class, String.class, NodeList.class}, newParser(), OWN_MODEL_URI, base.getPath(), "x",
            namespaceUris);
    }

    /**
     * Runs {@code checkRequiredModels} with the given console input and expects the prompt to run out of input.
     *
     * @param base the model folder
     * @param namespaceUris the namespace URIs
     * @param input the console input
     * @return the console output
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    private static String runRequiredModelsPrompt(File base, NodeList namespaceUris, String input)
        throws ReflectiveOperationException, ParserConfigurationException {
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer));
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            try {
                invokeCheckRequiredModels(base, namespaceUris);
                Assert.fail("Expected the prompt to run out of input");
            } catch (InvocationTargetException e) {
                Assert.assertTrue(e.getCause() instanceof NoSuchElementException);
            }
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
        }
        return buffer.toString();
    }

    /**
     * Tests finding required models in both folders, including non-element nodes and non-{@code Uri} children in the
     * namespace list.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testCheckRequiredModelsFound() throws Exception {
        Document doc = newDocument();
        Element uris = newRoot(doc, "NamespaceUris");
        Element uri = doc.createElement("Uri");
        uri.setTextContent(RESULT_MODEL_URI);
        uris.appendChild(uri);
        File base = prepareModelFolder("requiredModelsFound");
        Assert.assertEquals(2, invokeCheckRequiredModels(base, doc.getElementsByTagName("NamespaceUris")).length);

        Document noisy = newDocument();
        Element container = newRoot(noisy, "container");
        container.appendChild(noisy.createTextNode("  "));
        Element noisyUris = noisy.createElement("NamespaceUris");
        container.appendChild(noisyUris);
        noisyUris.appendChild(noisy.createTextNode("  "));
        noisyUris.appendChild(noisy.createElement("Other"));
        Element noisyUri = noisy.createElement("Uri");
        noisyUri.setTextContent(RESULT_MODEL_URI);
        noisyUris.appendChild(noisyUri);
        base = prepareModelFolder("requiredModelsSkip");
        Assert.assertEquals(2, invokeCheckRequiredModels(base, container.getChildNodes()).length);
    }

    /**
     * Tests a missing and an incomplete model folder together with the console prompt.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testCheckRequiredModelsMissingAndPrompt() throws Exception {
        File base = new File("target/tmp/requiredModelsMissing");
        FileUtils.deleteQuietly(base);
        Assert.assertTrue(base.mkdirs());
        Document doc = newDocument();
        newRoot(doc, "NamespaceUris");
        NodeList uris = doc.getElementsByTagName("NamespaceUris");

        // RequiredModels does not exist: it is created, stays empty, then the input is exhausted
        runRequiredModelsPrompt(base, uris, "y\n");
        Assert.assertTrue(new File(base, "RequiredModels").isDirectory());

        // folder is not empty but the core model is missing; input other than "y" is ignored
        Assert.assertTrue(new File(base, "RequiredModels/Other.xml").createNewFile());
        runRequiredModelsPrompt(base, uris, "n\ny\n");
    }

    /**
     * Tests that a model folder which cannot be created is reported.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testCheckRequiredModelsDirectoryCannotBeCreated() throws Exception {
        FileUtils.deleteQuietly(new File("target/tmp/noSuchParent"));
        Document doc = newDocument();
        newRoot(doc, "NamespaceUris");
        String output = runRequiredModelsPrompt(new File("target/tmp/noSuchParent/deeper"),
            doc.getElementsByTagName("NamespaceUris"), "y\n");
        Assert.assertTrue(output.contains("can't be created"));
    }

    /**
     * Tests {@code parseFile} with text nodes between the elements and elements with an unknown parent.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testParseFileRootElementsAndNonElementNodes()
        throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element root = newRoot(doc, "root");
        Element ots = doc.createElement("ots");
        Element objs = doc.createElement("objs");
        Element vars = doc.createElement("vars");
        Element meths = doc.createElement("meths");
        for (Element e : new Element[] {ots, objs, vars, meths}) {
            root.appendChild(e);
            e.appendChild(doc.createTextNode(" "));
        }
        typedWithParent(doc, ots, "UAObjectType", "ns=1;i=1", "Type", "", null);
        typedWithParent(doc, objs, "UAObject", "ns=1;i=2", "Obj", "ns=1;i=1", null);
        typedWithParent(doc, vars, "UAVariable", "ns=1;i=3", "Var", "ns=1;i=1", "Double");
        typedWithParent(doc, meths, "UAMethod", "ns=1;i=4", "Meth", "ns=1;i=1", null);
        typedWithParent(doc, objs, "UAObject", "ns=1;i=5", "Orphan", "ns=1;i=999", null);
        typedWithParent(doc, meths, "UAMethod", "ns=1;i=6", "OrphanMeth", "ns=1;i=999", null);

        DomParser parser = newParser(ots.getChildNodes(), objs.getChildNodes(), vars.getChildNodes(),
            meths.getChildNodes(), null);
        call(parser, "parseFile", new Class<?>[] {});

        Assert.assertEquals(4, getHierarchy(parser).size());
    }

    /**
     * Tests {@code parseFile} for no, only data and only object types.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testParseFileElementTypeCondition() throws ReflectiveOperationException, ParserConfigurationException {
        Document doc = newDocument();
        Element root = newRoot(doc, "root");

        DomParser none = newParser();
        call(none, "parseFile", new Class<?>[] {});
        Assert.assertTrue(getHierarchy(none).isEmpty());

        Element dts = doc.createElement("dts");
        root.appendChild(dts);
        addTyped(doc, dts, "UADataType", "ns=1;i=10", "OnlyData");
        DomParser dataOnly = newParserWithDataTypes(dts.getChildNodes());
        call(dataOnly, "parseFile", new Class<?>[] {});
        Assert.assertEquals(1, getHierarchy(dataOnly).size());

        Element ots = doc.createElement("ots");
        root.appendChild(ots);
        addTyped(doc, ots, "UAObjectType", "ns=1;i=11", "OnlyType");
        DomParser typeOnly = newParser(ots.getChildNodes(), null, null, null, null);
        call(typeOnly, "parseFile", new Class<?>[] {});
        Assert.assertEquals(1, getHierarchy(typeOnly).size());
    }

    private static DomParser newParserWithCoreDocument()
        throws ReflectiveOperationException, ParserConfigurationException {
        Document core = newDocument();
        Element coreRoot = newRoot(core, "UANodeSet");
        addTyped(core, coreRoot, "UAObject", "i=1", "Rule");
        addTyped(core, coreRoot, "UAObjectType", "i=2", "OT");
        addTyped(core, coreRoot, "UAVariableType", "i=3", "VT");
        DomParser parser = newParser();
        parser.setBaseNameSpace("1");
        parser.setDocuments(new Document[] {core});
        return parser;
    }

    private static Object resolveTypes(DomParser parser, String nodeId, String reference, Object type)
        throws ReflectiveOperationException {
        return call(parser, "getTypeListAndTypeRootNs", new Class<?>[] {String.class, String.class,
            elementTypeClass()}, nodeId, reference, type);
    }

    private static void assertResolved(Object result, boolean hasList, String expectedType)
        throws ReflectiveOperationException {
        Assert.assertEquals(hasList, fieldOf(result, "typeList") != null);
        Assert.assertEquals(expectedType, fieldOf(result, "type").toString());
    }

    /**
     * Tests type resolution for references into the namespace of the companion specification.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testTypeListAndTypeRootNsOwnNamespace() throws Exception {
        DomParser parser = newParserWithCoreDocument();
        for (Object type : elementTypeClass().getEnumConstants()) {
            String name = type.toString();
            for (String reference : REFERENCE_TYPES) {
                Object result = resolveTypes(parser, "ns=1;i=5", reference, type);
                if (OBJECT_ELEMENT_TYPES.contains(name)) {
                    assertResolved(result, true, name);
                } else if (VARIABLE_ELEMENT_TYPES.contains(name)) {
                    assertResolved(result, true, "VARIABLETYPE");
                } else {
                    assertResolved(result, false, name);
                }
            }
        }
    }

    /**
     * Tests type resolution for references into the core namespace.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testTypeListAndTypeRootNsCoreNamespace() throws Exception {
        DomParser parser = newParserWithCoreDocument();
        for (Object type : elementTypeClass().getEnumConstants()) {
            String name = type.toString();
            for (String reference : REFERENCE_TYPES) {
                boolean rule = reference.equals("HasModellingRule");
                Object result = resolveTypes(parser, "i=5", reference, type);
                NodeList list = (NodeList) fieldOf(result, "typeList");
                if (CORE_ELEMENT_TYPES.contains(name)) {
                    Assert.assertEquals(rule ? "UAObject" : "UAObjectType", list.item(0).getNodeName());
                    Assert.assertEquals(rule ? name : "OBJECTTYPE", fieldOf(result, "type").toString());
                } else if (VARIABLE_ELEMENT_TYPES.contains(name)) {
                    Assert.assertEquals(rule ? "UAObject" : "UAVariableType", list.item(0).getNodeName());
                    Assert.assertEquals(rule ? "ROOTOBJECT" : "VARIABLETYPE", fieldOf(result, "type").toString());
                } else {
                    assertResolved(result, false, name);
                }
            }
        }
    }

    /**
     * Tests that references into a foreign namespace are not resolved.
     *
     * @throws Exception shall not occur
     */
    @Test
    public void testTypeListAndTypeRootNsForeignNamespace() throws Exception {
        DomParser parser = newParserWithCoreDocument();
        for (Object type : elementTypeClass().getEnumConstants()) {
            for (String reference : REFERENCE_TYPES) {
                assertResolved(resolveTypes(parser, "ns=2;i=5", reference, type), false, type.toString());
            }
        }
    }

    private static String resolveExtern(DomParser parser, String nodeId) throws ReflectiveOperationException {
        return (String) call(parser, "retrieveAttributesForExternDataType", new Class<?>[] {String.class}, nodeId);
    }

    /**
     * Tests resolving an external data type whose namespace index is rewritten, preceded by a document without a
     * match.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testExternDataTypeNamespaceRewrite() throws ReflectiveOperationException,
        ParserConfigurationException {
        DomParser parser = newParser();
        parser.setDocuments(new Document[] {externDocument("ns=1;i=999", "Nope"),
            externDocument("ns=1;i=100", "Ext")});
        Assert.assertEquals("Ext", resolveExtern(parser, "ns=2;i=100"));
        Assert.assertEquals(1, getHierarchy(parser).size());
        Assert.assertEquals("ns=2;i=100", getHierarchy(parser).get(0).getNodeId());
        Assert.assertTrue(((Set<?>) fieldOf(parser, "externalDataTypesInProgress")).isEmpty());
    }

    /**
     * Tests resolving a core data type, i.e., without namespace index.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testExternDataTypeCoreId() throws ReflectiveOperationException, ParserConfigurationException {
        DomParser parser = newParser();
        parser.setDocuments(new Document[] {externDocument("i=100", "Core")});
        Assert.assertEquals("Core", resolveExtern(parser, "i=100"));
        Assert.assertEquals(1, getHierarchy(parser).size());
    }

    /**
     * Tests that a type which is already being resolved (recursive structure) is not expanded again.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testExternDataTypeRecursionGuard() throws ReflectiveOperationException,
        ParserConfigurationException {
        DomParser parser = newParser();
        parser.setDocuments(new Document[] {externDocument("ns=1;i=100", "Ext")});
        Set<String> inProgress = (Set<String>) fieldOf(parser, "externalDataTypesInProgress");
        inProgress.add("ns=2;i=100");
        Assert.assertEquals("Ext", resolveExtern(parser, "ns=2;i=100"));
        Assert.assertTrue(getHierarchy(parser).isEmpty());
        Assert.assertTrue(inProgress.contains("ns=2;i=100"));
    }

    /**
     * Tests an external data type without display name and an unknown external data type.
     *
     * @throws ReflectiveOperationException shall not occur
     * @throws ParserConfigurationException shall not occur
     */
    @Test
    public void testExternDataTypeNameMissingOrUnknown() throws ReflectiveOperationException,
        ParserConfigurationException {
        DomParser parser = newParser();
        parser.setDocuments(new Document[] {externDocument("i=200", null)});
        Assert.assertEquals("", resolveExtern(parser, "i=200"));

        parser = newParser();
        parser.setDocuments(new Document[] {externDocument("ns=1;i=1", "X")});
        Assert.assertEquals("", resolveExtern(parser, "ns=2;i=555"));
        Assert.assertTrue(getHierarchy(parser).isEmpty());
    }    
}
