package test.de.iip_ecosphere.platform.transport.spring;

import java.io.IOException;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;

/**
 * Just for testing the Spring configuration.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeTransportConnector2 implements TransportConnector {

    @Override
    public void syncSend(String stream, Object data) throws IOException {
    }

    @Override
    public void asyncSend(String stream, Object data) throws IOException {
    }

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
    }

    @Override
    public String composeStreamName(String parent, String name) {
        return null;
    }

    @Override
    public void connect(TransportParameter params) throws IOException {
    }

    @Override
    public void disconnect() throws IOException {
    }
    
    @Override
    public String getName() {
        return "Fake 2";
    }

    @Override
    public String supportedEncryption() {
        return null;
    }

    @Override
    public String enabledEncryption() {
        return null;
    }

}
