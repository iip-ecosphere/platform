package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.TransportMessage;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import test.de.iip_ecosphere.platform.transport.JsonUtils;

public class TransportMessageJsonSerializer implements Serializer<TransportMessage> {

    @Override
    public TransportMessage from(byte[] data) throws IOException {
        TransportMessage result;
        
        Kryo tes = new Kryo();
        tes.register(TransportMessage.class);
        tes.register(byte[].class);
        
        Input in = new Input(data);
        result = tes.readObject(in, TransportMessage.class);
//        try {
//            JSONParser parser = new JSONParser();
//            JSONObject obj = (JSONObject) parser.parse(new String(data));
//            result = new TransportMessage(JsonUtils.readString(obj, "streamId"),
//                    JsonUtils.readString(obj, "messageTxt"), JsonUtils.readString(obj, "requestWatch"));
//        } catch (ParseException e) {
//            throw new IOException(e.getMessage(), e);
//        } catch (ClassCastException e) {
//            throw new IOException(e.getMessage(), e);
//        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] to(TransportMessage value) throws IOException {
//        JSONObject json = new JSONObject();
//        json.put("streamId", value.getStreamId());
//        json.put("messageTxt", value.getMessageTxt());
//        json.put("requestWatch", value.getRequestWatch());
//        return json.toJSONString().getBytes();
        
        Output out = new Output(10000);
        Kryo tes = new Kryo();
        tes.register(TransportMessage.class);
        tes.register(byte[].class);
        
        tes.writeObject(out, value);
        
        return out.getBuffer();
    }

    @Override
    public TransportMessage clone(TransportMessage origin) throws IOException {
        return new TransportMessage(origin.getStreamId(), origin.getMessageByte(), origin.getRequestWatch());
    }

    @Override
    public Class<TransportMessage> getType() {
        return TransportMessage.class;
    }

}
