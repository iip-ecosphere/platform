package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

import java.util.HashMap;
import java.util.Map;

import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * A "map" holding names/id to service mappings.
 * 
 * @author Holger Eichelberger, SSE
 */
class ServiceMap {
    
    private Map<String, IDecisionVariable> nameToService = new HashMap<>();
    private Map<String, IDecisionVariable> idToService = new HashMap<>();
    
    /**
     * Returns a service by id or by configured name.
     * 
     * @param svc the service id/name
     * @return the resolved service, may be <b>null</b>
     */
    public IDecisionVariable getService(String svc) {
        IDecisionVariable result = nameToService.get(svc);
        if (null == result) {
            result = idToService.get(svc);
        }
        return result;
    }
    
    /**
     * Adds a service.
     * 
     * @param var the configured variable representing the service
     */
    void add(IDecisionVariable var) {
        String name = var.getDeclaration().getName(); // just fallback
        name = IvmlUtils.getStringValue(var.getNestedElement("name"), name);
        if (null != name) {
            nameToService.put(name, var);
        }
        String id = IvmlUtils.getStringValue(var.getNestedElement("id"), name);
        if (null != id) {
            idToService.put(id, var);
        }
    }
    
}