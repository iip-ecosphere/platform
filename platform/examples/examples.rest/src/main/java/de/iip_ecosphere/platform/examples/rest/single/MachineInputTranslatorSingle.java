package de.iip_ecosphere.platform.examples.rest.single;

import java.io.IOException;

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
        // TODO Auto-generated method stub
        return null;
    }

}
