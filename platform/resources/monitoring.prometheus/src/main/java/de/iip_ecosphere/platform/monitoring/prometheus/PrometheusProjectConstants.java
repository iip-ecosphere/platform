package de.iip_ecosphere.platform.monitoring.prometheus;

/**
 * Class to with constant values for config purposes on test setup.
 */
public class PrometheusProjectConstants {
    public static final String PROMETHEUSSERVERIP = "192.168.2.118";
    public static final String PROMETHEUSPUSHGATEWAYIP = "192.168.2.118";
    public static final int PROMETHEUSSERVERPORT = 9090;
    public static final int PROMETHEUSPUSHGATEWAYPORT = 9400;
    
    //Hivemq test server
    public static final String HIVEMQSERVERIP = "192.168.2.101";
    public static final String HIVEMQMQTTBROKERIP = "192.168.2.101";
    public static final int HIVEMQSERVERPORT = 9321;
    public static final int HIVEMQMQTTBROKERPORT = 1883;
}
