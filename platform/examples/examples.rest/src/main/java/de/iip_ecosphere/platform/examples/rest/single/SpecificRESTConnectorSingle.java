package de.iip_ecosphere.platform.examples.rest.single;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsTariffNumber;


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
    protected  Class<? extends RESTServerResponse>[] getResponseClass() {
        return new Class[]{TestServerResponsMeasurementSingle.class, TestServerResponsTariffNumber.class};
    }

}
