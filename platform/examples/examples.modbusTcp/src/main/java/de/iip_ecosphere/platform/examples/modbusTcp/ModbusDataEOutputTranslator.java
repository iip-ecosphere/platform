package de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

public class ModbusDataEOutputTranslator<S> extends AbstractConnectorOutputTypeTranslator<S, ModbusDataE> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Constructor.
     * 
     * @param withNotifications
     * @param sourceType
     */
    public ModbusDataEOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }

    @Override
    public ModbusDataE to(Object source) throws IOException {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        ModbusDataE result = new ModbusDataE();
        
        result.setDay((short) access.get("Day"));
        result.setMonth((short) access.get("Month"));
        result.setYear((short) access.get("Year"));
        result.setU12((float) access.get("U12"));
        result.setU23((float) access.get("U23"));
        result.setU31((float) access.get("U31"));
//        result.setU1((float) access.get("U1"));
//        result.setU2((float) access.get("U2"));
//        result.setU3((float) access.get("U3"));
//        result.setFrequency((float) access.get("frequency"));
//        result.setI1((float) access.get("I1"));
//        result.setI2((float) access.get("I2"));
//        result.setI3((float) access.get("I3"));
//        result.setTotalActivePower((float) access.get("Total active power"));
        
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
    public Class<? extends ModbusDataE> getTargetType() {
        return ModbusDataE.class;
    }

}
