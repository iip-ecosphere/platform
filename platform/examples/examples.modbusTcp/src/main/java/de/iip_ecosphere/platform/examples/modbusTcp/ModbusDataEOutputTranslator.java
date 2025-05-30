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

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

/**
 * The modbus ModbusDataE output translator for tests.
 * 
 * @param <S> the source datatype
 * 
 * @author Christian Nikolajew
 */
public class ModbusDataEOutputTranslator<S> extends AbstractConnectorOutputTypeTranslator<S, ModbusDataE> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Constructor.
     * 
     * @param withNotifications true = with Notifications ; false = without Notification
     * @param sourceType the sourceType
     */
    public ModbusDataEOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }

    @Override
    public ModbusDataE to(Object source) throws IOException {

        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        ModbusDataE result = new ModbusDataE();
        
        result.setU12((float) access.get("U12"));
        result.setU23((float) access.get("U23"));
        result.setU31((float) access.get("U31"));
        result.setU1((float) access.get("U1"));
        result.setU2((float) access.get("U2"));
        result.setU3((float) access.get("U3"));
        result.setI1((float) access.get("I1"));
        result.setI2((float) access.get("I2"));
        result.setI3((float) access.get("I3"));
        result.setTotalActivePower((float) access.get("Total active power"));
        
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
