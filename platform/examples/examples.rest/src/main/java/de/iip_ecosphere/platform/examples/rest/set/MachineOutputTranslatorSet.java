package de.iip_ecosphere.platform.examples.rest.set;

import java.io.IOException;
import java.util.Set;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.rest.RESTEndpointMap;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.support.json.JsonUtils;

public class MachineOutputTranslatorSet<S> extends AbstractConnectorOutputTypeTranslator<S, MachineOutputSet> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Constructor.
     * @param withNotifications
     * @param sourceType
     */
    public MachineOutputTranslatorSet(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType; 
    }
    
    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);
    }

    @Override
    public Class<? extends S> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends MachineOutputSet> getTargetType() {
        return MachineOutputSet.class;
    }

    @Override
    public MachineOutputSet to(S source) throws IOException {
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();
        ConnectorParameter params = access.getConnectorParameter();

        
        Object endpoints = params.getSpecificSetting("Endpoints");
        RESTEndpointMap map = JsonUtils.fromJson(endpoints, RESTEndpointMap.class);
        //final ModelInputConverter inConverter = access.getInputConverter();
        
        MachineOutputSet result = new MachineOutputSet();
        
        Set<String> keys = map.keySet();
        
        if (keys.size() == 1) {
            String key = keys.iterator().next();
            TestServerResponseSet response = (TestServerResponseSet) access.get(key.toLowerCase());
            TestServerResponseSetItem[] items = response.getItems();
            
            for (TestServerResponseSetItem item : items) {
                
                if (item.getId().equals("f")) {
                    result.setF(item);
                } else if (item.getId().equals("u1")) {
                    result.setU1(item);
                } else if (item.getId().equals("u2")) {
                    result.setU2(item);
                } else if (item.getId().equals("u3")) {
                    result.setU3(item);
                } else if (item.getId().equals("u12")) {
                    result.setU12(item);
                } else if (item.getId().equals("u23")) {
                    result.setU23(item);
                } else if (item.getId().equals("u31")) {
                	result.setU31(item);
                } else if (item.getId().equals("i1")) {
                	result.setI1(item);
                } else if (item.getId().equals("i2")) {
                	result.setI2(item);
                } else if (item.getId().equals("i3")) {
                	result.setI3(item);
                }
            }             
        }

        return result;
    }

}
