/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

/**
 * Helper functions for active AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasUtils {
    
    /**
     * Reads the {@code index} argument from {@code} args as String.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the value
     */
    public static String readString(Object[] args, int index, String dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        return null == param ? dflt : param.toString();
    }
    
    /**
     * Reads the {@code index} argument from {@code} args as int.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value is no int...
     * @return the value
     */
    public static int readInt(Object[] args, int index, int dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        int result = dflt;
        if (null != param) {
            try {
                result = Integer.parseInt(param.toString());
            } catch (NumberFormatException e) {
                // handled by result = deflt
            }
        }
        return result;
    }

}
