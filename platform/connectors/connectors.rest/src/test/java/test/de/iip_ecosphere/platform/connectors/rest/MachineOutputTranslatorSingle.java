package test.de.iip_ecosphere.platform.connectors.rest;

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

        MachineOutputSingle result = new MachineOutputSingle();
        result.setStringValue((TestServerResponsSingleRestType) access.get("string"));
        result.setShortValue((TestServerResponsSingleRestType) access.get("short"));
        result.setIntegerValue((TestServerResponsSingleRestType) access.get("integer"));
        result.setLongValue((TestServerResponsSingleRestType) access.get("long"));
        result.setFloatValue((TestServerResponsSingleRestType) access.get("float"));
        result.setDoubleValue((TestServerResponsSingleRestType) access.get("double"));

        return result;
    }

}
