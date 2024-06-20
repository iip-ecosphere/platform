package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

public class ModbusDataCOutputTranslator<S> extends AbstractConnectorOutputTypeTranslator<S, ModbusDataC> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Constructor.
     * 
     * @param withNotifications
     * @param sourceType
     */
    public ModbusDataCOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }

    @Override
    public ModbusDataC to(Object source) throws IOException {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        ModbusDataC result = new ModbusDataC();
        
        result.setShort((short) access.get("Short"));
        result.setInteger((int) access.get("Integer"));
        result.setFloat((float) access.get("Float"));
        result.setLong((long) access.get("Long"));
        result.setDouble((double) access.get("Double"));
        
        return result;
    }
    
    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);
        
    }

    @Override
    public Class<? extends S> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends ModbusDataC> getTargetType() {
        return ModbusDataC.class;
    }

}
