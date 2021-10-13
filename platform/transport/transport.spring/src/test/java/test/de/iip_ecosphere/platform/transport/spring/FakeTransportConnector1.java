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
public class FakeTransportConnector1 implements TransportConnector {

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
        return "Fake 1";
    }

    /**
     * Returns the supported encryption mechanisms.
     * 
     * @return the supported encryption mechanisms, may be <b>null</b> or empty
     */
    public String supportedEncryption() {
        return null;
    }

    /**
     * Returns the actually enabled encryption mechanisms on this instance.
     * 
     * @return the enabled encryption mechanisms, may be <b>null</b> or empty
     */
    public String enabledEncryption() {
        return null;
    }

}
