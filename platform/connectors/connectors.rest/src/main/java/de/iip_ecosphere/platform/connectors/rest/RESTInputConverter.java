package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;

/**
 * So far only needed to get a public constructor.
 * 
 * @author Christian Nikolajew
 */
public class RESTInputConverter extends ModelInputConverter {

    /**
     * Creates an instance.
     */
    public RESTInputConverter() {
        
    }
    
    @Override
    public Object toObject(Object data) throws IOException {
        
        if (data instanceof Convertable) {
            data = ((Convertable) data).fromREST(data);
        }
        System.out.println(data);
        return data;
    }
}
