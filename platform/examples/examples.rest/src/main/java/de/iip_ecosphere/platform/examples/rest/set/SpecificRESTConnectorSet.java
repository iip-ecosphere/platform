package de.iip_ecosphere.platform.examples.rest.set;


import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

public class SpecificRESTConnectorSet extends RESTConnector<MachineOutputSet, MachineInputSet> {

    /**
     * Constructor.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public SpecificRESTConnectorSet(ProtocolAdapter<RESTItem, Object, MachineOutputSet, MachineInputSet>... adapter) {
        super(adapter);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected  Class<? extends RESTServerResponse>[] getResponseClass() {

        return new Class[]{TestServerResponseSet.class};
    }

    

}
