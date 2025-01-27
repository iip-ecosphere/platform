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

    @SuppressWarnings("unchecked")
    @Override
    public O from(MachineInput data) throws IOException {

        System.out.println(data);
        return (O) data.getStringValue();
    }

}
