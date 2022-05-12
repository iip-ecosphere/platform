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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.function.IOConsumer;

/**
 * JSON output formatter (preliminary).
 * 
 * @author Holger Eichelberger, SSE
 */
@MachineFormatter
public class JsonOutputFormatter implements OutputFormatter<IOConsumer<JsonGenerator>> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private StringWriter writer; // 
    private JsonGenerator gen; // temporary
    private String parentName = ""; // temporary, initial top-level
    
    public static class JsonOutputConverter implements OutputConverter<IOConsumer<JsonGenerator>> {

        @Override
        public IOConsumer<JsonGenerator> fromInteger(int data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromLong(long data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromString(String data) throws IOException {
            return g -> g.writeString(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromShort(short data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromDouble(double data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromFloat(float data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromBoolean(boolean data) throws IOException {
            return g -> g.writeBoolean(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromIntegerArray(int[] data) throws IOException {
            return g -> g.writeArray(data, 0, data.length);
        }

        @Override
        public IOConsumer<JsonGenerator> fromDoubleArray(double[] data) throws IOException {
            return g -> g.writeArray(data, 0, data.length);
        }

        @Override
        public IOConsumer<JsonGenerator> fromObject(Object data) throws IOException {
            return g -> g.writeObject(data);
        }
        
        @Override
        public IOConsumer<JsonGenerator> fromDate(Date data, String format) throws IOException {
            return g -> {
                SimpleDateFormat f = FormatCache.getDateFormatter(format);
                g.writeString(f.format(data));
            };
        }

    }
    
    /**
     * Cleans up the nesting.
     * 
     * @param parent the target parent path
     * @throws IOException when writing data fails
     */
    private void cleanNesting(String parent) throws IOException {
        while (parentName.length() > 0) {
            if (parent.equals(parentName)) {
                break;
            } else {
                gen.writeEndObject();
                int pos = parentName.lastIndexOf(SEPARATOR);
                if (pos > 0) {
                    parentName = parentName.substring(0, pos);
                } else {
                    parentName = "";
                }
            }
        }
    }
    
    @Override
    public void add(String name, IOConsumer<JsonGenerator> func) throws IOException {
        if (null == gen) {
            JsonFactory f = objectMapper.getFactory();
            writer = new StringWriter();
            gen = f.createGenerator(writer);
            gen.writeStartObject();
        }
        String fieldName = name;
        if (name.indexOf(SEPARATOR) > 0) {
            int pos = name.lastIndexOf(SEPARATOR);
            fieldName = name.substring(pos + 1);
            String parent = name.substring(0, pos);
            cleanNesting(parent);            
            // either parentName is empty or a prefix of parent
            int startPos = parentName.length() + 1;
            while (!parentName.equals(parent)) {
                pos = parent.indexOf(SEPARATOR, startPos);
                if (pos < 0) {
                    gen.writeFieldName(parent);
                    gen.writeStartObject();
                    parentName = parent;
                    break;
                } else {
                    String pathPartName = parent.substring(startPos, pos);
                    gen.writeFieldName(pathPartName);
                    gen.writeStartObject();
                    if (parentName.length() > 0) {
                        parentName += SEPARATOR;
                    }
                    parentName += pathPartName;
                }
            }
        } else { // top-level field
            cleanNesting("");
            parentName = "";
        }
        gen.writeFieldName(fieldName);
        func.accept(gen);
    }

    @Override
    public byte[] chunkCompleted() throws IOException {
        byte[] res = null;
        if (gen != null) {
            cleanNesting("");
            gen.writeEndObject();
            gen.close();
            res = writer.toString().getBytes();
            gen = null;
        }
        return res;
    }

    @Override
    public JsonOutputConverter getConverter() {
        return new JsonOutputConverter();
    }

}
