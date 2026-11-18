package de.iip_ecosphere.platform.examples.energy.aggregator.model;

import java.time.Instant;

public record MappedModelUpdate(
	String deviceId,
    String target,
    double value,
    String unit,
    Instant timestamp,
    OpcUaQuality quality
) {
}