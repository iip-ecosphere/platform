package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.util.Set;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.rest.RESTEndpointMap;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;


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
//        ConnectorParameter params = access.getConnectorParameter();
//
//        
//        Object endpoints = params.getSpecificSetting("Endpoints");
//        RESTEndpointMap map = JsonUtils.fromJson(endpoints, RESTEndpointMap.class);
        //final ModelInputConverter inConverter = access.getInputConverter();
        
        RESTItem restItem = (RESTItem) source;
        RESTEndpointMap map = restItem.getEndpointMap();
        
        MachineOutputSet result = new MachineOutputSet();
        
        
        Set<String> keys = map.keySet();
        
        if (keys.size() == 1) {
            String key = keys.iterator().next();
            TestServerResponseSet response = (TestServerResponseSet) access.get(key.toLowerCase());
            TestServerResponseSetItem[] items = response.getItems();
            
            for (TestServerResponseSetItem item : items) {
                
                if (item.getId().equals("string")) {
                    result.setStringValue(item);
                } else if (item.getId().equals("short")) {
                    result.setShortValue(item);
                } else if (item.getId().equals("integer")) {
                    result.setIntegerValue(item);
                } else if (item.getId().equals("long")) {
                    result.setLongValue(item);
                } else if (item.getId().equals("float")) {
                    result.setFloatValue(item);
                } else if (item.getId().equals("double")) {
                    result.setDoubleValue(item);
                }
            }             
        }

        return result;
    }

}
