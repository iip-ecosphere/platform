package de.iip_ecosphere.platform.examples.rest.single;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

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
        result.setF((TestServerResponsSingle) access.get("f"));
        result.setU1((TestServerResponsSingle) access.get("u1"));
        result.setU2((TestServerResponsSingle) access.get("u2"));
        result.setU3((TestServerResponsSingle) access.get("u3"));
        result.setU12((TestServerResponsSingle) access.get("u12"));
        result.setU23((TestServerResponsSingle) access.get("u23"));
        result.setU31((TestServerResponsSingle) access.get("u31"));
        result.setI1((TestServerResponsSingle) access.get("i1"));
        result.setI2((TestServerResponsSingle) access.get("i2"));
        result.setI3((TestServerResponsSingle) access.get("i3"));

        return result;
    }

}
