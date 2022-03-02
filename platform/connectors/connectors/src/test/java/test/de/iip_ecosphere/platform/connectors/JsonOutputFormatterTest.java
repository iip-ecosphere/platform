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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.jsoniter.any.Any;

import de.iip_ecosphere.platform.connectors.formatter.ConsumerWithException;
import de.iip_ecosphere.platform.connectors.formatter.JsonOutputFormatter;
import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter.OutputConverter;
import de.iip_ecosphere.platform.connectors.parser.InputParser.InputConverter;
import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;
import de.iip_ecosphere.platform.connectors.parser.JsonInputParser;

/**
 * Tests {@link JsonOutputFormatter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonOutputFormatterTest {
    
    /**
     * Tests the output formatter.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testFormatter() throws IOException {
        JsonOutputFormatter formatter = new JsonOutputFormatter();
        OutputConverter<ConsumerWithException<JsonGenerator>> fConv = formatter.getConverter();
        formatter.add("field", fConv.fromInteger(10));
        formatter.add("nest.name", fConv.fromString("abba"));
        formatter.add("nest.value", fConv.fromDouble(1.234));
        formatter.add("fieldX", fConv.fromInteger(20));
        byte[] chunk = formatter.chunkCompleted();
        String tmp = new String(chunk);
        System.out.println("OUT " + tmp);
        
        JsonInputParser parser = new JsonInputParser();
        InputConverter<Any> pConv = parser.getConverter();
        ParseResult<Any> pr = parser.parse(chunk);
        Assert.assertEquals(10, pConv.toInteger(pr.getData("field", 0)));
        Assert.assertEquals("abba", pConv.toString(pr.getData("nest.name")));
        Assert.assertEquals(1.234, pConv.toDouble(pr.getData("nest.value", 0)), 0.01);
        Assert.assertEquals(1.234, pConv.toDouble(pr.getData("", 1, 1)), 0.01);
        Assert.assertEquals(20, pConv.toInteger(pr.getData("fieldX", 0)));
    }

}
