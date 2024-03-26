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

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusNamespace;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

/**
 * The modbus machine command input translator tests.
 * 
 * @param <O> the output datatype
 * 
 * @author Christian Nikolajew
 */
public class ModbusMachineCommandInputTranslator<O>
        extends AbstractConnectorInputTypeTranslator<ModbusMachineCommand, O> {

    private Class<? extends O> sourceType;


    /**
     * Creates a new machine command input translator.
     * 
     * @param sourceType the source type
     */
    public ModbusMachineCommandInputTranslator(Class<? extends O> sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public O from(ModbusMachineCommand data) throws IOException {

        ModelAccess access = getModelAccess();

        if (data.getChangeShortValue()) {
            access.call(ModbusNamespace.NAME_VAR_SHORT_VALUE, data.getShortValue());
        }

        if (data.getChangeIntValue()) {
            access.call(ModbusNamespace.NAME_VAR_INT_VALUE, data.getIntValue());
        }

        if (data.getChangeFloatValue()) {
            access.call(ModbusNamespace.NAME_VAR_FLOAT_VALUE, data.getFloatValue());
        }

        if (data.getChangeLongValue()) {
            access.call(ModbusNamespace.NAME_VAR_LONG_VALUE, data.getLongValue());
        }

        if (data.getChangeDoubleValue()) {
            access.call(ModbusNamespace.NAME_VAR_DOUBLE_VALUE, data.getDoubleValue());
        }

        return null;

    }

    @Override
    public Class<? extends O> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends ModbusMachineCommand> getTargetType() {
        return ModbusMachineCommand.class;
    }

}
