package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.AbstractChannelConnector;
import de.iip_ecosphere.platform.connectors.AbstractPluginChannelConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ChannelAdapterSelector;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;

/**
 * Implements a testing connector. However, we have to "emulate" the underlying protocol
 * in the same class so that testing can ingest/access the data.
 * 
 * @param <CO> the connector output type
 * @param <CI> the connector input type
 * 
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = false, supportsEvents = false, supportsHierarchicalQNames = false, 
    supportsModelCalls = false, supportsModelProperties = false, supportsModelStructs = false, specificSettings = {})
public class MyChannelConnector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {

    public static final String NAME = "MyChannelConnector";
    private Deque<byte[]> offers = new LinkedBlockingDeque<byte[]>();
    private Deque<byte[]> received = new LinkedBlockingDeque<byte[]>();

    /**
     * The descriptor of this connector (see META-INF/services).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor extends AbstractPluginChannelConnectorDescriptor<byte[], byte[]> {

        @Override
        public String getName() {
            return MyChannelConnector.NAME;
        }

        @Override
        public Class<?> getConnectorType() {
            return MyModelConnector.class;
        }

        @Override
        protected String initId(String id) {
            return PLUGIN_TEST_ID_PREFIX + "myChannelConnector";
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <O, I, CO, CI, S extends ChannelAdapterSelector<byte[], byte[], CO, CI>, 
            A extends ChannelProtocolAdapter<byte[], byte[], CO, CI>> Connector<byte[], byte[], CO, CI> 
            createConnectorImpl(S selector, Supplier<ConnectorParameter> params, A... adapter) {
            return new MyChannelConnector<CO, CI>(adapter);
        }
        
    }
    
    /**
     * Creates a channel connector instance.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public MyChannelConnector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        super(adapter);
    }

    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        installPollTask();
    }

    @Override
    public void disconnectImpl() throws IOException {
        //uninstallPollTask(); happens automatically
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void writeImpl(byte[] data, String channel) throws IOException {
        received.offer(data);
    }

    @Override
    protected byte[] read() throws IOException {
        return offers.pollFirst();
    }

    @Override
    protected void error(String message, Throwable th) {
        System.out.println();
    }
    
    // for testing
    
    /**
     * Offer something to the connector, i.e., play the protocol.
     * 
     * @param data the data
     */
    public void offer(byte[] data) {
        offers.offer(data);
    }
    
    /**
     * Polls a received data element.
     * 
     * @return the received element, <b>null</b> for nothing received
     */
    public byte[] pollReceived() {
        return received.poll();
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
