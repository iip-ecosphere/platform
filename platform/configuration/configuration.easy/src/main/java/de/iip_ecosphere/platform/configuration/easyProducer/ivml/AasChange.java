package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

import de.iip_ecosphere.platform.configuration.cfg.ConfigurationChangeType;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Records an AAS change.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasChange extends de.iip_ecosphere.platform.configuration.cfg.AasChange {
    
    private IDecisionVariable var;

    /**
     * Creates an instance.
     * 
     * @param var the decision variable
     * @param type the change type
     */
    public AasChange(IDecisionVariable var, ConfigurationChangeType type) {
        super(type);
        this.var = var;
    }
    
    @Override
    public void apply(Submodel sm, SubmodelBuilder smB) {
        AasIvmlMapper mapper = ConfigurationManager.getAasIvmlMapper();
        mapper.deleteAasVariableMapping(sm, var); // throw away, do nothing if not exists
        switch (getType()) {
        case CREATED:
            mapper.mapVariableToAas(smB, var);
            break;
        case DELETED:
            // already deleted
            break;
        case MODIFIED:
            mapper.mapVariableToAas(smB, var);
            break;
        default:
            break;
        }
    }
    
}