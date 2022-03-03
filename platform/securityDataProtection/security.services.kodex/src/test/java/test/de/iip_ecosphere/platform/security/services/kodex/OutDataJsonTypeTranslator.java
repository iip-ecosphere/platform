package test.de.iip_ecosphere.platform.security.services.kodex;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import test.de.iip_ecosphere.platform.transport.JsonUtils;

/**
 * Out-data JSON type translator.
 * 
 * @author Holger Eichelberger, SSE
 */
class OutDataJsonTypeTranslator implements TypeTranslator<String, OutData> {

    @Override
    public String from(OutData data) throws IOException {
        return null; // shall be filled, not needed here
    }

    @Override
    public OutData to(String source) throws IOException {
        OutData result;
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(source);
            result = new OutData(JsonUtils.readString(obj, "_kip"), 
                JsonUtils.readString(obj, "name"), 
                JsonUtils.readString(obj, "id"));
        } catch (ParseException e) {
            throw new IOException(e.getMessage(), e);
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage(), e);
        }
        return result;
    }
    
}