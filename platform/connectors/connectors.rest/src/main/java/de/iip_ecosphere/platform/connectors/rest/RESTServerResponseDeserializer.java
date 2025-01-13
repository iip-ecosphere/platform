package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class RESTServerResponseDeserializer<T1 extends RESTServerResponse, T2 extends RESTServerResponseValue>
        extends JsonDeserializer<T1> {

    private Class<T1> responseClass;
    private Class<T2> valueClass;

    /**
     * Constructor.
     * 
     * @param targetType for Json deserialization
     */
    protected RESTServerResponseDeserializer(Class<T1> responseClass, Class<T2> valueClass) {
        this.responseClass = responseClass;
        this.valueClass = valueClass;
    }

    @Override
    public T1 deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);

        T1 response = null;

        try {
            response = responseClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        if (response instanceof RESTServerResponseSingle && node.has("value")) {
            // Deserialize a single value
            JsonNode valueNode = node.get("value");
            T2 value = jp.getCodec().treeToValue(valueNode, valueClass);
            ((RESTServerResponseSingle) response).setValue(value);
        } else if (response instanceof RESTServerResponseSet && node.has("values")) {
            // Deserialize a list of values
            JsonNode valuesNode = node.get("values");

            ArrayList<RESTServerResponseValue> values = new ArrayList<>();

            for (JsonNode valueNode : valuesNode) {
                T2 value = jp.getCodec().treeToValue(valueNode, valueClass);
                values.add(value); // Add as ServerResponseValue
            }
            ((RESTServerResponseSet) response).setValues(values);
        }

        return response;

    }

}
