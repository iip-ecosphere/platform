package de.iip_ecosphere.platform.examples.rest.mixed;

import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingleRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumberRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformationRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSetRestType;

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

    @Override
    protected Class<?>[] getResponseClasses() {

        return new Class[] {TestServerResponseTariffNumberRestType.class, 
            TestServerResponseMeasurementSingleRestType.class,
            TestServerResponseMeasurementSetRestType.class, 
            TestServerResponseInformationRestType.class};
    }

}
