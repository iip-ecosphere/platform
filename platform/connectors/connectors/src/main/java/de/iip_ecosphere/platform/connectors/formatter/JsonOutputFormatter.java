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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonGenerator;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElement;

/**
 * JSON output formatter (preliminary).
 * 
 * @author Holger Eichelberger, SSE
 */
@MachineFormatter
public class JsonOutputFormatter implements OutputFormatter<IOConsumer<JsonGenerator>> {

    private enum StructureType {
        ARRAY,
        OBJECT
    }
    
    private StringWriter writer; // 
    private JsonGenerator gen; // temporary
    private String parentName = ""; // temporary, initial top-level
    private Stack<StructureType> structures = new Stack<>();
    
    public static class JsonOutputConverter implements OutputConverter<IOConsumer<JsonGenerator>> {

        @Override
        public IOConsumer<JsonGenerator> fromInteger(int data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromByte(byte data) throws IOException {
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
        public IOConsumer<JsonGenerator> fromStringArray(String[] data) throws IOException {
            return g -> {
                if (null == data) {
                    g.writeNull();
                } else {
                    g.writeArray(data, 0, data.length);
                }
            };
        }

        @Override
        public IOConsumer<JsonGenerator> fromByteArray(byte[] data) throws IOException {
            return g -> {
                if (null == data) {
                    g.writeNull();
                } else {
                    int[] tmp = new int[data.length];
                    for (int i = 0; i < data.length; i++) {
                        tmp[i] = data[i];
                    }
                    g.writeArray(tmp, 0, tmp.length);
                }
            };
        }

        @Override
        public IOConsumer<JsonGenerator> fromList(List<?> data) throws IOException {
            return g -> {
                g.writeStartArray();
                for (Object o : data) {
                    if (o instanceof QualifiedElement) {
                        o = ((QualifiedElement<?>) o).getValue();
                    }
                    g.writeObject(o);
                }
                g.writeEndArray();
            };
        }

        @Override
        public <E> IOConsumer<JsonGenerator> fromElementList(List<QualifiedElement<E>> data) throws IOException {
            return g -> {
                g.writeStartArray();
                for (QualifiedElement<E> e : data) {
                    g.writeObject(e.getValue());
                }
                g.writeEndArray();
            };
        }

        @Override
        public IOConsumer<JsonGenerator> fromObject(Object data) throws IOException {
            return g -> g.writeObject(data);
        }
        
        @Override
        public IOConsumer<JsonGenerator> fromDate(Date data, String format) throws IOException {
            return g -> {
                g.writeString(TimeUtils.format(data, format));
            };
        }

        @Override
        public IOConsumer<JsonGenerator> fromBigInteger(BigInteger data) throws IOException {
            return g -> g.writeNumber(data);
        }

        @Override
        public IOConsumer<JsonGenerator> fromBigDecimal(BigDecimal data) throws IOException {
            return g -> g.writeNumber(data);
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
    
    /**
     * Initializes the JSON writing.
     * 
     * @throws IOException if initialization fails
     */
    private void initialize() throws IOException {
        if (null == gen) {
            writer = new StringWriter();
            gen = Json.createGenerator(writer);
            gen.writeStartObject();
        }
    }
    
    @Override
    public void add(String name, IOConsumer<JsonGenerator> func) throws IOException {
        initialize();
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
    public void startArrayStructure(String name) throws IOException {
        initialize();
        structures.push(StructureType.ARRAY);
        if (null != name) {
            gen.writeFieldName(name);
        }
        gen.writeStartArray();
    }

    @Override
    public void startObjectStructure(String name) throws IOException {
        initialize();
        structures.push(StructureType.OBJECT);
        if (null != name) {
            gen.writeFieldName(name);
        }
        gen.writeStartObject();
    }

    @Override
    public void endStructure() throws IOException {
        if (structures.isEmpty()) {
            throw new IOException("No structure to close");
        }
        switch (structures.pop()) {
        case ARRAY:
            gen.writeEndArray();
            break;
        case OBJECT:
            gen.writeEndObject();
            break;
        default: 
            break;
        }
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
