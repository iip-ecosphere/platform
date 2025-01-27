package test.de.iip_ecosphere.platform.connectors.rest;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;


public class SpecificRESTConnectorSingle extends RESTConnector<MachineOutputSingle, MachineInput> {

    
    /**
     * Constructor.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public SpecificRESTConnectorSingle(
            ProtocolAdapter<RESTItem, Object, MachineOutputSingle, MachineInput>... adapter) {
        super(adapter);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected  Class<? extends RESTServerResponse>[] getResponseClass() {
     
        return  new Class[]{TestServerResponsSingle.class};
    }

}
