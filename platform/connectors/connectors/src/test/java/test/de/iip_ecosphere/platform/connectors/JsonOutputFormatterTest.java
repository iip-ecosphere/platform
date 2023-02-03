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
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.formatter.JsonOutputFormatter;
import de.iip_ecosphere.platform.connectors.formatter.JsonOutputFormatter.JsonOutputConverter;
import de.iip_ecosphere.platform.connectors.parser.JsonInputParser;
import de.iip_ecosphere.platform.connectors.parser.JsonInputParser.JsonInputConverter;
import de.iip_ecosphere.platform.connectors.parser.JsonInputParser.JsonParseResult;

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
        final String iec61131u3DateTime = "'DT#'yyyy-MM-dd-HH:mm:ss.SS"; 
        JsonOutputFormatter formatter = new JsonOutputFormatter();
        JsonOutputConverter fConv = formatter.getConverter();
        formatter.add("field", fConv.fromInteger(10));
        formatter.add("nest.name", fConv.fromString("abba"));
        formatter.add("nest.value", fConv.fromDouble(1.234));
        formatter.add("fieldX", fConv.fromInteger(20));
        formatter.add("enum", fConv.fromEnum(MyEnum.TEST2));
        formatter.add("enumName", fConv.fromEnumAsName(MyEnum.TEST1));
        Date now = Calendar.getInstance().getTime();
        formatter.add("time", fConv.fromDate(now, iec61131u3DateTime));
        byte[] chunk = formatter.chunkCompleted();
        String tmp = new String(chunk);
        System.out.println("OUT " + tmp);
        
        JsonInputParser parser = new JsonInputParser();
        JsonInputConverter pConv = parser.getConverter();
        JsonParseResult pr = parser.parse(chunk);
        Assert.assertEquals(10, pConv.toInteger(pr.getData("field", 0)));
        Assert.assertEquals("abba", pConv.toString(pr.getData("nest.name")));
        Assert.assertEquals(1.234, pConv.toDouble(pr.getData("nest.value", 0)), 0.01);
        Assert.assertEquals(1.234, pConv.toDouble(pr.getData("", 1, 1)), 0.01);
        Assert.assertEquals(20, pConv.toInteger(pr.getData("fieldX", 0)));
        Assert.assertEquals(now, pConv.toDate(pr.getData("time", 0), iec61131u3DateTime));
        Assert.assertEquals(MyEnum.TEST2, pConv.toEnum(pr.getData("enum", 0), MyEnum.class));
        Assert.assertEquals(MyEnum.TEST1, pConv.toEnum(pr.getData("enumName", 0), MyEnum.class));
    }

    /**
     * Tests the output formatter on objects.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testFormatter4Object() throws IOException {
        JsonOutputFormatter formatter = new JsonOutputFormatter();
        JsonOutputConverter fConv = formatter.getConverter();
        
        formatter.startObjectStructure("obj");
        formatter.add("iValue", fConv.fromInteger(-1));
        formatter.add("sValue", fConv.fromString("abba"));
        formatter.endStructure();
        
        formatter.startArrayStructure("arr");
        formatter.startObjectStructure(null);
        formatter.add("iValue", fConv.fromInteger(1));
        formatter.add("sValue", fConv.fromString("bap"));
        formatter.endStructure();
        formatter.endStructure();

        byte[] chunk = formatter.chunkCompleted();
        String tmp = new String(chunk);
        System.out.println("OUT " + tmp);

        JsonInputParser parser = new JsonInputParser();
        JsonInputConverter pConv = parser.getConverter();
        JsonParseResult pr = parser.parse(chunk);
        
        JsonParseResult sub = pr.stepInto("obj", 0);
        Assert.assertEquals(-1, pConv.toInteger(sub.getData("iValue")));
        Assert.assertEquals("abba", pConv.toString(sub.getData("sValue")));
        sub.stepOut();
        
        sub = pr.stepInto("arr", 1);
        int size = sub.getArraySize();
        Assert.assertEquals(1, size);
        for (int i = 0; i < size; i++) {
            JsonParseResult subO = sub.stepInto("", i);
            Assert.assertEquals(1, pConv.toInteger(subO.getData("iValue")));
            Assert.assertEquals("bap", pConv.toString(subO.getData("sValue")));
            subO.stepOut();
        }
        sub.stepOut();
    }

}
