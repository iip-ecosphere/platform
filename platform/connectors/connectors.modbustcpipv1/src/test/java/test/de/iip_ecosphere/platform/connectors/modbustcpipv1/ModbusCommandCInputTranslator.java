package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class ModbusCommandCInputTranslator<O> extends AbstractConnectorInputTypeTranslator<ModbusCommandC, O> {

    private Class<? extends O> sourceType;

    /**
     * Creates a new modbus command input translator.
     * 
     * @param sourceType the source type
     */
    public ModbusCommandCInputTranslator(Class<? extends O> sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public Class<? extends O> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends ModbusCommandC> getTargetType() {
        return ModbusCommandC.class;
    }

    @Override
    public O from(ModbusCommandC data) throws IOException {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

        if (data.getShort() != null) {
            access.set("Short", data.getShort());
        }
        
        if (data.getInteger() != null) {
            access.set("Integer", data.getInteger());
        }
        
        if (data.getFloat() != null) {
            access.set("Float", data.getFloat());
        }
        
        if (data.getLong() != null) {
            access.set("Long", data.getLong());
        }
        
        if (data.getDouble() != null) {
            access.set("Double", data.getDouble());
        }

        return null;
    }

}
