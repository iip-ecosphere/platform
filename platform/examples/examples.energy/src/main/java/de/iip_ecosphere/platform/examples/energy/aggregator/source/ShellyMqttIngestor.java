package de.iip_ecosphere.platform.examples.energy.aggregator.source;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * MQTT ingestor for the Shelly Pro 3EM.
 *
 * <p>Accepts both the Gen2 field names used by IFW's mapping
 * ({@code total_act_power}, {@code total_act}) and the Gen1 names
 * ({@code power}, {@code total}) so existing test messages keep working.</p>
 *
 * <p>One incoming MQTT message may contain only power or only energy; the
 * parser checks for the fields, not the topic, so both cases produce the
 * correct single measurement.</p>
 */
public class ShellyMqttIngestor {

    private static final Logger LOG = LoggerFactory.getLogger(ShellyMqttIngestor.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String sourceId;   // "ShellyPro3EM" — the meter
    private final String deviceId;   // "LatheNEF400" — the machine
    private final String brokerUrl;
    private final String clientId;
    private final String topic;
    private final int qos;
    private final Consumer<MachineEnergyMeasurement> consumer;
    private final Consumer<Throwable> errorConsumer;

    private MqttClient client;
    private volatile boolean running = false;

    public ShellyMqttIngestor(String sourceId,
                              String deviceId,
                              String brokerUrl,
                              String clientId,
                              String topic,
                              int qos,
                              Consumer<MachineEnergyMeasurement> consumer,
                              Consumer<Throwable> errorConsumer) {
        this.sourceId = sourceId;
        this.deviceId = deviceId;
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.consumer = consumer;
        this.errorConsumer = errorConsumer;
    }

    // -----------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------

    public void connect() throws MqttException {
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setConnectionTimeout(10);
        opts.setKeepAliveInterval(60);

        client.setCallback(new MqttCallbackExtended() {
            @Override public void connectComplete(boolean reconnect, String serverURI) {
                LOG.info("[{}] MQTT connected (reconnect={}) to {}", deviceId, reconnect, serverURI);
                try {
                    client.subscribe(topic, qos, (t, msg) -> handleMessage(msg));
                } catch (MqttException e) {
                    notifyError(e);
                }
            }
            @Override public void connectionLost(Throwable cause) {
                LOG.warn("[{}] MQTT connection lost", deviceId, cause);
                notifyError(cause);
            }
            @Override public void messageArrived(String t, MqttMessage msg) { }
            @Override public void deliveryComplete(IMqttDeliveryToken token) { }
        });

        client.connect(opts);
    }

    public void start() {
        running = true;
    }

    public void disconnect() {
        running = false;
        if (client != null && client.isConnected()) {
            try { client.disconnect(); } catch (MqttException e) { LOG.warn("disconnect failed", e); }
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    // -----------------------------------------------------------------
    // Parsing — package-private so it can be unit-tested without MQTT
    // -----------------------------------------------------------------

    private void handleMessage(MqttMessage msg) {
        if (!running) return;
        try {
            String payload = new String(msg.getPayload(), StandardCharsets.UTF_8);
            for (MachineEnergyMeasurement m : parse(payload)) {
                consumer.accept(m);
            }
        } catch (Exception e) {
            LOG.warn("[{}] Failed to parse Shelly payload", deviceId, e);
            notifyError(e);
        }
    }

    /**
     * Parses a Shelly status payload. Gen2 names are preferred; Gen1 names are
     * accepted as fallback. Returns an empty list if the JSON is valid but
     * contains neither pair of fields.
     *
     * @throws Exception if the payload is not valid JSON
     */
    List<MachineEnergyMeasurement> parse(String json) throws Exception {
        JsonNode node = MAPPER.readTree(json);
        Instant now = Instant.now();
        List<MachineEnergyMeasurement> out = new ArrayList<>(2);

        if (node.has("total_act_power")) {
            out.add(build(MeasurementType.CURRENT_POWER,
                node.get("total_act_power").asDouble(), "W", now));
        } else if (node.has("power")) {
            out.add(build(MeasurementType.CURRENT_POWER,
                node.get("power").asDouble(), "W", now));
        }

        if (node.has("total_act")) {
            out.add(build(MeasurementType.CUMULATIVE_ENERGY,
                node.get("total_act").asDouble(), "Wh", now));
        } else if (node.has("total")) {
            out.add(build(MeasurementType.CUMULATIVE_ENERGY,
                node.get("total").asDouble(), "Wh", now));
        }

        return out;
    }

    private MachineEnergyMeasurement build(MeasurementType type,
                                           double value,
                                           String unit,
                                           Instant timestamp) {
        return new MachineEnergyMeasurement(
            sourceId, deviceId, type, value, unit, timestamp, MeasurementQuality.VALID);
    }

    private void notifyError(Throwable t) {
        if (errorConsumer != null) errorConsumer.accept(t);
    }
}
