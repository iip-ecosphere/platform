package de.iip_ecosphere.platform.transport.connectors.impl;

/**
 * MQTT quality levels. Included as own class as so far non quality definition
 * was found in the dependencies.
 * 
 * @author Holger Eichelberger, SSE
 */
enum MqttQoS {
    
    AT_MOST_ONCE(0), 
    AT_LEAST_ONCE(1), 
    EXACTLY_ONCE(2), 
    FAILURE(0x80);

    private final int value;

    /**
     * Creates a QoS constant.
     * 
     * @param value the MQTT level value
     */
    MqttQoS(int value) {
        this.value = value;
    }

    /**
     * Returns the QoS value.
     * 
     * @return the QoS value as int
     */
    public int value() {
        return value;
    }

    /**
     * Returns the QoS constant for a given value.
     * 
     * @param value the level value to map into a constant
     * @return the corresponding constant
     * @throws IllegalArgumentException if no QoS level can be found
     */
    public static MqttQoS valueOf(int value) {
        MqttQoS result = null;
        for (MqttQoS q : values()) {
            if (q.value == value) {
                result = q;
            }
        }
        if (null == result) {
            throw new IllegalArgumentException("invalid QoS: " + value);
        }
        return result;
    }
}
