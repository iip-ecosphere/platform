package de.iip_ecosphere.platform.examples.rest.mixed;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class MachineInputTranslatorMixed<O> extends AbstractConnectorInputTypeTranslator<MachineInputMixed, O> {

    @Override
    public Class<? extends O> getSourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends MachineInputMixed> getTargetType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public O from(MachineInputMixed data) throws IOException {
       
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

        if (data.getTn() != null) {
            access.set("tn", data.getTn().getValueToWrite());
        }
        
        return null;
    }

}
