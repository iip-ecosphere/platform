package test.de.iip_ecosphere.platform.connectors.rest;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

public class SpecificRESTConnectorSet extends RESTConnector<MachineOutputSet, MachineInputSet> {

    /**
     * Constructor.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public SpecificRESTConnectorSet(ProtocolAdapter<RESTItem, Object, MachineOutputSet,
            MachineInputSet>... adapter) {
        super(adapter);
    }
    
    @Override
    protected Class<?> [] getResponseClasses() {

        return new Class[]{TestServerResponseSetRestType.class};
    }
}
