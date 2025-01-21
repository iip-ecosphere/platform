package de.iip_ecosphere.platform.examples.rest.single;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
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
    
    @SuppressWarnings("unchecked")
    @Override
    protected <T1 extends RESTServerResponse> Class<T1> getResponseClass() {
        return (Class<T1>) TestServerResponsSingle.class;
    }

    @Override
    protected <T2> Class<T2> getItemClass() {
        // No inner Item Class
        return null;
    }

}
