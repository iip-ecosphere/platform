package de.oktoflow.platform.support.json.jackson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

/**
 * A property naming strategy exactly using the given names as JSON and Java field/getter/setter names.
 * Applies a fallback strategy if there is no mapping.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MappingPropertyNamingStrategy extends PropertyNamingStrategy {
    
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
    
    /**
     * Creates a property naming strategy, i.e., a mapping so that Java field names receive exactly the given names.
     * 
     * @param fieldNames the field names (names of JSON/Java fields)
     * @return the naming strategy
     */
    public static MappingPropertyNamingStrategy createFor(String... fieldNames) {
        Map<String, String> mapping = new HashMap<>();
        for (String fn : fieldNames) {
            String javaField = fn;
            if (javaField.length() > 0) {
                javaField = Character.toUpperCase(javaField.charAt(0)) + javaField.substring(1);
            }
            mapping.put(javaField, fn);
        }
        return new MappingPropertyNamingStrategy(mapping);        
    }
    
}