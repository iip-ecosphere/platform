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

package de.oktoflow.platform.support.json.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
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
import de.iip_ecosphere.platform.support.Filter;
import de.iip_ecosphere.platform.support.Ignore;
import de.iip_ecosphere.platform.support.IgnoreProperties;
import de.iip_ecosphere.platform.support.Include;
import de.iip_ecosphere.platform.support.json.IOIterator;
import de.iip_ecosphere.platform.support.json.Json.EnumMapping;
import de.iip_ecosphere.platform.support.json.JsonIgnore;
import de.iip_ecosphere.platform.support.json.JsonProperty;

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
     * @param introspector the current introspector, may be <b>null</b>
     * @param cls the class to be configured
     * @return the configuring annotation introspector
     */
    public static OktoAnnotationIntrospector configureFor(ObjectMapper mapper, 
        OktoAnnotationIntrospector introspector, Class<?> cls) {
        IgnoreProperties annIgnoreProp = cls.getAnnotation(IgnoreProperties.class);
        JsonIgnoreProperties jsonIgnoreProp = cls.getAnnotation(JsonIgnoreProperties.class);
        Map<String, String> renames = new HashMap<>();
        Set<String> nonNullInclude = new HashSet<>();
        if (null != annIgnoreProp && annIgnoreProp.ignoreUnknown()) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        if (null != jsonIgnoreProp && jsonIgnoreProp.ignoreUnknown()) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        for (Field f : cls.getDeclaredFields()) {
            handleAnnotations(f.getName(), f, renames, nonNullInclude);
        }
        for (Method m : cls.getDeclaredMethods()) {
            String name = m.getName();
            if (name.length() > 3 && (name.startsWith("get") || name.startsWith("set"))) {
                name = name.substring(3);
            }
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            handleAnnotations(name, m, renames, nonNullInclude);
        }
        if (nonNullInclude.size() > 0) {
            SimpleModule module = new SimpleModule();
            module.setSerializerModifier(new CustomPropertyInclusionModifier(cls, nonNullInclude));
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
        return setAnnotationIntrospector(mapper, introspector, null);
    }
    
    /**
     * Basic annotation introspector for abstracting oktoflow data annotations, in particular {@link ConfiguredName} 
     * and {@link Ignore}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class OktoAnnotationIntrospector extends JacksonAnnotationIntrospector {

        private static final long serialVersionUID = -1021095562978855964L;
        private Set<String> exclusions;
        private Set<Object> ignore;

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember member) {
            Ignore ignoreAnn = member.getAnnotation(Ignore.class);
            if (null != ignoreAnn) {
                return ignoreAnn.value();
            }
            JsonIgnore jsonIgnore = member.getAnnotation(JsonIgnore.class); // TODO remove
            if (null != jsonIgnore) {
                return jsonIgnore.value();
            }
            if (exclusions != null) {
                boolean exclude = exclusions.contains(member.getName());
                if (!exclude) {
                    ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
                    if (null != cfgName && cfgName.value() != null) {
                        exclude = exclusions.contains(cfgName.value());
                    }
                }
                if (!exclude) {
                    JsonProperty jsonProp = member.getAnnotation(JsonProperty.class);
                    if (null != jsonProp && jsonProp.value() != null) {
                        exclude = exclusions.contains(jsonProp.value());
                    }
                }
                return exclude;
            }
            if (null != ignore) {
                return ignore.contains(member.getType().getRawClass()) || ignore.contains(member.getMember());
            }
            return super.hasIgnoreMarker(member);
        }
        
        @Override
        public PropertyName findNameForDeserialization(Annotated member) {
            ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
            if (cfgName != null) {
                return PropertyName.construct(cfgName.value());
            } else {                
                return super.findNameForDeserialization(member);
            }
        }
    
        @Override
        public PropertyName findNameForSerialization(Annotated member) {
            ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
            if (cfgName != null) {
                return PropertyName.construct(cfgName.value());
            } else {                
                return super.findNameForSerialization(member);
            }
        }
        
        @Override
        public JsonIgnoreProperties.Value findPropertyIgnorals(Annotated member) {
            IgnoreProperties ignoreProp = member.getAnnotation(IgnoreProperties.class);
            if (ignoreProp == null) {
                return JsonIgnoreProperties.Value.empty();
            }
            return super.findPropertyIgnorals(member);
        }
        
        @Override
        public Object findFilterId(Annotated member) {
            Filter filter = member.getAnnotation(Filter.class);
            if (filter != null) {
                String id = filter.value();
                // Empty String is same as not having annotation, to allow overrides
                if (id.length() > 0) {
                    return id;
                }
            }
            return super.findFilterId(member);
        }
        
    }
    
    /**
     * Collects data on supported annotations ({@link ConfiguredName}, {@link JsonProperty} {@link Include}) for an 
     * accessible object.
     * 
     * @param propName the property name
     * @param obj the accessible object
     * @param renames the property renamings (original name, new name)
     * @param nonNullInclude the properties to not include if their value is <b>null</b>
     */
    private static void handleAnnotations(String propName, AccessibleObject obj, Map<String, String> renames, 
        Set<String> nonNullInclude) {
        ConfiguredName cfgName = obj.getAnnotation(ConfiguredName.class);
        JsonProperty annProp = obj.getAnnotation(JsonProperty.class);
        Include annIncl = obj.getAnnotation(Include.class);
        if (null != cfgName && isRename(cfgName.value(), propName)) {
            renames.put(propName, cfgName.value());
        } else if (null != annProp && isRename(annProp.value(), propName)) {
            renames.put(propName, annProp.value());
        }
        if (null != annIncl && annIncl.value() == Include.Type.NON_NULL) {
            nonNullInclude.add(propName);
        }
    }

    /**
     * Returns whether the specified configured name {@code cfgName} represents a renaming of {@code propName}.
     * 
     * @param cfgName the configured name
     * @param propName the property name
     * @return {@code true} for renaming, {@code false} for no renaming
     */
    private static boolean isRename(String cfgName, String propName) {
        return cfgName != null && cfgName.length() > 0 && !propName.equals(cfgName);
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
     * Sets the annotation introspector on {@code mapper}.
     * 
     * @param mapper the mapper; the introspector is only set if {@code introspector} was <b>null</b> before, else
     *    the already set introspector may be reconfigured through {@code configurer}
     * @param introspector the actual introspector, may be <b>null</b>
     * @param configurer the configurer function, may be <b>null</b> for none
     * @return the actual introspector (new if <b>null</b> before or reconfigured)
     */
    private static OktoAnnotationIntrospector setAnnotationIntrospector(ObjectMapper mapper, 
        OktoAnnotationIntrospector introspector, Consumer<OktoAnnotationIntrospector> configurer) {
        if (null == introspector) {
            introspector = new OktoAnnotationIntrospector();
            mapper.setAnnotationIntrospector(introspector);
        }
        if (null != configurer) {
            configurer.accept(introspector);
        }
        return introspector;
    }
    
    /**
     * Returns an object writer for a mapper that applies a filter on {@code fieldNames} to be excluded.
     * 
     * @param mapper the mapper
     * @param introspector the current introspector, may be <b>null</b>
     * @param fieldNames the field names
     * @return the actual introspector (new if <b>null</b> before or reconfigured)
     */
    public static OktoAnnotationIntrospector exceptFields(ObjectMapper mapper, OktoAnnotationIntrospector introspector,
        String... fieldNames) {
        final Set<String> exclusions = CollectionUtils.addAll(new HashSet<String>(), fieldNames);
        return setAnnotationIntrospector(mapper, introspector, i -> i.exclusions = exclusions);
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
     * @param introspector the current introspector, may be <b>null</b>
     * @param ignore, classes (also as return types) and (reflection) fields that shall be ignored
     * @return the actual introspector (new if <b>null</b> before or reconfigured)
     */
    public static OktoAnnotationIntrospector configureLazy(ObjectMapper mapper, 
        OktoAnnotationIntrospector introspector, Set<Object> ignore) {
        OktoAnnotationIntrospector result = introspector;
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // may become empty through ignores
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL, 
            JsonTypeInfo.As.WRAPPER_ARRAY);
        if (!ignore.isEmpty()) {
            result = setAnnotationIntrospector(mapper, result, i -> i.ignore = ignore);
        }
        return result;
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
     * Property exclusion modifier to simulate {@link IgnoreProperties}, {@link JsonIgnoreProperties}.
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
     * Property exclusion modifier to simulate {@link Include}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomPropertyInclusionModifier extends BeanSerializerModifier {

        private final Set<String> nonNullInclude;
        private final Class<?> targetClass;

        /**
         * Creates an instance.
         * 
         * @param targetClass the target class
         * @param nonNullInclude the properties that shall not included if null, ignored if <b>null</b> 
         */
        public CustomPropertyInclusionModifier(Class<?> targetClass, Set<String> nonNullInclude) {
            this.targetClass = targetClass;
            this.nonNullInclude = nonNullInclude;
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
                    if (nonNullInclude != null && nonNullInclude.contains(writer.getName())) {
                        newProperties.add(new JsonNullableBeanPropertyWriter(writer));
                    } else {
                        newProperties.add(writer);
                    }
                }
                return newProperties;
            }
            return super.changeProperties(config, beanDesc, beanProperties);
        }
        
    }

    /**
     * Writer to prevent writing null properties if disabled by {@link Include}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class JsonNullableBeanPropertyWriter extends BeanPropertyWriter {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a property bean writer based on the given one.
         * 
         * @param base the base writer
         */
        protected JsonNullableBeanPropertyWriter(BeanPropertyWriter base) {
            super(base);
        }

        /**
         * Creates a property bean writer based on the given one and the property name.
         * 
         * @param base the base writer
         * @param newName the new property name
         */
        protected JsonNullableBeanPropertyWriter(BeanPropertyWriter base, PropertyName newName) {
            super(base, newName);
        }

        @Override
        protected BeanPropertyWriter _new(PropertyName newName) {
            return new JsonNullableBeanPropertyWriter(this, newName);
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception {
            Object value = get(bean);
            if (value == null) {
                return;
            }
            super.serializeAsField(bean, jgen, prov);
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
