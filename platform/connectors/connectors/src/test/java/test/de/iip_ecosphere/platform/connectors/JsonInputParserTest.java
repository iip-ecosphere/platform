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

import de.iip_ecosphere.platform.connectors.parser.JsonInputParser;
import de.iip_ecosphere.platform.connectors.parser.JsonInputParser.JsonInputConverter;
import de.iip_ecosphere.platform.connectors.parser.JsonInputParser.JsonParseResult;

/**
 * Tests {@link JsonInputParser}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonInputParserTest {
    
    /**
     * Tests {@link JsonInputParser}.
     */
    @Test
    public void testJsonInputParser() throws IOException {
        JsonInputParser parser = new JsonInputParser();
        String data = "{\"name\": \"abc\", \"value\": 1, \"enum\":20, \"enumName\":\"TEST1\"}";
        JsonParseResult res = parser.parse(data.getBytes());
        JsonInputConverter conv = parser.getConverter();
        Assert.assertEquals("abc", conv.toString(res.getData("name", 0)));
        Assert.assertEquals(1, conv.toInteger(res.getData("value", 0)));
        Assert.assertEquals(MyEnum.TEST2, conv.toEnum(res.getData("enum", 2), MyEnum.class));
        Assert.assertEquals(MyEnum.TEST1, conv.toEnum(res.getData("enumName", 3), MyEnum.class));
        
        Assert.assertEquals("", res.getFieldName());
        Assert.assertEquals("name", res.getFieldName(0));
        Assert.assertEquals("value", res.getFieldName(1));
        
        data = "{\"obj\": {\"name\": \"abc\", \"value\": 1}}";
        res = parser.parse(data.getBytes());
        Assert.assertEquals("abc", conv.toString(res.getData("obj.name", 0)));
        Assert.assertEquals(1, conv.toInteger(res.getData("obj.value", 0)));
        Assert.assertEquals(1, conv.toInteger(res.getData("x", 0, 1)));

        Assert.assertEquals("", res.getFieldName());
        Assert.assertEquals("obj", res.getFieldName(0));
        Assert.assertEquals("name", res.getFieldName(0, 0));
        Assert.assertEquals("value", res.getFieldName(0, 1));
        Assert.assertEquals("value", res.getFieldName(v -> Assert.assertEquals(1, conv.toInteger(v)), 0, 1));
        
        res = res.stepInto("obj", 0);
        Assert.assertEquals("abc", conv.toString(res.getData("name", 0)));
        Assert.assertEquals(1, conv.toInteger(res.getData("value", 0)));
        Assert.assertEquals(1, conv.toInteger(res.getData("x", 1)));
        Assert.assertEquals("name", res.getFieldName(0));
        Assert.assertEquals("value", res.getFieldName(1));
        res = res.stepOut();
        Assert.assertEquals("obj", res.getFieldName(0));
    }

}
