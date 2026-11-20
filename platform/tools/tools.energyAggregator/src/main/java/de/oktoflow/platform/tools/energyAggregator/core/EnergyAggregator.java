package de.oktoflow.platform.tools.energyAggregator.core;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;

/**
 * Keeps the latest measurement per device and measurement type. Measurements older than (or as old as) the
 * stored one are ignored, so that delayed messages cannot overwrite newer data. Thread-safe, as sources may
 * deliver measurements concurrently.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public class EnergyAggregator {

    /**
     * The latest measurements per device and measurement type.
     */
    private final Map<MeasurementKey, MachineEnergyMeasurement> latestMeasurements = new ConcurrentHashMap<>();

    /**
     * Stores the measurement if it is newer than the one currently held for its device and measurement type.
     *
     * @param measurement the incoming measurement
     * @return {@code true} if the measurement became the latest state and shall be forwarded, {@code false} if
     *     it was older or had the same timestamp and was ignored
     * @throws NullPointerException if {@code measurement} is <b>null</b>
     */
    public boolean updateMeasurement(MachineEnergyMeasurement measurement) {
        Objects.requireNonNull(measurement, "measurement must not be null");
        MeasurementKey key = new MeasurementKey(measurement.deviceId(), measurement.measurementType());
        boolean[] accepted = {false};
        latestMeasurements.compute(key, (k, existing) -> {
            MachineEnergyMeasurement result = existing;
            if (existing == null || measurement.timestamp().isAfter(existing.timestamp())) {
                accepted[0] = true;
                result = measurement;
            }
            return result;
        });
        return accepted[0];
    }

    /**
     * Returns the latest measurement for the given device and measurement type.
     * 
     * @param deviceId the device id
     * @param measurementType the measurement type
     * @return the latest measurement, empty if there is none
     */
    public Optional<MachineEnergyMeasurement> getLatestMeasurement(String deviceId,
        MeasurementType measurementType) {
        return Optional.ofNullable(latestMeasurements.get(new MeasurementKey(deviceId, measurementType)));
    }

    /**
     * Key of the latest-state map.
     * 
     * @param deviceId the device id
     * @param measurementType the measurement type
     * @author Rhoda Ikhuohon-Eboreime
     */
    private record MeasurementKey(String deviceId, MeasurementType measurementType) {
    }

}
