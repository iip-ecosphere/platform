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
    protected <T1 extends RESTServerResponse> Class<T1> getResponseClass() {

        return (Class<T1>) TestServerResponseSet.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T2> Class<T2> getItemClass() {
        return (Class<T2>) TestServerResponseSetItem.class;
    }

}
