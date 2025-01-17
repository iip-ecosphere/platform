package test.de.iip_ecosphere.platform.services.environment.pythonEnv;

import java.io.IOException;
import de.iip_ecosphere.platform.transport.serialization.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * JSON transport serializer for Rec13.
 * Generated by: EASy-Producer.
 */
public class Rec13InTranslator implements TypeTranslator<Rec13, String> {

    @Override             
    public Rec13 from(String data) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(data, Rec13Impl.class);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    @Override    
    public String to(Rec13 source) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

}

