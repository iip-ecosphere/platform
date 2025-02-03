package de.iip_ecosphere.platform.examples.rest.mixed;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsTariffNumber;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformation;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSet;

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
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();
        access.getConnectorParameter();

        MachineOutputMixed result = new MachineOutputMixed();
        result.setTn((TestServerResponsTariffNumber) access.get("tn"));
        result.setF((TestServerResponsMeasurementSingle) access.get("f"));
        result.setU1((TestServerResponsMeasurementSingle) access.get("u1"));
        result.setU2((TestServerResponsMeasurementSingle) access.get("u2"));
        result.setU3((TestServerResponsMeasurementSingle) access.get("u3"));
        result.setI1(((TestServerResponseMeasurementSet) access.get("all")).getItems()[7]);
        result.setI2(((TestServerResponseMeasurementSet) access.get("all")).getItems()[8]);
        result.setI3(((TestServerResponseMeasurementSet) access.get("all")).getItems()[9]);
        result.setRoot1(((TestServerResponseInformation) access.get("information")).getRootItems()[0]);
        result.setRoot2(((TestServerResponseInformation) access.get("information")).getRootItems()[1]);
        result.setInfo1(((TestServerResponseInformation) access.get("information")).getInfoItems()[0]);
        result.setInfo2(((TestServerResponseInformation) access.get("information")).getInfoItems()[1]);
        
        return result;
    }

}
