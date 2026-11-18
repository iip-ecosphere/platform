package de.iip_ecosphere.platform.examples.energy.aggregator.mapping;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MappedModelUpdate;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.OpcUaQuality;

public class OpcEnergyModelMapper implements EnergyModelMapper {

	@Override
	public MappedModelUpdate map(MachineEnergyMeasurement measurement) {

	    if (measurement.measurementType() == MeasurementType.CURRENT_POWER) {
	        return mapCurrentPower(measurement);
	    }

	    if (measurement.measurementType() == MeasurementType.CUMULATIVE_ENERGY) {
	        return mapCumulativeEnergy(measurement);
	    }

	    throw new IllegalArgumentException(
	        "Unsupported measurement type: " + measurement.measurementType()
	    );
	}

    private MappedModelUpdate mapCurrentPower(
    	    MachineEnergyMeasurement measurement
    	) {
    	    double normalizedValue = switch (measurement.unit()) {
    	    case "W" -> measurement.value();
    	    case "kW" -> measurement.value() * 1000.0;
    	    default -> throw new IllegalArgumentException(
    	        "Unsupported power unit: " + measurement.unit()
    	    );
    	    };

    	    return new MappedModelUpdate(
    	    	    measurement.deviceId(),
    	    	    "AcActivePowerTotal",
    	    	    normalizedValue,
    	    	    "W",
    	    	    measurement.timestamp(),
    	    	    mapQuality(measurement.quality())
    	    	);
    	}
    
    private MappedModelUpdate mapCumulativeEnergy(
    	    MachineEnergyMeasurement measurement
    	) {
    	    double normalizedValue = switch (measurement.unit()) {
    	    case "Wh" -> measurement.value();
    	    case "kWh" -> measurement.value() * 1000.0;
    	    default -> throw new IllegalArgumentException(
    	        "Unsupported energy unit: " + measurement.unit()
    	    );
    	    };

    	    return new MappedModelUpdate(
    	    	    measurement.deviceId(),
    	    	    "AcActiveEnergyTotalImportLp",
    	    	    normalizedValue,
    	    	    "Wh",
    	    	    measurement.timestamp(),
    	    	    mapQuality(measurement.quality())
    	    	);
    	}
    
    private OpcUaQuality mapQuality(MeasurementQuality quality) {
        return switch (quality) {
        case VALID -> OpcUaQuality.GOOD;
        case STALE -> OpcUaQuality.UNCERTAIN;
        case UNAVAILABLE -> OpcUaQuality.BAD_NOT_CONNECTED;
        case INVALID -> OpcUaQuality.BAD_INVALID_DATA;
        };
    }

}