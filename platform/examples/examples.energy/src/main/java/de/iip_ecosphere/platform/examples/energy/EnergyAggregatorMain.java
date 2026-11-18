package de.iip_ecosphere.platform.examples.energy;

import de.iip_ecosphere.platform.examples.energy.aggregator.core.EnergyAggregator;
import de.iip_ecosphere.platform.examples.energy.aggregator.mapping.OpcEnergyModelMapper;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MappedModelUpdate;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.OpcUaQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.server.EnergyAggregatorServer;
import de.iip_ecosphere.platform.examples.energy.aggregator.server.EnergyNamespace;
import de.iip_ecosphere.platform.examples.energy.aggregator.source.SentronModbusIngestor;
import de.iip_ecosphere.platform.examples.energy.aggregator.source.ShellyMqttIngestor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class EnergyAggregatorMain {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyAggregatorMain.class);

    public static void main(String[] args) {
        try {
            run();
        } catch (Throwable t) {
            System.err.println("EnergyAggregatorMain failed:");
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        EnergyAggregatorServer server = new EnergyAggregatorServer("127.0.0.1", 4840, "smoke");
        System.out.println("Starting EnergyAggregatorServer on " + server.getEndpointUrl() + " ...");
        server.start();
        System.out.println("EnergyAggregatorServer started. Namespace size: "
            + server.getNamespace().size());

        EnergyAggregator aggregator = new EnergyAggregator();
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        java.util.function.Consumer<MachineEnergyMeasurement> pipeline = measurement -> {
            try {
                boolean accepted = aggregator.updateMeasurement(measurement);
                if (!accepted) {
                    LOG.debug("Ignoring older/duplicate: device={}, type={}, ts={}",
                        measurement.deviceId(), measurement.measurementType(),
                        measurement.timestamp());
                    return;
                }
                MachineEnergyMeasurement latest = aggregator
                    .getLatestMeasurement(measurement.deviceId(), measurement.measurementType())
                    .orElse(null);
                if (latest == null) return;

                MappedModelUpdate update = mapper.map(latest);
                server.applyUpdate(update);

                LOG.info("Applied: device={}, target={}, value={}, unit={}",
                    update.deviceId(), update.target(), update.value(), update.unit());
            } catch (EnergyNamespace.UnknownTargetException | IllegalArgumentException e) {
                LOG.error("Could not process measurement: {}", measurement, e);
            }
        };

        boolean anyIngestor = false;

        // Shelly (MQTT) — sourceId = meter, deviceId = machine
        if (Boolean.parseBoolean(System.getProperty("p3.mqtt.enabled", "false"))) {
            ShellyMqttIngestor shelly = new ShellyMqttIngestor(
                "ShellyPro3EM",
                "LatheNEF400",
                System.getProperty("p3.mqtt.broker", "tcp://localhost:1883"),
                "energy-agg-shelly",
                System.getProperty("p3.mqtt.topic", "shellies/shellypro3em/+/emeter/0"),
                1, pipeline, t -> LOG.error("Shelly error", t));
            shelly.connect();
            shelly.start();
            System.out.println("Shelly MQTT ingestor started (sourceId=ShellyPro3EM, deviceId=LatheNEF400)");
            anyIngestor = true;
        }

        // SENTRON (ModbusTCP)
        if (Boolean.parseBoolean(System.getProperty("p3.modbus.enabled", "false"))) {
            SentronModbusIngestor sentron = new SentronModbusIngestor(
                "SentronPAC",
                "MarkingLaser",
                System.getProperty("p3.modbus.host", "b7-lasertisch.ifw.uni-hannover.de"),
                Integer.parseInt(System.getProperty("p3.modbus.port", "502")),
                1,
                200,
                pipeline,
                t -> LOG.error("Sentron error", t));
            sentron.connect();
            sentron.start();
            System.out.println("SENTRON Modbus ingestor started (deviceId=MarkingLaser)");
            anyIngestor = true;
        }

        // Server-only check runs only when neither ingestor is enabled.
        if (!anyIngestor) {
            System.out.println("No ingestor enabled. Running server-only check.");
            runServerOnlyCheck(server);
        }

        System.out.println("Server running at " + server.getEndpointUrl());
        System.out.println("Browse: Objects → EnergyAggregator → Machines → <Machine>");
        System.out.println("  Power node:  " + EnergyNamespace.POWER_TARGET);
        System.out.println("  Energy node: " + EnergyNamespace.ENERGY_TARGET);
        System.out.println("Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping EnergyAggregatorServer ...");
            server.stop();
        }));

        Thread.currentThread().join();
    }

    private static void runServerOnlyCheck(EnergyAggregatorServer server) throws Exception {
        OpcUaQuality good = OpcUaQuality.GOOD;
        Instant now = Instant.now();

        server.applyUpdate(new MappedModelUpdate(
            "LatheNEF400", EnergyNamespace.POWER_TARGET, 1500.5, "W", now, good));
        server.applyUpdate(new MappedModelUpdate(
            "LatheNEF400", EnergyNamespace.ENERGY_TARGET, 123456.7, "Wh", now, good));

        System.out.println("Server-only check: applied 2 updates to LatheNEF400.");
    }
}
