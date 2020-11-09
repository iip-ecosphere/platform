package de.iip_ecosphere.platform.transport.connectors.impl;

/**
 * An abstract MQTT transport connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractMqttTransportConnector extends AbstractTransportConnector {

    @Override
    public String composeStreamName(String parent, String name) {
        return parent != null && parent.length() > 0 ? parent + "/" + name : name;
    }

}
