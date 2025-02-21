package de.iip_ecosphere.platform.examples.rest.mixed;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingleRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumberRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformationRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSetRestType;

public class MachineOutputTranslatorMixed<S> extends AbstractConnectorOutputTypeTranslator<S, MachineOutputMixed> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;

    /**
     * Constructor.
     * 
     * @param withNotifications
     * @param sourceType
     */
    public MachineOutputTranslatorMixed(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }

    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);

    }

    @Override
    public Class<? extends S> getSourceType() {
        // TODO Auto-generated method stub
        return sourceType;
    }

    @Override
    public Class<? extends MachineOutputMixed> getTargetType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MachineOutputMixed to(S source) throws IOException {

//        if (source != null) {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

        MachineOutputMixed result = new MachineOutputMixed();
        result.setTn1((TestServerResponseTariffNumberRestType) access.get("tn1"));
        result.setTn2((TestServerResponseTariffNumberRestType) access.get("tn2"));
        result.setF((TestServerResponseMeasurementSingleRestType) access.get("f"));
        result.setU1((TestServerResponseMeasurementSingleRestType) access.get("u1"));
        result.setU2((TestServerResponseMeasurementSingleRestType) access.get("u2"));
        result.setU3((TestServerResponseMeasurementSingleRestType) access.get("u3"));
        result.setAll((TestServerResponseMeasurementSetRestType) access.get("all"));
        result.setInformation((TestServerResponseInformationRestType) access.get("information"));

        return result;

//        }
//
//        return null;
    }

}
