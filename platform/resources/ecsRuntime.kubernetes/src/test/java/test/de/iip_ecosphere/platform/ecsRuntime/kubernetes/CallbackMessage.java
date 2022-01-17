package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.util.concurrent.ConcurrentLinkedDeque;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.TransportMessage;
import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;

public class CallbackMessage extends AbstractReceptionCallback<TransportMessage> {

//    private MqttMessage data;
    private ConcurrentLinkedDeque<TransportMessage> dataDeque = new ConcurrentLinkedDeque<TransportMessage>();

    /**
     * Creates the callback instance.
     */
    protected CallbackMessage() {
        super(TransportMessage.class);
    }

    /**
     * Returns the MQTT message.
     * 
     * @return the MqttMessage
     */
    public TransportMessage getData() {
        
        return dataDeque.removeFirst();
    }

    /**
     * Set the MQTT message.
     *
     * @param data the MQTT message
     */
    public void setData(TransportMessage data) {
        this.dataDeque.addLast(data);
//        this.data = data;
    }

    @Override
    public void received(TransportMessage data) {
        this.dataDeque.addLast(data);
//      this.data = data;
    }
    
    /**
     * Check if the MQTT messages queue is empty.
     *
     * @return the MQTT messages queue element check
     */
    public boolean dequeIsEmpty() {
        return this.dataDeque.isEmpty();
    }

}