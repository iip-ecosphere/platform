package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelOutputConverter;

/**
 * So far only needed to get a public constructor.
 * 
 * @author Christian Nikolajew
 */
public class RESTOutputConverter extends ModelOutputConverter {
    
    /**
     * Creates an instance.
     */
    public RESTOutputConverter() {
        
    }
    
    @Override
    public Object fromObject(Object data) throws IOException {
        
        if (data instanceof Convertable[]) {
            
            Convertable[] con = (Convertable[]) data;
            data = con[0].toREST(data);
            
        } else  if (data instanceof Convertable) {
            
            Convertable con = (Convertable) data;
            data = con.toREST(data);
        }

        return data;
    }

}
