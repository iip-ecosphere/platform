package de.iip_ecosphere.platform.examples.rest.mixed;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumber;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformation;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSet;

public class SpecificRESTConnectorMixed extends RESTConnector<MachineOutputMixed, MachineInputMixed> {

    /**
     * Constructor.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public SpecificRESTConnectorMixed(
            ProtocolAdapter<RESTItem, Object, MachineOutputMixed, MachineInputMixed>... adapter) {
        super(adapter);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends RESTServerResponse>[] getResponseClasses() {

        return new Class[] {TestServerResponseTariffNumber.class, TestServerResponseMeasurementSingle.class,
            TestServerResponseMeasurementSet.class, TestServerResponseInformation.class};
    }

}
