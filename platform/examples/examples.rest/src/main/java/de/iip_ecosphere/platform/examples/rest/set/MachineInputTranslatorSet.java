package de.iip_ecosphere.platform.examples.rest.set;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class MachineInputTranslatorSet<O> extends AbstractConnectorInputTypeTranslator<MachineInputSet, O> {

    @Override
    public Class<? extends O> getSourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends MachineInputSet> getTargetType() {
        return MachineInputSet.class;
    }

    @Override
    public O from(MachineInputSet data) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
