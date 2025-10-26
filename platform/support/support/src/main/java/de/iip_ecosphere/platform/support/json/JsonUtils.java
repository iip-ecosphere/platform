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

package de.iip_ecosphere.platform.support.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.ConfiguredName;
import de.iip_ecosphere.platform.support.json.Json.EnumMapping;

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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Turns an {@code object} to JSON.
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
                result = MAPPER.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                // handled by default value
            }            
            /*try {
                result = Json.toJsonDflt(obj);
            } catch (IOException e) {
                // handled by default value
            }*/
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
                result = MAPPER.readValue(json.toString(), cls);
            } catch (JsonProcessingException e) {
                //result = null;
            }            
            /*try {
                result = Json.fromJsonDflt(json.toString(), cls);
            } catch (IOException e) {
                //result = null;
            }*/
        }
        return result; 
    }
    
    /**
     * Reads a typed List from a JSON string.
     * 
     * @param <R> the entity type
     * @param json the JSON value (usually a String)
     * @param cls the class of the entity type to read
     * @return the list or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public static <R> java.util.List<R> listFromJson(Object json, Class<R> cls) {
        return Json.listFromJsonDflt(json, cls);
    }

    /**
     * Reads a typed Map from a JSON string.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param json the JSON value (usually a String)
     * @param keyCls the class of the key type to read
     * @param valueCls the class of the value type to read
     * @return the map or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public static <K, V> Map<K, V> mapFromJson(Object json, Class<K> keyCls, Class<K> valueCls) {
        return Json.mapFromJsonDflt(json, keyCls, valueCls);
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
     * Configures the given class for through the abstracted annotations.
     * 
     * @param mapper the mapper to be configured
     * @param cls the class to be configured
     * @return {@code mapper}
     */
    public static ObjectMapper configureFor(ObjectMapper mapper, Class<?> cls) {
        JsonIgnoreProperties annIgnoreProp = cls.getAnnotation(JsonIgnoreProperties.class);
        Set<String> ignores = new HashSet<>();
        Map<String, String> renames = new HashMap<>();
        boolean ignoreCls = false;
        if (null != annIgnoreProp && annIgnoreProp.ignoreUnknown()) {
            ignoreCls = true;
        }
        for (Field f : cls.getDeclaredFields()) {
            handleAnnotations(f.getName(), f, ignores, renames);
        }
        for (Method m : cls.getDeclaredMethods()) {
            String name = m.getName();
            if (name.length() > 3 && (name.startsWith("get") || name.startsWith("set"))) {
                name = name.substring(3);
            }
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            handleAnnotations(name, m, ignores, renames);
        }
        if (ignoreCls || ignores.size() > 0) {
            SimpleModule module = new SimpleModule();
            ignores = ignores.isEmpty() ? null : ignores; // ignore all
            module.setSerializerModifier(new CustomPropertyExclusionModifier(cls, ignores));
            mapper.registerModule(module);
        }
        if (!renames.isEmpty()) {
            PropertyNamingStrategy pns = mapper.getPropertyNamingStrategy();
            CustomPropertyNamingStrategy cpns;
            
            if (pns instanceof CustomPropertyNamingStrategy) {
                cpns = (CustomPropertyNamingStrategy) pns;
            } else {
                cpns = new CustomPropertyNamingStrategy();
                mapper.setPropertyNamingStrategy(cpns);
            }
            cpns.addMapping(cls, renames);
        }
        return mapper;
    }
    
    /**
     * Handles supported annotations ({@link ConfiguredName}, {@link JsonIgnore}, {@link JsonProperty}) on an 
     * accessible object.
     * 
     * @param propName the property name
     * @param obj the accessible object
     * @param ignores the properties to ignore
     * @param renames the property renamings (original name, new name)
     */
    private static void handleAnnotations(String propName, AccessibleObject obj, Set<String> ignores, 
        Map<String, String> renames) {
        ConfiguredName cfgName = obj.getAnnotation(ConfiguredName.class);
        JsonIgnore annIgnore = obj.getAnnotation(JsonIgnore.class);
        JsonProperty annProp = obj.getAnnotation(JsonProperty.class);
        if (null != annIgnore && annIgnore.value()) {
            ignores.add(propName);
        }
        if (null != cfgName && cfgName.value() != null && cfgName.value().length() > 0) {
            renames.put(propName, cfgName.value());
        } else if (null != annProp && annProp.value() != null && annProp.value().length() > 0) {
            renames.put(propName, annProp.value());
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
    
    /**
     * Returns an object writer for a mapper that applies a filter on {@code fieldNames} to be excluded.
     * 
     * @param mapper the mapper
     * @param fieldNames the field names
     * @return the object writer
     */
    public static ObjectMapper exceptFields(ObjectMapper mapper, String... fieldNames) {
        final Set<String> exclusions = CollectionUtils.addAll(new HashSet<String>(), fieldNames);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

            private static final long serialVersionUID = -6485293464445674590L;

            @Override
            public boolean hasIgnoreMarker(final AnnotatedMember member) {
                boolean excludesByName = exclusions.contains(member.getName()) || super.hasIgnoreMarker(member);
                if (!excludesByName) {
                    ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
                    if (null != cfgName && cfgName.value() != null) {
                        excludesByName = exclusions.contains(cfgName.value());
                    }
                }
                if (!excludesByName) {
                    JsonIgnore jsonIgnore = member.getAnnotation(JsonIgnore.class);
                    if (null != jsonIgnore) {
                        excludesByName = jsonIgnore.value();
                    }
                }
                if (!excludesByName) {
                    JsonProperty jsonProp = member.getAnnotation(JsonProperty.class);
                    if (null != jsonProp && jsonProp.value() != null) {
                        excludesByName = exclusions.contains(jsonProp.value());
                    }
                }
                return excludesByName;
            }
        });
        
        return mapper;
    }
    
    /**
     * Turns {@code object} into JSON using {@code mapper}.
     * 
     * @param mapper the object mapper
     * @param object the object to write (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     */
    public static String toJson(ObjectMapper mapper, Object object) {
        String result = "";
        if (null != object) {
            try {
                result = mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }
    
    /**
     * Specifies the mapping of an enumeration for serialization/deserialization.
     * 
     * @param <T> the enumeration type to map
     * @author Holger Eichelberger, SSE
     */
    public static class JacksonEnumMapping<T> implements EnumMapping<T> {
        
        private Class<T> type;
        private Map<String, T> mapping = new HashMap<>();
        
        /**
         * Creates a mapping specification, with no mapping.
         * 
         * @param type the type to map
         */
        public JacksonEnumMapping(Class<T> type) {
            this(type, null);
        }
 
        /**
         * Creates a mapping specification with mapping.
         * 
         * @param type the type to map
         * @param mapping the name-value mapping, may be <b>null</b>
         */
        public JacksonEnumMapping(Class<T> type, Map<String, T> mapping) {
            this.type = type;
            if (null != mapping) {
                this.mapping.putAll(mapping);
            }
        }
 
        @Override
        public void addMapping(String name, T value) {
            mapping.put(name, value);
        }
        
        @Override
        public Class<T> getType() {
            return type;
        }

        /**
         * Ass this mapping to the given module.
         * 
         * @param module the target module
         */
        public void addToModule(SimpleModule module) {
            module.addDeserializer(type, new EnumDeserializer<T>(mapping, type));
        }
        
    }
    
    
    /**
     * Generic enum deserializer.
     * 
     * @param <T> the type to deserialize
     * @author Holger Eichelberger, SSE
     */
    public static class EnumDeserializer<T> extends StdDeserializer<T> {

        private static final long serialVersionUID = -1654499344527076310L;
        private Map<String, T> mapping;
        
        /**
         * Creates a deserializer class.
         * 
         * @param mapping the mapping to use for deserialization
         * @param cls the type of enums to deserialize
         */
        public EnumDeserializer(Map<String, T> mapping, Class<T> cls) {
            super(cls);
            this.mapping = mapping;
        }
        
        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
            T result = null;
            JsonNode node = jp.getCodec().readTree(jp);
            String name = node.asText();
            if (null != name) {
                result = mapping.get(name);
            }
            return result;
        }
        
    }

    /**
     * Declares enumerations on the specified mapper.
     * 
     * @param mapper the mapper
     * @param mappings the enumeration mappings
     * @return {@code mapper}
     */
    public static ObjectMapper declareEnums(ObjectMapper mapper, EnumMapping<?>... mappings) {
        SimpleModule module = new SimpleModule();
        for (EnumMapping<?> m : mappings) {
            if (m instanceof JacksonEnumMapping) {
                ((JacksonEnumMapping<?>) m).addToModule(module);
            }
        }
        return mapper.registerModule(module);
    }

    /**
     * Configures the given mapper for lazy serialization ignoring given classes and members.
     * 
     * @param mapper the mapper to configure
     * @param ignore, classes (also as return types) and (reflection) fields that shall be ignored
     * @return the object mapper
     */
    public static ObjectMapper configureLazy(ObjectMapper mapper, Set<Object> ignore) {
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // may become empty through ignores
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL, 
            JsonTypeInfo.As.WRAPPER_ARRAY);
        if (!ignore.isEmpty()) {
            mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
    
                private static final long serialVersionUID = 7445592829151624983L;
    
                @Override
                public boolean hasIgnoreMarker(final AnnotatedMember member) {
                    return ignore.contains(member.getType().getRawClass()) || ignore.contains(member.getMember()) 
                        || super.hasIgnoreMarker(member); 
                }
            });
        }
        return mapper;
    }

    /**
     * Creates an iterator over the information in {@code stream} assuming a heterogeneous collection of {@code cls}.
     * 
     * @param <T> the element type
     * @param mapper the object mapper to use
     * @param stream the input stream to read
     * @param cls the element type
     * @return an iterator over the element types
     * @throws IOException if accessing the stream fails
     */
    public static <T> IOIterator<T> createIterator(ObjectMapper mapper, InputStream stream, Class<T> cls) 
        throws IOException {
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(stream);
        jp.setCodec(mapper);
        jp.nextToken();
        return new IOIterator<T>() {

            @Override
            public boolean hasNext() throws IOException {
                return jp.hasCurrentToken();
            }

            @Override
            public T next() throws IOException {
                T data = jp.readValueAs(cls);
                jp.nextToken();
                return data;
            }
            
        };
    }

    /**
     * Property exclusion modifier to simulate {@link JsonIgnoreProperties}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomPropertyExclusionModifier extends BeanSerializerModifier {

        private final Set<String> propertiesToExclude;
        private final Class<?> targetClass;

        /**
         * Creates an instance.
         * 
         * @param targetClass the target class
         * @param propertiesToExclude properties to specifically exclude 
         */
        public CustomPropertyExclusionModifier(Class<?> targetClass, String... propertiesToExclude) {
            this(targetClass, new HashSet<>(Arrays.asList(propertiesToExclude)));
        }

        /**
         * Creates an instance.
         * 
         * @param targetClass the target class
         * @param propertiesToExclude properties to specifically exclude, if <b>null</b> exclude all
         */
        public CustomPropertyExclusionModifier(Class<?> targetClass, Set<String> propertiesToExclude) {
            this.targetClass = targetClass;
            this.propertiesToExclude = propertiesToExclude;
        }

        @Override
        public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {

            // Only apply this modifier to the specific target class
            if (beanDesc.getBeanClass() == targetClass) {
                List<BeanPropertyWriter> newProperties = new ArrayList<>();
                for (BeanPropertyWriter writer : beanProperties) {
                    if (propertiesToExclude != null && !propertiesToExclude.contains(writer.getName())) {
                        newProperties.add(writer);
                    }
                }
                return newProperties;
            }
            return super.changeProperties(config, beanDesc, beanProperties);
        }
    }    
    
    /**
     * Renames properties.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomPropertyNamingStrategy extends PropertyNamingStrategy {

        private static final long serialVersionUID = -6610876934860406571L;
        private final Map<Class<?>, Map<String, String>> propertyRenameMap = new HashMap<>();
        
        /**
         * Adds a mapping.
         * 
         * @param cls the class the mapping applies to
         * @param mapping the property-to-name mapping
         */
        public void addMapping(Class<?> cls, Map<String, String> mapping) {
            propertyRenameMap.put(cls, mapping);
        }
        
        /**
         * Returns a (mapped) name, if not found relying on {@code fallback}.
         * 
         * @param cls the class to return the mapping for
         * @param defaultName the default name of the property as detected by Jackson
         * @param fallback the fallback, usually a super call
         * @return the name
         */
        private String getMapping(Class<?> cls, String defaultName, Supplier<String> fallback) {
            Map<String, String> mapping = propertyRenameMap.get(cls);
            if (null != mapping && mapping.containsKey(defaultName)) {
                return mapping.get(defaultName);
            } else {
                return fallback.get();
            }
        }

        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            return getMapping(field.getDeclaringClass(), defaultName, 
                () -> super.nameForField(config, field, defaultName));
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            return getMapping(method.getDeclaringClass(), defaultName, 
                () -> super.nameForGetterMethod(config, method, defaultName));
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            return getMapping(method.getDeclaringClass(), defaultName, 
                () -> super.nameForSetterMethod(config, method, defaultName));
        }

        @Override
        public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, 
            String defaultName) {
            return getMapping(ctorParam.getDeclaringClass(), defaultName, 
                () -> super.nameForConstructorParameter(config, ctorParam, defaultName));
        }

    }

}
