package de.iip_ecosphere.platform.examples.energy.aggregator.mapping;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MappedModelUpdate;

public interface EnergyModelMapper {

    MappedModelUpdate map(MachineEnergyMeasurement measurement);

}