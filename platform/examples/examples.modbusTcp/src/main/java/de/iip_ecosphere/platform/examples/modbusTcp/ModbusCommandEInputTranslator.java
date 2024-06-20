package de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class ModbusCommandEInputTranslator<O> extends AbstractConnectorInputTypeTranslator<ModbusCommandE, O> {

    private Class<? extends O> sourceType;

    /**
     * Creates a new modbus command input translator.
     * 
     * @param sourceType the source type
     */
    public ModbusCommandEInputTranslator(Class<? extends O> sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public Class<? extends O> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends ModbusCommandE> getTargetType() {
        return ModbusCommandE.class;
    }

    @Override
    public O from(ModbusCommandE data) throws IOException {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

        if (data.getDay() != null) {
            access.set("Day", data.getDay());
        }
        
        if (data.getMonth() != null) {
            access.set("Month", data.getMonth());
        }
        
        if (data.getYear() != null) {
            access.set("Year", data.getYear());
        }

        return null;
    }

}
