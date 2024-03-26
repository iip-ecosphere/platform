package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusNamespace;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator.OutputCustomizer;

/**
 * The modbus machine data output translator for tests.
 * 
 * @param <S> the source datatype
 * 
 * @author Christian Nikolajew
 */
public class ModbusMachineDataOutputTranslator<S>  extends AbstractConnectorOutputTypeTranslator<S, ModbusMachineData> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    private OutputCustomizer customizer;
    
    /**
     * Creates a new machine data output translator.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @param sourceType the source type
     * @param customizer the translator customizer
     */
    public ModbusMachineDataOutputTranslator(boolean withNotifications, Class<? extends S> sourceType, 
        OutputCustomizer customizer) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
        this.customizer = customizer;
    }

    @Override
    public ModbusMachineData to(Object source) throws IOException {
        
        ModelAccess access = getModelAccess();

        String varShort = ModbusNamespace.NAME_VAR_SHORT_VALUE;
        String varInt = ModbusNamespace.NAME_VAR_INT_VALUE;
        String varFloat = ModbusNamespace.NAME_VAR_FLOAT_VALUE;
        String varLong = ModbusNamespace.NAME_VAR_LONG_VALUE;
        String varDouble = ModbusNamespace.NAME_VAR_DOUBLE_VALUE;
        
        short lShort = access.getInputConverter().toShort(access.get(varShort));
        int lInt = access.getInputConverter().toInteger(access.get(varInt));
        float lFloat = access.getInputConverter().toFloat(access.get(varFloat));
        long lLong = access.getInputConverter().toLong(access.get(varLong));
        double lDouble = access.getInputConverter().toDouble(access.get(varDouble));
        
        ModbusMachineData lResult = new ModbusMachineData();
        lResult.setShortValue(lShort);
        lResult.setIntValue(lInt);
        lResult.setFloatValue(lFloat);
        lResult.setLongValue(lLong);
        lResult.setDoubleValue(lDouble);

        return lResult;
    }

    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);
        customizer.initializeModelAccess(access, withNotifications);
        
    }

    @Override
    public Class<? extends S> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends ModbusMachineData> getTargetType() {
        return ModbusMachineData.class;
    }
}
