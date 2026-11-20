package de.oktoflow.platform.tools.energyAggregator.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;

/**
 * MQTT source for the Shelly Pro 3EM. Accepts the Gen2 field names ({@code total_act_power}, {@code total_act}) 
 * and, as fallback, the Gen1 names ({@code power}, {@code total}). A message may contain only power or only
 * energy; the parser checks for the fields, not for the topic. Reconnects automatically after connection losses.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class ShellyMqttIngestor {

    /**
     * Connection timeout in seconds.
     */
    private static final int CONNECTION_TIMEOUT = 10;

    /**
     * Keep-alive interval in seconds.
     */
    private static final int KEEP_ALIVE_INTERVAL = 60;

    /**
     * The logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ShellyMqttIngestor.class);

    /**
     * The JSON parser.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The source (meter) id.
     */
    private final String sourceId;

    /**
     * The device (machine) id.
     */
    private final String deviceId;

    /**
     * The broker URL, e.g., "tcp://localhost:1883".
     */
    private final String brokerUrl;

    /**
     * The MQTT client id.
     */
    private final String clientId;

    /**
     * The topic (filter) to subscribe to.
     */
    private final String topic;

    /**
     * The MQTT quality of service.
     */
    private final int qos;

    /**
     * Receives the measurements.
     */
    private final Consumer<MachineEnergyMeasurement> consumer;

    /**
     * Receives errors, may be <b>null</b>.
     */
    private final Consumer<Throwable> errorConsumer;

    /**
     * The MQTT client, <b>null</b> if not connected.
     */
    private MqttClient client;

    /**
     * Whether received messages are processed.
     */
    private volatile boolean running;

    // checkstyle: stop parameter number check

    /**
     * Creates a Shelly source.
     * 
     * @param sourceId the source (meter) id, e.g., "ShellyPro3EM"
     * @param deviceId the device (machine) id, e.g., "LatheNEF400"
     * @param brokerUrl the broker URL
     * @param clientId the MQTT client id
     * @param topic the topic (filter) to subscribe to
     * @param qos the MQTT quality of service
     * @param consumer receives the measurements
     * @param errorConsumer receives errors, may be <b>null</b>
     */
    public ShellyMqttIngestor(String sourceId, String deviceId, String brokerUrl, String clientId, String topic,
        int qos, Consumer<MachineEnergyMeasurement> consumer, Consumer<Throwable> errorConsumer) {
        this.sourceId = sourceId;
        this.deviceId = deviceId;
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.consumer = consumer;
        this.errorConsumer = errorConsumer;
    }

    // checkstyle: resume parameter number check

    /**
     * Connects to the broker and subscribes to the topic, also after automatic reconnects.
     * 
     * @throws MqttException if connecting fails
     */
    public void connect() throws MqttException {
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setConnectionTimeout(CONNECTION_TIMEOUT);
        opts.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        client.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverUri) {
                LOG.info("[{}] MQTT connected (reconnect={}) to {}", deviceId, reconnect, serverUri);
                try {
                    client.subscribe(topic, qos, (tpc, msg) -> handleMessage(msg));
                } catch (MqttException e) {
                    notifyError(e);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                LOG.warn("[{}] MQTT connection lost: {}", deviceId, cause.getMessage());
                notifyError(cause);
            }

            @Override
            public void messageArrived(String tpc, MqttMessage msg) {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }

        });
        client.connect(opts);
    }

    /**
     * Starts processing received messages.
     */
    public void start() {
        running = true;
    }

    /**
     * Stops processing and disconnects from the broker.
     */
    public void disconnect() {
        running = false;
        if (null != client && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                LOG.warn("[{}] MQTT disconnect failed: {}", deviceId, e.getMessage());
            }
        }
    }

    /**
     * Returns whether the client is connected to the broker.
     * 
     * @return {@code true} if connected, {@code false} else
     */
    public boolean isConnected() {
        return null != client && client.isConnected();
    }

    /**
     * Handles a received message.
     * 
     * @param msg the message
     */
    private void handleMessage(MqttMessage msg) {
        if (running) {
            try {
                for (MachineEnergyMeasurement m : parse(new String(msg.getPayload(), StandardCharsets.UTF_8))) {
                    consumer.accept(m);
                }
            } catch (IOException e) {
                LOG.warn("[{}] Failed to parse Shelly payload: {}", deviceId, e.getMessage());
                notifyError(e);
            }
        }
    }

    /**
     * Parses a Shelly status payload. Gen2 names are preferred, Gen1 names are accepted as fallback.
     * 
     * @param json the payload
     * @return the measurements, empty if the payload is valid JSON but contains none of the fields
     * @throws IOException if the payload is not valid JSON
     */
    public List<MachineEnergyMeasurement> parse(String json) throws IOException {
        JsonNode node = MAPPER.readTree(json);
        Instant now = Instant.now();
        List<MachineEnergyMeasurement> result = new ArrayList<>();
        String powerField = firstPresent(node, "total_act_power", "power");
        if (null != powerField) {
            result.add(build(MeasurementType.CURRENT_POWER, node.get(powerField).asDouble(), "W", now));
        }
        String energyField = firstPresent(node, "total_act", "total");
        if (null != energyField) {
            result.add(build(MeasurementType.CUMULATIVE_ENERGY, node.get(energyField).asDouble(), "Wh", now));
        }
        return result;
    }

    /**
     * Returns the first field name present in {@code node}.
     * 
     * @param node the JSON node
     * @param names the field names in order of preference
     * @return the first present field name, <b>null</b> if none is present
     */
    private static String firstPresent(JsonNode node, String... names) {
        String result = null;
        for (int n = 0; null == result && n < names.length; n++) {
            if (node.has(names[n])) {
                result = names[n];
            }
        }
        return result;
    }

    /**
     * Creates a valid measurement of this source.
     * 
     * @param type the measurement type
     * @param value the value
     * @param unit the unit
     * @param timestamp the timestamp
     * @return the measurement
     */
    private MachineEnergyMeasurement build(MeasurementType type, double value, String unit, Instant timestamp) {
        return new MachineEnergyMeasurement(sourceId, deviceId, type, value, unit, timestamp, 
            MeasurementQuality.VALID);
    }

    /**
     * Passes an error to the error consumer, if present.
     * 
     * @param error the error
     */
    private void notifyError(Throwable error) {
        if (null != errorConsumer) {
            errorConsumer.accept(error);
        }
    }

}
