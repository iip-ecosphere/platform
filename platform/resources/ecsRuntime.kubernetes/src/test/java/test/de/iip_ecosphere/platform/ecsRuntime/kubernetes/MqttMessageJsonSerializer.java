package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.MqttMessage;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import test.de.iip_ecosphere.platform.transport.JsonUtils;

public class MqttMessageJsonSerializer implements Serializer<MqttMessage> {

    @Override
    public MqttMessage from(byte[] data) throws IOException {
        MqttMessage result;
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(new String(data));
            result = new MqttMessage(JsonUtils.readString(obj, "streamId"), JsonUtils.readString(obj, "messageTxt"));
        } catch (ParseException e) {
            throw new IOException(e.getMessage(), e);
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage(), e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] to(MqttMessage value) throws IOException {
        JSONObject json = new JSONObject();
        json.put("streamId", value.getStreamId());
        json.put("messageTxt", value.getMessageTxt());
        return json.toJSONString().getBytes();
    }

    @Override
    public MqttMessage clone(MqttMessage origin) throws IOException {
        return new MqttMessage(origin.getStreamId(), origin.getMessageTxt());
    }

    @Override
    public Class<MqttMessage> getType() {
        return MqttMessage.class;
    }

}
