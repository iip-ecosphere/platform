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

    @SuppressWarnings("unchecked")
	@Override
    public O from(MachineInputSingle data) throws IOException {
        return (O) data.getTn().getValue();
    }

}
