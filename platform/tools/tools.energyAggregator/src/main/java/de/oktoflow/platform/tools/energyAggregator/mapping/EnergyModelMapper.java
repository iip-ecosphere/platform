package de.oktoflow.platform.tools.energyAggregator.mapping;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;

/**
 * Maps protocol-neutral measurements to updates of the target information model.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public interface EnergyModelMapper {

    /**
     * Maps a measurement to a model update.
     * 
     * @param measurement the measurement to map
     * @return the model update
     * @throws IllegalArgumentException if the measurement type or the unit is not supported
     */
    public MappedModelUpdate map(MachineEnergyMeasurement measurement);

}
