package de.oktoflow.platform.tools.energyAggregator;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.oktoflow.platform.tools.energyAggregator.core.EnergyAggregator;
import de.oktoflow.platform.tools.energyAggregator.mapping.OpcEnergyModelMapper;
import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;
import de.oktoflow.platform.tools.energyAggregator.model.OpcUaQuality;
import de.oktoflow.platform.tools.energyAggregator.server.EnergyAggregatorServer;
import de.oktoflow.platform.tools.energyAggregator.server.UnknownTargetException;
import de.oktoflow.platform.tools.energyAggregator.source.SentronModbusIngestor;
import de.oktoflow.platform.tools.energyAggregator.source.ShellyMqttIngestor;

/**
 * Starts the energy aggregator: the OPC UA server and, if enabled via system properties, the Shelly (MQTT) and 
 * the SENTRON (ModbusTCP) source. Without enabled source, two example values are written to the server 
 * (server-only check). System properties (with defaults):
 * <ul>
 *   <li>{@code energyAggregator.host} ({@code 127.0.0.1}), {@code energyAggregator.port} ({@code 4840})</li>
 *   <li>{@code energyAggregator.mqtt.enabled} ({@code false}), {@code energyAggregator.mqtt.broker} 
 *       ({@code tcp://localhost:1883}), {@code energyAggregator.mqtt.topic} 
 *       ({@code shellies/shellypro3em/+/emeter/0})</li>
 *   <li>{@code energyAggregator.modbus.enabled} ({@code false}), {@code energyAggregator.modbus.host} 
 *       ({@code b7-lasertisch.ifw.uni-hannover.de}), {@code energyAggregator.modbus.port} ({@code 502})</li>
 * </ul>
 * 
 * @author Kodjo Gilles Adjroloh
 * @author Rhoda Ikhuohon-Eboreime
 */
public class EnergyAggregatorMain {

    /**
     * The machine fed by the Shelly source.
     */
    public static final String MACHINE_SHELLY = "LatheNEF400";

    /**
     * The machine fed by the SENTRON source.
     */
    public static final String MACHINE_SENTRON = "MarkingLaser";

    /**
     * Prefix of the system properties.
     */
    private static final String PROP_PREFIX = "energyAggregator.";

    /**
     * Default OPC UA server port.
     */
    private static final int DEFAULT_PORT = 4840;

    /**
     * Default Modbus port.
     */
    private static final int DEFAULT_MODBUS_PORT = 502;

    /**
     * SENTRON polling interval in ms.
     */
    private static final int MODBUS_INTERVAL_MS = 200;

    /**
     * Example power value of the server-only check in W.
     */
    private static final double CHECK_POWER = 1500.5;

    /**
     * Example energy value of the server-only check in Wh.
     */
    private static final double CHECK_ENERGY = 123456.7;

    /**
     * The logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnergyAggregatorMain.class);

    /**
     * The OPC UA server.
     */
    private final EnergyAggregatorServer server;

    /**
     * Keeps the latest measurement per device and type.
     */
    private final EnergyAggregator aggregator = new EnergyAggregator();

    /**
     * Maps measurements to OPC UA model updates.
     */
    private final OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

    /**
     * Creates an instance.
     * 
     * @param server the OPC UA server to write to
     */
    public EnergyAggregatorMain(EnergyAggregatorServer server) {
        this.server = server;
    }

    /**
     * Processes a measurement: aggregates it and, if it is newer than the known one, maps it and applies it to
     * the server.
     * 
     * @param measurement the measurement
     * @return {@code true} if the measurement was applied, {@code false} else
     */
    public boolean process(MachineEnergyMeasurement measurement) {
        boolean applied = false;
        try {
            if (aggregator.updateMeasurement(measurement)) {
                MappedModelUpdate update = mapper.map(measurement);
                server.applyUpdate(update);
                applied = true;
                LOG.info("Applied: device={}, target={}, value={}, unit={}", update.deviceId(), update.target(), 
                    update.value(), update.unit());
            } else {
                LOG.debug("Ignoring older/duplicate: device={}, type={}, ts={}", measurement.deviceId(), 
                    measurement.measurementType(), measurement.timestamp());
            }
        } catch (UnknownTargetException | IllegalArgumentException e) {
            LOG.error("Could not process measurement {}: {}", measurement, e.getMessage());
        }
        return applied;
    }

    /**
     * Starts the Shelly source if enabled.
     * 
     * @return {@code true} if the source was started, {@code false} else
     * @throws MqttException if connecting to the broker fails
     */
    private boolean startShelly() throws MqttException {
        boolean enabled = Boolean.getBoolean(PROP_PREFIX + "mqtt.enabled");
        if (enabled) {
            ShellyMqttIngestor shelly = new ShellyMqttIngestor("ShellyPro3EM", MACHINE_SHELLY, 
                System.getProperty(PROP_PREFIX + "mqtt.broker", "tcp://localhost:1883"), "energy-agg-shelly", 
                System.getProperty(PROP_PREFIX + "mqtt.topic", "shellies/shellypro3em/+/emeter/0"), 1, 
                this::process, t -> LOG.error("Shelly error: {}", t.getMessage()));
            shelly.connect();
            shelly.start();
            Runtime.getRuntime().addShutdownHook(new Thread(shelly::disconnect));
            LOG.info("Shelly MQTT source started (sourceId=ShellyPro3EM, deviceId={})", MACHINE_SHELLY);
        }
        return enabled;
    }

    /**
     * Starts the SENTRON source if enabled.
     * 
     * @return {@code true} if the source was started, {@code false} else
     */
    private boolean startSentron() {
        boolean enabled = Boolean.getBoolean(PROP_PREFIX + "modbus.enabled");
        if (enabled) {
            SentronModbusIngestor sentron = new SentronModbusIngestor("SentronPAC", MACHINE_SENTRON, 
                System.getProperty(PROP_PREFIX + "modbus.host", "b7-lasertisch.ifw.uni-hannover.de"), 
                Integer.getInteger(PROP_PREFIX + "modbus.port", DEFAULT_MODBUS_PORT), 1, MODBUS_INTERVAL_MS, 
                this::process, t -> LOG.error("SENTRON error: {}", t.getMessage()));
            try {
                sentron.connect();
            } catch (IOException e) {
                LOG.warn("SENTRON not reachable, polling anyway (values are marked unavailable): {}", 
                    e.getMessage());
            }
            sentron.start();
            Runtime.getRuntime().addShutdownHook(new Thread(sentron::stop));
            LOG.info("SENTRON Modbus source started (sourceId=SentronPAC, deviceId={})", MACHINE_SENTRON);
        }
        return enabled;
    }

    /**
     * Writes two example values to the server if no source is enabled.
     * 
     * @throws UnknownTargetException if a target is unknown (shall not occur)
     */
    private void runServerOnlyCheck() throws UnknownTargetException {
        Instant now = Instant.now();
        server.applyUpdate(new MappedModelUpdate(MACHINE_SHELLY, OpcEnergyModelMapper.POWER_TARGET, CHECK_POWER, 
            "W", now, OpcUaQuality.GOOD));
        server.applyUpdate(new MappedModelUpdate(MACHINE_SHELLY, OpcEnergyModelMapper.ENERGY_TARGET, 
            CHECK_ENERGY, "Wh", now, OpcUaQuality.GOOD));
        LOG.info("No source enabled. Server-only check: applied 2 updates to {}", MACHINE_SHELLY);
    }

    /**
     * Starts the enabled sources or, if none is enabled, runs the server-only check.
     * 
     * @throws MqttException if connecting to the MQTT broker fails
     * @throws UnknownTargetException if a target is unknown (shall not occur)
     */
    public void startSources() throws MqttException, UnknownTargetException {
        boolean shelly = startShelly();
        boolean sentron = startSentron();
        if (!shelly && !sentron) {
            runServerOnlyCheck();
        }
    }

    /**
     * Starts the server and the sources and runs until terminated.
     * 
     * @param args command line arguments, ignored
     * @throws IOException if the server cannot be started
     * @throws InterruptedException if waiting is interrupted
     * @throws MqttException if connecting to the MQTT broker fails
     * @throws UnknownTargetException if a target is unknown (shall not occur)
     */
    public static void main(String[] args) throws IOException, InterruptedException, MqttException, 
        UnknownTargetException {
        EnergyAggregatorServer server = new EnergyAggregatorServer(
            System.getProperty(PROP_PREFIX + "host", "127.0.0.1"), 
            Integer.getInteger(PROP_PREFIX + "port", DEFAULT_PORT), List.of(MACHINE_SHELLY, MACHINE_SENTRON));
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        new EnergyAggregatorMain(server).startSources();
        LOG.info("Server running at {}", server.getEndpointUrl());
        LOG.info("Browse: Objects > EnergyAggregator > Machines > <Machine> > {} | {}", 
            OpcEnergyModelMapper.POWER_TARGET, OpcEnergyModelMapper.ENERGY_TARGET);
        LOG.info("Press Ctrl+C to stop.");
        Thread.currentThread().join();
    }

}
