package de.oktoflow.platform.tools.energyAggregator.model;

import java.time.Instant;

/**
 * A value mapped to the target information model, ready to be applied by the server.
 * 
 * @param deviceId the machine the value belongs to; together with {@code target} it identifies the node to update
 * @param target the target variable name, e.g., "AcActivePowerTotal"
 * @param value the value in the unit required by the target variable
 * @param unit the unit of {@code value}, e.g., "W" or "Wh"
 * @param timestamp the time of measurement
 * @param quality the quality of the value in OPC UA terms
 * @author Rhoda Ikhuohon-Eboreime
 */
public record MappedModelUpdate(
    String deviceId,
    String target,
    double value,
    String unit,
    Instant timestamp,
    OpcUaQuality quality) {
}
