package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

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
            result = new OutData((int) JsonUtils.readDouble(obj, "id", 0), // double??
                JsonUtils.readDouble(obj, "value1", 0), 
                JsonUtils.readDouble(obj, "value2", 0),
                JsonUtils.readDouble(obj, "confidence", 0),
                Boolean.valueOf(JsonUtils.readString(obj, "prediction")));
        } catch (ParseException e) {
            throw new IOException(e.getMessage(), e);
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage(), e);
        }
        return result;
    }
    
}