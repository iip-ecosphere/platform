package de.iip_ecosphere.platform.transport.mqttv3;

import de.iip_ecosphere.platform.transport.DefaultTransportFactoryDescriptor;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * The factory descriptor for this connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV3TransportConnectorFactoryDescriptor extends DefaultTransportFactoryDescriptor {

    public static final ConnectorCreator MAIN = new ConnectorCreator() {

        @Override
        public TransportConnector createConnector() {
            return new PahoMqttV3TransportConnector();
        }

        @Override
        public String getName() {
            return PahoMqttV3TransportConnector.NAME;
        }
        
    };
    
    @Override
    public ConnectorCreator getMainCreator() {
        return MAIN;
    }

}
