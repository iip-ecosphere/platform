# P3 Source Ingestion — current state

## Where the code lives

    aggregator/source/
      ShellyMqttIngestor.java
      SentronModbusIngestor.java
      SourceConfig.java
      SourceHealthMonitor.java
    aggregator/server/
      EnergyAggregatorServer.java
      EnergyNamespace.java
    EnergyAggregatorMain.java   (smoke test harness, not production)

## How to run the smoke test

    cd examples/examples.energy
    mvn -U clean install -DskipTests
    mvn exec:java -Dexec.mainClass=de.iip_ecosphere.platform.examples.energy.EnergyAggregatorMain

Server address: `opc.tcp://127.0.0.1:4840/` (root, no path).

Browse:

    Objects → EnergyAggregator → Machines → LatheNEF400 → AcActivePowerTotal
                                                      → AcActiveEnergyTotalImportLp
                                → MarkingLaser → AcActivePowerTotal
                                               → AcActiveEnergyTotalImportLp

## How to run with ingestors

    # Shelly over MQTT (Gen2 fields preferred, Gen1 fallback)
    -Dp3.mqtt.enabled=true
    -Dp3.mqtt.broker=tcp://localhost:1883
    -Dp3.mqtt.topic=shellies/shellypro3em/+/emeter/0

    # SENTRON over ModbusTCP (holding registers, FLOAT32 power 65–66, FLOAT64 energy 801–804)
    -Dp3.modbus.enabled=true
    -Dp3.modbus.host=b7-lasertisch.ifw.uni-hannover.de
    -Dp3.modbus.port=502

If neither `p3.mqtt.enabled` nor `p3.modbus.enabled` is set, the harness
applies two hand-crafted updates to `LatheNEF400` so the server can be
inspected alone.

## Boundary with P4

    MQTT / ModbusTCP / OPC UA
            ↓
    aggregator.source.* → MachineEnergyMeasurement
            ↓
    aggregator.core.EnergyAggregator → latest state per (deviceId, type)
            ↓
    aggregator.mapping.OpcEnergyModelMapper → MappedModelUpdate
            ↓
    aggregator.server.EnergyNamespace.applyUpdate → OPC UA address space
            ↓
    generated oktoflow OPC UA connector reads the values

## Known items to verify against the real device

- Register offset: IFW's Telegraf map uses 65 and 801. Some Modbus
  libraries are 0-based; if readings look wrong, try 64 and 800.
- Unit of `AcActiveEnergyTotalImportLp`: currently emitted as `Wh`.
  Confirm from one real reading or the PAC4200 manual; change to `kWh`
  in the ingestor if the device is in kWh (the mapper normalizes).
- Shelly topic: Gen2 typically publishes on `<device-id>/status/em:0`
  (power) and `<device-id>/status/emdata:0` (energy). Subscribe to
  both, or one topic that carries both fields. The parser accepts
  either shape.
