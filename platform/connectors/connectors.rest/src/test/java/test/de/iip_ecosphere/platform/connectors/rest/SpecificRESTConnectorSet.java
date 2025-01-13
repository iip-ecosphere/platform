package test.de.iip_ecosphere.platform.connectors.rest;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponseValue;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

public class SpecificRESTConnectorSet extends RESTConnector<RESTMeasurement, RESTCommand> {

    /**
     * Constructor.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public SpecificRESTConnectorSet(ProtocolAdapter<RESTItem, Object, RESTMeasurement, RESTCommand>... adapter) {
        super(adapter);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <T1 extends RESTServerResponse> Class<T1> getResponseClass() {

        return (Class<T1>) SetValue.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T2 extends RESTServerResponseValue> Class<T2> getValueClass() {
        
        return (Class<T2>) ResponseValue.class;
    }

}
