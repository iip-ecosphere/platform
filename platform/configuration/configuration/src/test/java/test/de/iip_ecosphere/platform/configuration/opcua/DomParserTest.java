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

package test.de.iip_ecosphere.platform.configuration.opcua;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.opcua.parser.DomParser;

/**
 * Tests {@link DomParser}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DomParserTest {
    
    /**
     * Tests {@link DomParser}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDomParser() throws IOException {
        File in = new File("src/main/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
        Assert.assertTrue(in.exists());
        File tmp = new File("target/tmp");
        tmp.mkdirs();
        File out = new File(tmp, "OpcMachineTool.ivml");
        // implicit from in to out
        DomParser.setDefaultVerbose(false); // reduce output
        DomParser.main(new String[0]);
        DomParser.main(new String[] {in.toString()});
        DomParser.process(in, "MachineTool", out, false);
        
        Charset charset = Charset.forName("UTF-8");
        File expected = new File("src/test/resources/OpcMachineTool.ivml");
        String exContents = normalize(FileUtils.readFileToString(expected, charset));
        String outContents = normalize(FileUtils.readFileToString(out, charset));
        /*for (int i = 0; i < Math.min(exContents.length(), outContents.length()); i++) {
            if (exContents.charAt(i) != outContents.charAt(i)) {
                System.out.println(((int)exContents.charAt(i))+" "+((int)outContents.charAt(i)));
            }
        }*/
        Assert.assertEquals(exContents, outContents);
    }

    /**
     * Normalizes unicode/UTF-8 strings for comparison (heuristics).
     * 
     * @param text the text to be normalized
     * @return the normalized text
     */
    private String normalize(String text) {
        StringBuilder tmp = new StringBuilder(text);
        for (int i = 0; i < tmp.length(); i++) {
            int c = (int) tmp.charAt(i);
            if (c == 8211 || c == 65533) {
                tmp.setCharAt(i, '-');
            } else if (c == 8804) {
                tmp.setCharAt(i, (char) 63);
            } else if (c == 8217 || c == 8222 || c == 8220) {
                tmp.setCharAt(i, (char) 45);
            }
        }
        return tmp.toString();
    }

}
