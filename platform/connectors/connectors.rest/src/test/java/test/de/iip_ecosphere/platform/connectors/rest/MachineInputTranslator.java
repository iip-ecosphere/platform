package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class MachineInputTranslator<O> extends AbstractConnectorInputTypeTranslator<MachineInput, O> {

    @Override
    public Class<? extends O> getSourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends MachineInput> getTargetType() {
        return MachineInput.class;
    }

    @Override
    public O from(MachineInput data) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
