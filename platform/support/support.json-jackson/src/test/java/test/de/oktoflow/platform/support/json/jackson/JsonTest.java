/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.oktoflow.platform.support.json.jackson;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Filter;
import de.iip_ecosphere.platform.support.IgnoreProperties;
import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonArray;
import de.iip_ecosphere.platform.support.json.JsonGenerator;
import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.json.JsonIterator.EntryIterator;
import de.iip_ecosphere.platform.support.json.JsonIterator.ValueType;
import de.iip_ecosphere.platform.support.json.JsonNumber;
import de.iip_ecosphere.platform.support.json.JsonObject;
import de.iip_ecosphere.platform.support.json.JsonString;
import de.oktoflow.platform.support.json.jackson.JacksonJson;
import de.oktoflow.platform.support.json.jackson.JsoniterAny;
import iip.datatypes.DataImpl;
import iip.datatypes.RoutingCommandNew;
import iip.datatypes.RoutingCommandNewImpl;
import iip.datatypes.RoutingCommandOld;
import iip.datatypes.RoutingCommandOldImpl;
import iip.serializers.RoutingCommandNewImplSerializer;
import iip.serializers.RoutingCommandNewSerializer;
import iip.serializers.RoutingCommandOldImplSerializer;
import iip.serializers.RoutingCommandOldSerializer;

/**
 * Tests {@link Json}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonTest {
    
    public static final String FILTER_ID_DATA = "dataFilter";

    /**
     * A test data class.
     * 
     * @author Holger Eichelberger, SSE
     */
    @Filter(FILTER_ID_DATA)
    public static class Data {
        
        private int iValue;
        private String sValue;
        
        /**
         * Returns the string value. 
         * 
         * @return the iValue
         */
        public int getiValue() {
            return iValue;
        }
        
        /**
         * Changes the string value. [JSON]
         * 
         * @param iValue the iValue to set
         */
        public void setiValue(int iValue) {
            this.iValue = iValue;
        }
        
        /**
         * Returns the string value. 
         * 
         * @return the sValue
         */
        public String getsValue() {
            return sValue;
        }
        
        /**
         * Changes the string value. [JSON]
         * 
         * @param sValue the sValue to set
         */
        public void setsValue(String sValue) {
            this.sValue = sValue;
        } 
        
    }
    
    /**
     * Asserts data instances.
     *
     * @param value the value to test
     * @param expected the expected value
     */
    private void assertData(Data value, Data expected) {
        Assert.assertNotNull(value);
        Assert.assertEquals(value.getiValue(), expected.getiValue());
        Assert.assertEquals(value.getsValue(), expected.getsValue());
    }

    /**
     * Tests basic JSON functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJson() throws IOException {
        Json json = Json.createInstance();
        Assert.assertTrue(json instanceof JacksonJson);

        Data data = new Data();
        data.setiValue(10);
        data.setsValue("abba");
        String s = Json.toJsonDflt(data);
        Data data1 = Json.fromJsonDflt(s, Data.class);
        assertData(data1, data);
        
        s = json.toJson(data);
        data1 = json.fromJson(s, Data.class);
        assertData(data1, data);
        
        byte[] b = json.writeValueAsBytes(data);
        data1 = json.readValue(b, Data.class);
        assertData(data1, data);
    }
    
    /**
     * Tests basic {@link JsonObject} functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonObject() throws IOException {
        JsonObject jobj = Json.createObjectBuilder()
            .add("intVal", 1)
            .add("strVal", "abc")
            .add("boolVal", true)
            .add("dblVal", 2.0)
            .add("arr", Json.createArrayBuilder()
                .add(1)
                .add("str")
                .add(true)
                .add(Json.createArrayBuilder()) // as array builder
                .build()) // as array value
            .build();
        String json = jobj.toString();
        
        JsonObject obj = Json.createObject(json);
        Assert.assertNotNull(obj);

        JsonNumber n1 = obj.getJsonNumber("intVal");
        Assert.assertNotNull(n1);
        Assert.assertEquals(1, n1.intValue());

        JsonNumber n2 = obj.getJsonNumber("dblVal");
        Assert.assertNotNull(n2);
        Assert.assertEquals(2.0, n2.intValue(), 0.01);
        
        JsonString s1 = obj.getJsonString("strVal");
        Assert.assertNotNull(s1);
        Assert.assertEquals("abc", s1.getString());

        Assert.assertEquals(1, obj.getInt("intVal"));
        Assert.assertEquals("abc", obj.getString("strVal"));
        Assert.assertEquals(true, obj.getBoolean("boolVal"));
        
        JsonArray a1 = obj.getJsonArray("arr");
        Assert.assertEquals(4, a1.size());
        Assert.assertNotNull(a1);
        Assert.assertEquals("str", a1.getString(1));
        Assert.assertEquals(1, a1.getInt(0));
        Assert.assertEquals(true, a1.getBoolean(2));
        JsonArray a2 = a1.getJsonArray(3);
        Assert.assertNotNull(a2);
        Assert.assertEquals(0, a2.size());
    }

    /**
     * Tests {@link JsoniterAny}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonIter() throws IOException {
        JsonObject jobj = Json.createObjectBuilder()
            .add("intVal", 1)
            .add("strVal", "abc")
            .add("boolVal", true)
            .add("dblVal", 2.0)
            .add("arr", Json.createArrayBuilder()
                .add(1)
                .add("str")
                .add(true)
                .add(Json.createArrayBuilder()) // as array builder
                .build()) // as array value
            .build();
        String json = jobj.toString();
        
        JsonIterator iter = Json.parse(json);
        Assert.assertNotNull(iter.getAnyKey());
        Assert.assertEquals(ValueType.OBJECT, iter.valueType());
        Assert.assertEquals(5, iter.size());
        Assert.assertTrue(iter.containsKey("strVal"));
        Assert.assertFalse(iter.containsKey("xyz"));
        JsonIterator tmp = iter.get("intVal"); 
        Assert.assertNotNull(tmp);
        Assert.assertNull(tmp.getAnyKey());
        Assert.assertEquals(ValueType.NUMBER, tmp.valueType());
        Assert.assertEquals(1, tmp.toIntValue());
        tmp = iter.get("strVal"); 
        Assert.assertNotNull(tmp);
        Assert.assertEquals(ValueType.STRING, tmp.valueType());
        Assert.assertEquals("abc", tmp.toStringValue());
        tmp = iter.get("boolVal"); 
        Assert.assertNotNull(tmp);
        Assert.assertEquals(ValueType.BOOLEAN, tmp.valueType());
        Assert.assertEquals(true, tmp.toBooleanValue());
        tmp = iter.get("dblVal"); 
        Assert.assertNotNull(tmp);
        Assert.assertEquals(ValueType.NUMBER, tmp.valueType());
        Assert.assertEquals(2.0, tmp.toDoubleValue(), 0.01);
        Assert.assertEquals(2.0, tmp.toFloatValue(), 0.01);
        tmp = iter.get("arr"); 
        Assert.assertNotNull(tmp);
        Assert.assertEquals(ValueType.ARRAY, tmp.valueType());
        JsonIterator arrTmp = tmp.get(0);
        Assert.assertNotNull(arrTmp);
        Assert.assertEquals(ValueType.NUMBER, arrTmp.valueType());

        iter = Json.parse(json);
        EntryIterator eIter = iter.entries();
        Map<String, JsonIterator> entries = new HashMap<>();
        while (eIter.next()) {
            entries.put(eIter.key(), eIter.value());
        }
        Assert.assertEquals(5, entries.size());
        Assert.assertTrue(entries.containsKey("arr"));
        
        byte[] data = json.getBytes();
        iter = Json.parse(data);
        tmp = iter.get("arr"); 
        Assert.assertNotNull(tmp);
        byte[] tmpData = iter.slice(data, tmp);
        Assert.assertNotNull(tmpData);
        Assert.assertTrue(tmpData.length < data.length);
        String tmpStr = new String(tmpData);
        Assert.assertTrue(tmpStr.startsWith("[") && tmpStr.endsWith("]"));
    }

    /**
     * Tests {@link JsoniterAny#asMap()}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonIterAsMap() throws IOException {
        JsonObject jobj = Json.createObjectBuilder()
            .add("intVal", 1)
            .add("strVal", "abc")
            .add("boolVal", true)
            .add("dblVal", 2.0)
            .add("arr", Json.createArrayBuilder()
                .add(1)
                .add("str")
                .add(true)
                .add(Json.createArrayBuilder()) // as array builder
                .build()) // as array value
            .add("obj", Json.createObjectBuilder()
                .add("innerIntVal", 1)
                .build())
            .build();
        String json = jobj.toString();
        
        Map<String, Object> data = Json.parse(json).asMap();
        Assert.assertEquals(1L, data.get("intVal"));
        Assert.assertEquals("abc", data.get("strVal"));
        Assert.assertEquals(true, data.get("boolVal"));
        Assert.assertEquals(2.0, data.get("dblVal"));
        Assert.assertTrue(data.get("obj") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) data.get("obj");
        Assert.assertEquals(1L, obj.get("innerIntVal"));
    }

    /**
     * Tests {@link Json#handleIipDataClasses()}.
     */
    @Test
    public void testHandleIipDataClasses() {
        iip.datatypes.Data data = new DataImpl();
        data.setValue(20);
        
        try {
            String str = Json.toJsonDflt(data);
            Assert.assertFalse(str.contains("iField"));
            Assert.assertTrue(str.contains("value"));
            Json.fromJsonDflt(str, iip.datatypes.Data.class);
            Assert.fail("Shall not succeed as cannot instantiate interface");
        } catch (IOException e) {
            // ok
        }
        Json json = Json.createInstance(iip.datatypes.Data.class, DataImpl.class).handleIipDataClasses();
        try {
            String str = json.toJson(data);
            Assert.assertTrue(str.contains("iField"));
            Assert.assertFalse(str.contains("value"));
            iip.datatypes.Data d = json.fromJson(str, iip.datatypes.Data.class);
            Assert.assertNotNull(d);
            Assert.assertEquals(data.getValue(), d.getValue());
        } catch (IOException e) {
            Assert.fail("Shall not fail");
        }

        json = Json.createInstance4All().handleIipDataClasses();
        try {
            String str = json.toJson(data);
            Assert.assertTrue(str.contains("iField"));
            Assert.assertFalse(str.contains("value"));
            iip.datatypes.Data d = json.fromJson(str, iip.datatypes.Data.class);
            Assert.assertNotNull(d);
            Assert.assertEquals(data.getValue(), d.getValue());
        } catch (IOException e) {
            Assert.fail("Shall not fail");
        }
    }
    
    /**
     * Tests {@link Json#exceptFields(String...)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testExceptFields() throws IOException {
        DataImpl data = new DataImpl();
        // consider plain fields
        Json json = Json.createInstance().exceptFields("value");
        String str = json.toJson(data);
        Assert.assertFalse(str.contains("iField"));
        Assert.assertFalse(str.contains("value"));

        // consider annotations
        json = Json.createInstance4All().exceptFields("iField");
        str = json.toJson(data);
        Assert.assertFalse(str.contains("iField"));
        Assert.assertFalse(str.contains("value"));
    }
    
    /**
     * Tests the generator.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testGenerator() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = Json.createGenerator(writer);
        gen.writeStartObject();
        gen.writeFieldName("test");
        gen.writeNumber(1);
        gen.writeFieldName("val");
        gen.writeNull();
        gen.writeFieldName("text");
        gen.writeString("abc");
        gen.writeFieldName("flag");
        gen.writeBoolean(true);
        gen.writeFieldName("arr");
        gen.writeArray(new int[] {1, 2}, 0, 2);
        gen.writeFieldName("arr2");
        gen.writeStartArray();
        gen.writeNumber(1.23);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();
        String tmp = writer.toString();
        JsonObject obj = Json.createObject(tmp);
        Assert.assertTrue(obj.containsKey("test"));
        Assert.assertEquals(1, obj.getInt("test"));
        Assert.assertTrue(obj.containsKey("val"));
        Assert.assertNull(obj.getValue("val"));
        Assert.assertTrue(obj.containsKey("text"));
        Assert.assertEquals("abc", obj.getString("text"));
        Assert.assertTrue(obj.containsKey("flag"));
        Assert.assertEquals(true, obj.getBoolean("flag"));
        Assert.assertTrue(obj.containsKey("arr"));
        JsonArray arr = obj.getJsonArray("arr");
        Assert.assertNotNull(arr);
        Assert.assertEquals(2, arr.size());
        Assert.assertEquals(1, arr.getInt(0));
        Assert.assertEquals(2, arr.getInt(1));
        Assert.assertTrue(obj.containsKey("arr2"));
        arr = obj.getJsonArray("arr2");
        Assert.assertNotNull(arr);
        Assert.assertEquals(1, arr.size());
        Assert.assertEquals(1.23, arr.getJsonNumber(0).doubleValue(), 0.01);
    }
    
    /**
     * From configuration's Routing test, test two classes (old, before/new, after migration to plugins) in oktoflow's 
     * serialization style.
     */
    @Test
    public void testSerializerMigration() throws IOException {
        RoutingCommandNewImpl rcni = new RoutingCommandNewImpl();
        RoutingCommandNew rcn = rcni;
        rcn.setCmd("myCmdNew");
        RoutingCommandOldImpl rcoi = new RoutingCommandOldImpl();
        RoutingCommandOldImpl rco = rcoi;
        rco.setCmd("myCmdOld");
        
        RoutingCommandNewSerializer rcnS = new RoutingCommandNewSerializer();
        byte[] rcnJ = rcnS.to(rcn);
        RoutingCommandNewImplSerializer rcniS = new RoutingCommandNewImplSerializer();
        byte[] rcniJ = rcniS.to(rcni);
        RoutingCommandOldSerializer rcoS = new RoutingCommandOldSerializer();
        byte[] rcoJ = rcoS.to(rco);
        RoutingCommandOldImplSerializer rcoiS = new RoutingCommandOldImplSerializer();
        byte[] rcoiJ = rcoiS.to(rco);
        
        RoutingCommandNewImpl rcniD = rcniS.from(rcniJ);
        RoutingCommandNew rcnD = rcnS.from(rcnJ);
        RoutingCommandOldImpl rcoiD = rcoiS.from(rcoiJ);
        RoutingCommandOld rcoD = rcoS.from(rcoJ);
        
        Assert.assertEquals(rcoi.getCmd(), rcoiD.getCmd());
        Assert.assertEquals(rcoi, rcoiD);
        Assert.assertEquals(rco.getCmd(), rcoD.getCmd());
        Assert.assertEquals(rco, rcoD);
        Assert.assertEquals(rcni.getCmd(), rcniD.getCmd());
        Assert.assertEquals(rcni, rcniD);
        Assert.assertEquals(rcn.getCmd(), rcnD.getCmd());
        Assert.assertEquals(rcn, rcnD);
    }

    @IgnoreProperties(ignoreUnknown = true)
    static class TestIgnoreUnknown {
        
        private boolean flag;

        /**
         * Returns the flag.
         * 
         * @return the flag value
         */
        public boolean getFlag() {
            return flag;
        }

        /**
         * Changes the flag.
         * 
         * @param flag the new flag value
         */
        public void setFlag(boolean flag) {
            this.flag = flag;
        }
        
    }
    
    /**
     * Tests {@link IgnoreProperties} and {@link Json#failOnUnknownProperties(boolean)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIgnoreUnkown() throws IOException {
        Json json = Json.createInstance(TestIgnoreUnknown.class); // or 4All
        String data = Json.createObjectBuilder()
            .add("field", 123)
            .add("flag", true)
            .build()
            .toString();
        TestIgnoreUnknown obj = json.fromJson(data, TestIgnoreUnknown.class);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.getFlag());
        
        json = Json.createInstance().failOnUnknownProperties(false); // or 4All
        obj = json.fromJson(data, TestIgnoreUnknown.class);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.getFlag());
    }
    
    /**
     * Tests explicitly filtering for fields using {@link Json#configureExceptFieldsFilter(String, String...)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testExcludeFilter() throws IOException {
        Json json = Json.createInstance(Data.class).configureExceptFieldsFilter(FILTER_ID_DATA, "sValue");
        Data data = new Data();
        data.setiValue(10);
        data.setsValue("abc");

        String text = json.toJson(data);
        JsonObject obj = Json.createObject(text);
        Assert.assertNotNull(obj);
        Assert.assertFalse(obj.containsKey("sValue")); // excluded based on annotation, but not in other tests

        byte[] ser = json.writeValueAsBytes(data);
        obj = Json.createObject(ser);
        Assert.assertNotNull(obj);
        Assert.assertFalse(obj.containsKey("sValue")); // excluded based on annotation, but not in other tests
    }

    /**
     * Am enum for enum testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum MyEnum {
        VAL_1,
        VAL_2
    }
    
    /**
     * A data class for enum testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class EnumTest {
        
        private MyEnum eVal;

        /**
         * Returns eVal.
         * 
         * @return eVal
         */
        public MyEnum geteVal() {
            return eVal;
        }

        /**
         * Changes eVal.
         * 
         * @param eVal the new value
         */
        public void seteVal(MyEnum eVal) {
            this.eVal = eVal;
        }
        
    }

    /**
     * Tests declaring, serializing and deserializing enums.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testEnums() throws IOException {
        Json mapper = Json.createInstance4All();
        mapper.declareEnums(mapper.createEnumValueMapping(MyEnum.class));
        EnumTest obj = new EnumTest();
        obj.seteVal(MyEnum.VAL_1);
        String txt = mapper.toJson(obj);
        EnumTest obj2 = mapper.fromJson(txt, EnumTest.class);
        Assert.assertNotNull(obj2);
        Assert.assertEquals(obj.geteVal(), obj2.geteVal());
        Assert.assertTrue(obj.geteVal() == obj2.geteVal());
    }

}
