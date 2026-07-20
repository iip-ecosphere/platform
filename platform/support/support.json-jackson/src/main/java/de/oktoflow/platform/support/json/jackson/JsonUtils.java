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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.ConfiguredName;
import de.iip_ecosphere.platform.support.IgnoreProperties;
import de.iip_ecosphere.platform.support.Include;
import de.iip_ecosphere.platform.support.json.IOIterator;
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
        return OktoAnnotationIntrospector.set(introspector, i-> mapper.setAnnotationIntrospector(i), null);
    }
    
    /**
     * Collects data on supported annotations ({@link ConfiguredName}) for an accessible object.
     * 
     * @param propName the property name
     * @param obj the accessible object
     * @param renames the property renamings (original name, new name)
     * @param nonNullInclude the properties to not include if their value is <b>null</b>
     */
    private static void handleAnnotations(String propName, AccessibleObject obj, Map<String, String> renames, 
        Set<String> nonNullInclude) {
        ConfiguredName cfgName = obj.getAnnotation(ConfiguredName.class);
        Include annIncl = obj.getAnnotation(Include.class);
        if (null != cfgName && isRename(cfgName.value(), propName)) {
            renames.put(propName, cfgName.value());
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
     * Creates a module for handling IIP data/implementation classes according to IIP conventions.
     * 
     * @return the module
     */
    public static SimpleModule createIipDataClassesModule() {
        SimpleModule iipModule = new SimpleModule();
        iipModule.setAbstractTypes(IIP_TYPE_RESOLVER);
        return iipModule;
    }
    
    /**
     * Configures a Jackson object mapper for IIP conventions.
     * 
     * @param mapper the mapper to be configured
     * @return {@code mapper}
     */
    public static ObjectMapper handleIipDataClasses(ObjectMapper mapper) {
        return mapper.registerModule(createIipDataClassesModule());
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
        return OktoAnnotationIntrospector.set(introspector, i-> mapper.setAnnotationIntrospector(i), 
            i -> i.setExclusions(exclusions));
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
            result = OktoAnnotationIntrospector.set(result, i-> mapper.setAnnotationIntrospector(i), 
                i -> i.setIgnore(ignore));
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
