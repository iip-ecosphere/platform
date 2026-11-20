# oktoflow platform: Energy data aggregator

Standalone tool that collects energy measurements from heterogeneous sources and exposes them uniformly through an own OPC UA server, so that platform applications (e.g., [examples.energy](../../examples/examples.energy/README.md)) can read all machines through a single, generated OPC UA connector.

    sources (MQTT, ModbusTCP) -> EnergyAggregator -> EnergyModelMapper -> EnergyAggregatorServer (OPC UA)

* **Sources** (`source`) turn device-specific data into a uniform `MachineEnergyMeasurement` (source, device, type, value, unit, timestamp, quality). Currently:
    * `ShellyMqttIngestor`: Shelly Pro 3EM via MQTT, Gen2 fields `total_act_power`/`total_act`, Gen1 fields `power`/`total` as fallback.
    * `SentronModbusIngestor`: Siemens SENTRON PAC4200 via ModbusTCP, holding registers 65-66 (FLOAT32, total active power, W) and 801-804 (FLOAT64, imported active energy, Wh), big-endian, polled every 200 ms. If the device is not reachable, both values are published with quality `UNAVAILABLE`.
* **Aggregation** (`core`): keeps the latest measurement per device and measurement type; older or equally old values arriving late are ignored.
* **Mapping** (`mapping`): `OpcEnergyModelMapper` converts units (kW to W, kWh to Wh), maps the measurement type to the target variable and the quality to an OPC UA quality.
* **Server** (`server`): Eclipse Milo server with namespace `urn:iip:energy:aggregator` and the address space `Objects/EnergyAggregator/Machines/<machine>/{AcActivePowerTotal, AcActiveEnergyTotalImportLp}` (Float, read-only). Variable names follow the energy profile of OPC 34100 (Energy Consumption Management). Quality is reported as status code (`Good`, `Uncertain`, `Bad_NotConnected`, `Bad_InvalidArgument`); before the first value, variables are `Bad_WaitingForInitialData`.

Further protocols are added by implementing a source that emits `MachineEnergyMeasurement` instances; further target models by implementing `EnergyModelMapper`.

## Running

* Build: `mvn install`
* Run: `mvn exec:java@energyServer` (without enabled source, two example values are written for `LatheNEF400` as server-only check)
* System properties (with defaults):
    * `energyAggregator.host` (`127.0.0.1`), `energyAggregator.port` (`4840`): OPC UA endpoint `opc.tcp://<host>:<port>/`
    * `energyAggregator.mqtt.enabled` (`false`), `energyAggregator.mqtt.broker` (`tcp://localhost:1883`), `energyAggregator.mqtt.topic` (`shellies/shellypro3em/+/emeter/0`): Shelly source for machine `LatheNEF400`
    * `energyAggregator.modbus.enabled` (`false`), `energyAggregator.modbus.host` (`b7-lasertisch.ifw.uni-hannover.de`), `energyAggregator.modbus.port` (`502`): SENTRON source for machine `MarkingLaser`
* Example: `mvn exec:java@energyServer -DenergyAggregator.mqtt.enabled=true -DenergyAggregator.modbus.enabled=true -DenergyAggregator.modbus.host=localhost -DenergyAggregator.modbus.port=5020`

## Limitations

* Machines, source assignment and register map are currently fixed in `EnergyAggregatorMain`; a configuration file (Yaml) is not yet supported.
* Only current active power and cumulative imported active energy are supported.
* The OPC UA server runs without security (`SecurityPolicy.None`, anonymous access) and is intended for local/lab use.
* The SENTRON source opens a new ModbusTCP connection per poll.
* Tests run without devices or broker; the server test starts a local OPC UA server on port 4855.
