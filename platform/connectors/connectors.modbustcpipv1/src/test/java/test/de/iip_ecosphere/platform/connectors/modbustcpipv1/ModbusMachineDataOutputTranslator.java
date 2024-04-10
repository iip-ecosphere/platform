package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;
import java.util.ArrayList;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusKeys;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusVarItem;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
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

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        ConnectorParameter params = access.getConnectorParameter();
        
        String[] keys = ModbusKeys.getKeys();
        
        ArrayList<ModbusVarItem> items = new ArrayList<ModbusVarItem>();
        
        for (int i = 0; i < keys.length; i++) {
            items.add((ModbusVarItem) params.getSpecificSetting(keys[i]));  
        }
        
        ModbusMachineData lResult = new ModbusMachineData();
        
        
        for (int i = 0; i < items.size(); i++) {
            lResult.addValue(items.get(i), access.get(keys[i]));
        }
       
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
