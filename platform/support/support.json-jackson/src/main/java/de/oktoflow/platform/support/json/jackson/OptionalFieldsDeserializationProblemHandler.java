package de.oktoflow.platform.support.json.jackson;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

/**
 * A handler for optional fields.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OptionalFieldsDeserializationProblemHandler extends DeserializationProblemHandler {

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