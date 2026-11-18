package de.iip_ecosphere.platform.examples.energy.aggregator.source;

/**
 * Configuration for a single source. Loaded from YAML or IVML-generated
 * deployment configuration.
 */
public class SourceConfig {

    private String sourceId;
    private String protocol;         // MQTT, MODBUS_TCP, OPC_UA
    private String host;
    private int port;
    private int intervalMs;
    private String topic;            // MQTT only
    private int unitId;              // Modbus only
    private String username;         // optional
    private String password;         // optional
    private boolean enabled = true;

    // getters and setters ...
}
