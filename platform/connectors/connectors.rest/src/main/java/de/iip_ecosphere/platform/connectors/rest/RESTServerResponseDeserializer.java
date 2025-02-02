package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;


public class RESTServerResponseDeserializer<T1 extends RESTServerResponse, T2>
        extends JsonDeserializer<T1> {

    private Class<T1> responseClass;

    /**
     * Constructor.
     * 
     * @param targetType for Json deserialization
     */
    protected RESTServerResponseDeserializer(Class<T1> responseClass) {
        this.responseClass = responseClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T1 deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);
        T1 response = null;

        try {
          
            response = responseClass.getDeclaredConstructor().newInstance();
            Iterator<String> iter = node.fieldNames();
            
            int itemClassIndex = 0;
            
            while (iter.hasNext()) {
                String fieldName = iter.next();
                
                JsonNode innerNode = node.get(fieldName);
                
                //System.out.println("Feldname: " + fieldName + " = " + node.get(fieldName).asText());
                
                if (innerNode.isArray()) {
                    
                    T2[] items = (T2[]) new Object[innerNode.size()];
                    
                    int itemsIndex = 0;
                    
                    for (JsonNode itemNode : innerNode) {
                        T2 item = (T2) jp.getCodec().treeToValue(itemNode, response.getItemClass()[itemClassIndex]);
                        items[itemsIndex] = item;
                        
                        itemsIndex++;
                        
                    }
                    itemClassIndex++;
                    
                    response.set(fieldName, items);
                    
                } else {
                    
                    if (innerNode.isTextual()) {
                        response.set(fieldName, node.get(fieldName).asText());
                    } else {
                        response.set(fieldName, node.get(fieldName).numberValue());
                    }

                }
                
            }
            
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }        

        return response;
    }

}
