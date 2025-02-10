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

        if (data.getTn1() != null) {
            access.set("tn1", data.getTn1().getValueToWrite());
        }
        
        if (data.getTn2() != null) {
            access.set("tn2", data.getTn2());
        }
        
        
        return null;
    }

}
