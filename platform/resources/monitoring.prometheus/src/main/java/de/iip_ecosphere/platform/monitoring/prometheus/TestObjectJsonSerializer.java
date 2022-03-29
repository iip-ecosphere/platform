package de.iip_ecosphere.platform.monitoring.prometheus;
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
import java.io.IOException;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

public class TestObjectJsonSerializer implements Serializer<TestObject> {
    
    /** String reading JSON.
     * 
     * @param obj
     * @param field
     * @return json obj.
     */
    public static String readString(JSONObject obj, String field) {
        Object tmp = obj.get(field);
        return null == tmp ? null : tmp.toString();
    }

    /** Int reading JSON.
     * 
     * @param obj
     * @param field
     * @param dflt
     * @return json obj
     * @throws IOException
     */
    public static int readInteger(JSONObject obj, String field, int dflt) throws IOException {
        try {
            Object tmp = obj.get(field);
            return null == tmp ? dflt : Integer.parseInt(tmp.toString());
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public TestObject from(byte[] data) {
        // TODO Auto-generated method stub
        TestObject testobj = null;
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(new String(data));
            testobj = new TestObject(readString(obj, "description"), readInteger(obj, "value", 0));
        } catch (org.json.simple.parser.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testobj;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] to(TestObject source) throws IOException {
        // TODO Auto-generated method stub
        JSONObject json = new JSONObject();
        json.put("description", source.getDescription());
        json.put("value", source.getValue());
        return json.toString().getBytes();
    }

    @Override
    public TestObject clone(TestObject origin) throws IOException {
        // TODO Auto-generated method stub
        return new TestObject(origin.getDescription(), origin.getValue());
    }

    @Override
    public Class<TestObject> getType() {
        // TODO Auto-generated method stub
        return TestObject.class;
    }

}
