package test.de.iip_ecosphere.platform.security.services.kodex;

import java.io.IOException;

import org.json.simple.JSONObject;

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * In-data JSON type translator.
 * 
 * @author Holger Eichelberger, SSE
 */
class InDataJsonTypeTranslator implements TypeTranslator<InData, String> {

    @Override
    public InData from(String data) throws IOException {
        return null; // shall be filled, not needed here
    }

    @SuppressWarnings("unchecked")
    @Override
    public String to(InData source) throws IOException {
        JSONObject json = new JSONObject();
        json.put("name", source.getName());
        json.put("id", source.getId());
        return json.toJSONString();
    }
    
}