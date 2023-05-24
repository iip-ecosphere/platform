/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;

/**
 * Some JSON utility methods, also reading/writing of specific types.
 * 
 * @author Holger Eichelberger, SSE
 * @author Lemur Project (<a href="http://lemurproject.org/galago-license">BSD License</a>) 
 */
public class JsonUtils {

    /**
     * A type resolver for "Impl" classes in "iip.".
     */
    public static final SimpleAbstractTypeResolver IIP_TYPE_RESOLVER = new SimpleAbstractTypeResolver() {
        
        private static final long serialVersionUID = -3746467806797935401L;
        // no instance data here

        @Override
        public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
            JavaType result = null;
            // for generated IIP-Ecosphere data interfaces, we can try it with Impl classes
            String className = type.getRawClass().getName();
            if (type.isInterface() && className.startsWith("iip.")) {
                String name = className + "Impl";
                try {
                    Class<?> cls = Class.forName(name);
                    result = config.getTypeFactory().constructType(cls);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (null == result) {
                result = super.findTypeMapping(config, type);
            }
            return result;
        }
            
    };

    /**
     * Reads a {@link ServerAddress} from a JSON string.
     * 
     * @param json the JSON value, usually a String
     * @return the server address or <b>null</b> if reading fails
     * @see #toJson(ServerAddress)
     */
    public static ServerAddress serverAddressFromJson(Object json) {
        ServerAddress result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ServerAddressHolder tmp = objectMapper.readValue(json.toString(), ServerAddressHolder.class);
                result = new ServerAddress(tmp.getSchema(), tmp.getHost(), tmp.getPort());
            } catch (JsonProcessingException e) {
                // result = null;
            }
        }
        return result;        
    }
    
    /**
     * Turns a {@link ServerAddress} into JSON.
     * 
     * @param address the address (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     * @see #serverAddressFromJson(Object)
     */
    public static String toJson(ServerAddress address) {
        String result = "";
        if (null != address) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ServerAddressHolder tmp = new ServerAddressHolder(address);
                result = objectMapper.writeValueAsString(tmp);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }

    /**
     * Turns a {@link ServerAddress} into JSON.
     * 
     * @param obj the object (may be <b>null</b>), must have getters/setters for all attributes and a no-arg constructor
     *   no-arg constructor
     * @return the JSON string or an empty string in case of problems/no address
     * @see #fromJson(Object, Class)
     */
    public static String toJson(Object obj) {
        String result = "";
        if (null != obj) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                defineFields(objectMapper);
                result = objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }
    
    /**
     * Reads an Object from a JSON string.
     * 
     * @param <R> the object type, must have getters/setters for all attributes and a no-arg constructor
     * @param json the JSON value (usually a String)
     * @param cls the class of the type to read
     * @return the server address or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public static <R> R fromJson(Object json, Class<R> cls) {
        R result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                defineFields(objectMapper);
                result = objectMapper.readValue(json.toString(), cls);
            } catch (JsonProcessingException e) {
                //result = null;
            }
        }
        return result; 
    }
    
    /**
     * Escapes an input string for JSON. Taken over from 
     * <a href="https://stackoverflow.com/questions/34706849/how-do-i-unescape-a-json-string-using-java-jackson">
     * Stackoverflow</a> and <a href="http://lemurproject.org/">Lemur Project</a>. The respective methods from <a 
     * href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringEscapeUtils.html">
     * Apache Commons Lang3</a> are too slow for our purpose.
     * 
     * @param input the input string
     * @return the escaped string
     */
    public static String escape(String input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            int chx = (int) ch;

            // let's not put any nulls in our strings
            assert (chx != 0);

            if (ch == '\n') {
                output.append("\\n");
            } else if (ch == '\t') {
                output.append("\\t");
            } else if (ch == '\r') {
                output.append("\\r");
            } else if (ch == '\\') {
                output.append("\\\\");
            } else if (ch == '"') {
                output.append("\\\"");
            } else if (ch == '\b') {
                output.append("\\b");
            } else if (ch == '\f') {
                output.append("\\f");
            } else if (chx >= 0x10000) {
                assert false : "Java stores as u16, so it should never give us a character that's bigger than 2 bytes. "
                    + "It literally can't.";
            } else if (chx > 127) {
                output.append(String.format("\\u%04x", chx));
            } else {
                output.append(ch);
            }
        }

        return output.toString();
    }

    /**
     * Unescapes an input string from JSON. Taken over from <a href=
     * "https://stackoverflow.com/questions/34706849/how-do-i-unescape-a-json-string-using-java-jackson">
     * Stackoverflow</a> and <a href="http://lemurproject.org/">Lemur Project</a>.
     * The respective methods from <a href=
     * "https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringEscapeUtils.html">
     * Apache Commons Lang3</a> are too slow for our purpose.
     * 
     * @param input the input string
     * @return the unescaped string
     */
    public static String unescape(String input) {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            char delimiter = input.charAt(i);
            i++; // consume letter or backslash

            if (delimiter == '\\' && i < input.length()) {

                // consume first after backslash
                char ch = input.charAt(i);
                i++;

                if (ch == '\\' || ch == '/' || ch == '"' || ch == '\'') {
                    builder.append(ch);
                } else if (ch == 'n') {
                    builder.append('\n');
                } else if (ch == 'r') {
                    builder.append('\r');
                } else if (ch == 't') {
                    builder.append('\t');
                } else if (ch == 'b') {
                    builder.append('\b');
                } else if (ch == 'f') {
                    builder.append('\f');
                } else if (ch == 'u') {
                    StringBuilder hex = new StringBuilder();

                    // expect 4 digits
                    if (i + 4 > input.length()) {
                        throw new RuntimeException("Not enough unicode digits! ");
                    }
                    for (char x : input.substring(i, i + 4).toCharArray()) {
                        if (!Character.isLetterOrDigit(x)) {
                            throw new RuntimeException("Bad character in unicode escape.");
                        }
                        hex.append(Character.toLowerCase(x));
                    }
                    i += 4; // consume those four digits.

                    int code = Integer.parseInt(hex.toString(), 16);
                    builder.append((char) code);
                } else {
                    throw new RuntimeException("Illegal escape sequence: \\" + ch);
                }
            } else { // it's not a backslash, or it's the last character.
                builder.append(delimiter);
            }
        }

        return builder.toString();
    }
    
    /**
     * A handler for optional fields.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class OptionalFieldsDeserializationProblemHandler extends DeserializationProblemHandler {

        private Class<?> cls;
        private Set<String> optionalFields = new HashSet<String>();
        
        /**
         * Creates an optional fields deserialization problem handler to declare certain fields as optional.
         * 
         * @param cls the class the fields are defined on
         * @param fieldNames the field names
         */
        public OptionalFieldsDeserializationProblemHandler(Class<?> cls, String... fieldNames) {
            this.cls = cls;
            for (String f : fieldNames) {
                optionalFields.add(f);
            }
        }

        @Override
        public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser parser,
            JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName)
            throws IOException {
            boolean result;
            if (optionalFields.contains(propertyName) && beanOrClass.getClass().equals(cls)) {
                result = true;
            } else {
                result = false;
            }
            return result;
        }
        
    }

    /**
     * A property naming strategy exactly using the given names as JSON and Java field/getter/setter names.
     * Applies a fallback strategy if there is no mapping.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MappingPropertyNamingStrategy extends PropertyNamingStrategy {
        
        private static final long serialVersionUID = -3963175454099182994L;
        private PropertyNamingStrategy fallback;
        private Map<String, String> mapping;

        /**
         * Creates a mapping property naming strategy. Fallback strategy is {@code PropertyNamingStrategy} using
         * the default names without strategy.
         * 
         * @param mapping the mapping of field names to json fields
         */
        public MappingPropertyNamingStrategy(Map<String, String> mapping) {
            this(mapping, new PropertyNamingStrategy());
        }

        /**
         * Creates a mapping property naming strategy with explicit fallback strategy.
         * 
         * @param mapping the mapping of field names to json fields
         * @param fallback the fallback strategy
         */
        public MappingPropertyNamingStrategy(Map<String, String> mapping, PropertyNamingStrategy fallback) {
            this.fallback = fallback;
            this.mapping = mapping;
        }

        @Override
        public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam,
            String defaultName) {
            return fallback.nameForConstructorParameter(config, ctorParam, defaultName);
        }
        
        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            String result = field.getName();
            //String result = mapping.get(field.getName());
            if (result == null) {
                result = fallback.nameForField(config, field, defaultName);
            }
            return result;
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            String fieldName = method.getName();
            if (fieldName.startsWith("get")) {
                fieldName = fieldName.substring(3);
            }
            String result = mapping.get(fieldName);
            if (result == null) {
                /*if (fieldName.length() > 2 && Character.isLowerCase(fieldName.charAt(1))) {
                    result = fallback.nameForSetterMethod(config, method, defaultName);
                } else {
                    result = fieldName;
                }*/
                result = fallback.nameForSetterMethod(config, method, defaultName);
            }
            return result;
        }
        
        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            String fieldName = method.getName();
            if (fieldName.startsWith("set")) {
                fieldName = fieldName.substring(3);
            }
            String result = mapping.get(fieldName);
            if (result == null) {
                /*if (fieldName.length() > 2 && Character.isLowerCase(fieldName.charAt(1))) {
                    result = fallback.nameForSetterMethod(config, method, defaultName);
                } else {
                    result = fieldName;
                }*/
                result = fallback.nameForSetterMethod(config, method, defaultName);
            }
            return result;
        }
        
    }
    
    /**
     * Configures a Jackson object mapper for IIP conventions.
     * 
     * @param mapper the mapper to be configured
     * @return {@code mapper}
     */
    public static ObjectMapper handleIipDataClasses(ObjectMapper mapper) {
        SimpleModule iipModule = new SimpleModule();
        iipModule.setAbstractTypes(IIP_TYPE_RESOLVER);
        return mapper.registerModule(iipModule);
    }
    
    /**
     * Defines the given {@code fieldNames} as optional during deserialization.
     * 
     * @param mapper the mapper to define the optionals on
     * @param cls the cls the class {@code fieldNames} are member of
     * @param fieldNames the field names (names of Java fields)
     * @return {@code mapper}
     */
    public static ObjectMapper defineOptionals(ObjectMapper mapper, Class<?> cls, String... fieldNames) {
        return mapper.addHandler(new OptionalFieldsDeserializationProblemHandler(cls, fieldNames));
    }

    /**
     * Defines a mapping of JSON names to Java field names using exactly the given names.
     * 
     * @param mapper the mapper to define the optionals on
     * @param fieldNames the field names (names of JSON/Java fields)
     * @return {@code mapper}
     */
    public static ObjectMapper defineFields(ObjectMapper mapper, String... fieldNames) {
        Map<String, String> mapping = new HashMap<>();
        for (String fn : fieldNames) {
            String javaField = fn;
            if (javaField.length() > 0) {
                javaField = Character.toUpperCase(javaField.charAt(0)) + javaField.substring(1);
            }
            mapping.put(javaField, fn);
        }
        return mapper.setPropertyNamingStrategy(new MappingPropertyNamingStrategy(mapping));
    }

}
