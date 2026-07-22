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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.easyProducer.opcua.parser.DomParser;
import de.iip_ecosphere.platform.support.FileUtils;

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
     * Tests processing explicit inputs exactly once in caller order.
     */
    @Test
    public void testDomParserMultipleInputs() {
        File first = new File("src/test/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
        File second = new File("src/test/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
        File firstOut = new File("target/gen/OpcWoodworking.ivml");
        File secondOut = new File("target/gen/OpcMachineTool.ivml");
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
