package de.iip_ecosphere.platform.examples.rest.single;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsTariffNumber;

public class MachineOutputTranslatorSingle<S> extends AbstractConnectorOutputTypeTranslator<S, MachineOutputSingle> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Constructor.
     * @param withNotifications
     * @param sourceType
     */
    public MachineOutputTranslatorSingle(boolean withNotifications, Class<? extends S> sourceType) {
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
        return sourceType;
    }

    @Override
    public Class<? extends MachineOutputSingle> getTargetType() {
        return MachineOutputSingle.class;
    }

    @Override
    public MachineOutputSingle to(S source) throws IOException {
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();
        //final ModelInputConverter inConverter = access.getInputConverter();

        MachineOutputSingle result = new MachineOutputSingle();
        result.setF((TestServerResponsMeasurementSingle) access.get("f"));
        result.setU1((TestServerResponsMeasurementSingle) access.get("u1"));
        result.setU2((TestServerResponsMeasurementSingle) access.get("u2"));
        result.setU3((TestServerResponsMeasurementSingle) access.get("u3"));
        result.setU12((TestServerResponsMeasurementSingle) access.get("u12"));
        result.setU23((TestServerResponsMeasurementSingle) access.get("u23"));
        result.setU31((TestServerResponsMeasurementSingle) access.get("u31"));
        result.setI1((TestServerResponsMeasurementSingle) access.get("i1"));
        result.setI2((TestServerResponsMeasurementSingle) access.get("i2"));
        result.setI3((TestServerResponsMeasurementSingle) access.get("i3"));
        result.setTn((TestServerResponsTariffNumber) access.get("tn"));

        return result;
    }

}
