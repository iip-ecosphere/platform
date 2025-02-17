package test.de.iip_ecosphere.platform.connectors.rest;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;


public class SpecificRESTConnectorSingle extends RESTConnector<MachineOutputSingle, MachineInputSingle> {

    
    /**
     * Constructor.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public SpecificRESTConnectorSingle(
            ProtocolAdapter<RESTItem, Object, MachineOutputSingle, MachineInputSingle>... adapter) {
        super(adapter);
    }
    
    @Override
    protected  Class<?>[] getResponseClasses() {
     
        return  new Class[]{TestServerResponsSingle.class};
    }

}
