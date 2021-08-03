package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.util.concurrent.ConcurrentLinkedDeque;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.MqttMessage;
import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;

public class CallbackMessage extends AbstractReceptionCallback<MqttMessage> {

//    private MqttMessage data;
    private ConcurrentLinkedDeque<MqttMessage> dataDeque = new ConcurrentLinkedDeque<MqttMessage>();

    /**
     * Creates the callback instance.
     */
    protected CallbackMessage() {
        super(MqttMessage.class);
    }

    /**
     * Returns the MQTT message.
     * 
     * @return the MqttMessage
     */
    public MqttMessage getData() {
        
        return dataDeque.remove();
    }

    /**
     * Set the MQTT message.
     *
     * @param data the MQTT message
     */
    public void setData(MqttMessage data) {
        this.dataDeque.add(data);
//        this.data = data;
    }

    @Override
    public void received(MqttMessage data) {
        this.dataDeque.add(data);
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