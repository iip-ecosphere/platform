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
        
        if (data instanceof Convertable[]) {

            Convertable[] con = (Convertable[]) data;
            data = con[0].fromREST(data);

        } else if (data instanceof Convertable) {
            
            Convertable con = (Convertable) data;
            data = con.fromREST(data);
        }

        return data;
    }
}
