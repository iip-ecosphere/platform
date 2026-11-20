package de.oktoflow.platform.tools.energyAggregator.mapping;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;
import de.oktoflow.platform.tools.energyAggregator.model.OpcUaQuality;

/**
 * Maps measurements to the electrical energy variables of OPC 34100 (Energy Consumption Management, ECM, as
 * used by OPC 40001-4), i.e., the variables of energy profile E2. Normalizes units to W/Wh and translates the
 * measurement quality into OPC UA terms; timestamps are passed through unchanged.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public class OpcEnergyModelMapper implements EnergyModelMapper {

    /**
     * Target variable for the current active power (W).
     */
    public static final String POWER_TARGET = "AcActivePowerTotal";

    /**
     * Target variable for the cumulative imported active energy (Wh).
     */
    public static final String ENERGY_TARGET = "AcActiveEnergyTotalImportLp";

    /**
     * Factor between kilo-units and base units.
     */
    private static final double KILO = 1000.0;

    @Override
    public MappedModelUpdate map(MachineEnergyMeasurement measurement) {
        MappedModelUpdate result;
        if (measurement.measurementType() == MeasurementType.CURRENT_POWER) {
            result = mapCurrentPower(measurement);
        } else if (measurement.measurementType() == MeasurementType.CUMULATIVE_ENERGY) {
            result = mapCumulativeEnergy(measurement);
        } else {
            throw new IllegalArgumentException("Unsupported measurement type: " + measurement.measurementType());
        }
        return result;
    }

    /**
     * Maps a current power measurement.
     * 
     * @param measurement the measurement
     * @return the model update in W
     * @throws IllegalArgumentException if the unit is not supported
     */
    private MappedModelUpdate mapCurrentPower(MachineEnergyMeasurement measurement) {
        double normalized = switch (measurement.unit()) {
        case "W" -> measurement.value();
        case "kW" -> measurement.value() * KILO;
        default -> throw new IllegalArgumentException("Unsupported power unit: " + measurement.unit());
        };
        return new MappedModelUpdate(measurement.deviceId(), POWER_TARGET, normalized, "W", 
            measurement.timestamp(), mapQuality(measurement.quality()));
    }

    /**
     * Maps a cumulative energy measurement.
     * 
     * @param measurement the measurement
     * @return the model update in Wh
     * @throws IllegalArgumentException if the unit is not supported
     */
    private MappedModelUpdate mapCumulativeEnergy(MachineEnergyMeasurement measurement) {
        double normalized = switch (measurement.unit()) {
        case "Wh" -> measurement.value();
        case "kWh" -> measurement.value() * KILO;
        default -> throw new IllegalArgumentException("Unsupported energy unit: " + measurement.unit());
        };
        return new MappedModelUpdate(measurement.deviceId(), ENERGY_TARGET, normalized, "Wh", 
            measurement.timestamp(), mapQuality(measurement.quality()));
    }

    /**
     * Translates a measurement quality into OPC UA terms.
     * 
     * @param quality the measurement quality
     * @return the OPC UA quality
     */
    private OpcUaQuality mapQuality(MeasurementQuality quality) {
        return switch (quality) {
        case VALID -> OpcUaQuality.GOOD;
        case STALE -> OpcUaQuality.UNCERTAIN;
        case UNAVAILABLE -> OpcUaQuality.BAD_NOT_CONNECTED;
        case INVALID -> OpcUaQuality.BAD_INVALID_DATA;
        };
    }

}
