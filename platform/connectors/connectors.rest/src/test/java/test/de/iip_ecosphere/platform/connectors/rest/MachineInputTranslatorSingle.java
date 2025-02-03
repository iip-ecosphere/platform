package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class MachineInputTranslatorSingle<O> extends AbstractConnectorInputTypeTranslator<MachineInputSingle, O> {

    @Override
    public Class<? extends O> getSourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends MachineInputSingle> getTargetType() {
        return MachineInputSingle.class;
    }

    @Override
    public O from(MachineInputSingle data) throws IOException {
        
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

        if (data.getStringValue() != null) {
            access.set("string", data.getStringValue());
        }
        
        return null;
    }

}
