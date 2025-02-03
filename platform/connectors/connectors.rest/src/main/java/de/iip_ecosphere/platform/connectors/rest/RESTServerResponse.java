package de.iip_ecosphere.platform.connectors.rest;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RESTServerResponse {

    /**
     * Setter for Class attributes.
     * 
     * @param key = name of the attribute to set
     * @param value to set for the attribute
     */
    @SuppressWarnings("unchecked")
    public void set(String key, Object value) {
        
        try {
            Field field = this.getClass().getDeclaredField(key);
            field.setAccessible(true);
            
            if (field.getType().isArray() && value instanceof Object[]) {
                Object[] objArray = (Object[]) value;
                //Class<?> componentType = field.getType().getComponentType();
                Object array = Arrays.copyOf(objArray, objArray.length, (Class<? extends Object[]>) field.getType());
                field.set(this, array);
            } else {
                field.set(this, value);
            }
           
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Getter for Class attribute value. Returns null if the Class has no simple value attribute.
     * 
     * @return value or null
     */
    public  abstract Object getValue();
    
    /**
     * Returns the specific inner Item Classes of RESTServerResponse as Array. If
     * RESTServerResponse don't have a inner Item Classes null is returned.
     * 
     * @param <T2> the specific inner ItemClass
     * @return the specific inner ItemClass or null
     */
    protected abstract Class<?>[] getItemClass();
}
