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
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

/**
 * The ModbusCommandE input translator for tests.
 * 
 * @param <O> the output datatype
 * 
 * @author Christian Nikolajew
 */
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
