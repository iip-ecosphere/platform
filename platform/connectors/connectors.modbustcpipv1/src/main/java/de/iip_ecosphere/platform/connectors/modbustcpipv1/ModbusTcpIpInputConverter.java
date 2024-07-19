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

package de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;
import java.math.BigInteger;

import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;

/**
 * Overrides methods that are needed to covert data input.
 * 
 * @author Christian Nikolajew
 */
public class ModbusTcpIpInputConverter extends ModelInputConverter {

    /**
     * Creates an instance.
     */
    public ModbusTcpIpInputConverter() {
    }

    @Override
    public short toShort(Object data) throws IOException {
        
        short result;
        
        if (data.getClass() == Integer.class) {
            
            result = ((Integer) data).shortValue();
            
        } else {
            
            result = (short) data;
        }
        
        return result;
    }
    
    @Override
    public int toInteger(Object data) throws IOException {
        
        int result;
        
        if (data.getClass() == Long.class) {
            
            result = ((Long) data).intValue();
            
        } else {
            
            result = (int) data;
        }
        
        return result;
    }
    
    @Override
    public long toLong(Object data) throws IOException {
        
        long result;
        
        if (data.getClass() == BigInteger.class) {
            
            result = ((BigInteger) data).longValue();
            
        } else {
            
            result = (long) data;
        }
        
        return result;
    }
}
