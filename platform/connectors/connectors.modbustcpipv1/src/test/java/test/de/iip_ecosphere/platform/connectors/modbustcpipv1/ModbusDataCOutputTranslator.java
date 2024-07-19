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

package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

/**
 * The modbus ModbusDataC output translator for tests.
 * 
 * @param <S> the source datatype
 * 
 * @author Christian Nikolajew
 */
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
