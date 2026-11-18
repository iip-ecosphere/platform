package de.iip_ecosphere.platform.examples.energy.aggregator.core;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;

public class EnergyAggregator {

    private final Map<MeasurementKey, MachineEnergyMeasurement> latestMeasurements
        = new ConcurrentHashMap<>();

    public boolean updateMeasurement(MachineEnergyMeasurement measurement) {
        Objects.requireNonNull(measurement, "measurement must not be null");
        MeasurementKey key = new MeasurementKey(
            measurement.deviceId(),
            measurement.measurementType()
        );

        boolean[] accepted = {false};
        latestMeasurements.compute(key, (k, existingMeasurement) -> {
            if (existingMeasurement == null
                    || measurement.timestamp().isAfter(existingMeasurement.timestamp())) {
                accepted[0] = true;
                return measurement;
            }

            return existingMeasurement;
        });
        return accepted[0];
    }
    
    public Optional<MachineEnergyMeasurement> getLatestMeasurement(
    	    String deviceId,
    	    MeasurementType measurementType
    	) {
    	    MeasurementKey key = new MeasurementKey(deviceId, measurementType);
    	    return Optional.ofNullable(latestMeasurements.get(key));
    	}

    private record MeasurementKey(
        String deviceId,
        MeasurementType measurementType
    ) {
    }

}