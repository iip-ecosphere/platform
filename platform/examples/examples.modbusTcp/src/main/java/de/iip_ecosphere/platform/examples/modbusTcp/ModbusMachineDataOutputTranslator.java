/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.IOException;
import java.util.Set;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusMap;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.support.json.JsonUtils;

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
    
    /**
     * Creates a new machine data output translator.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @param sourceType the source type
     */
    public ModbusMachineDataOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }

    @Override
    public ModbusMachineData to(Object source) throws IOException {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        ConnectorParameter params = access.getConnectorParameter();
        
        Object serverStructure = params.getSpecificSetting("SERVER_STRUCTURE");
        ModbusMap map = JsonUtils.fromJson(serverStructure, ModbusMap.class);
        
        Set<String> keys = map.keySet();
        
        ModbusMachineData lResult = new ModbusMachineData(map);
        
        for (String key : keys) {
            lResult.addValue(key, map.get(key), access.get(key));
        }
        
        return lResult;
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
    public Class<? extends ModbusMachineData> getTargetType() {
        return ModbusMachineData.class;
    }
}
