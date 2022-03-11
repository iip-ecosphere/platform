/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.serialization;

/**
 * Interface for generated IIP-Ecosphere enums.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface IipEnum {

    /**
    * Returns the model ordinal of this enum literal.
    *
    * @return the model ordinal value
    */
    public int getModelOrdinal();
    
    /**
    * Returns an enum literal via its model ordinal, more specifically the first one declaring the 
    * {@link #getModelOrdinal()} in declaration sequence of the enum constants.
    * 
    * @param <E> the enum type
    * @param enumType the class representing the enum type implementing {@link IipEnum}
    * @param modelOrdinal the ordinal to search for
    * @return the enum literal, may be <b>null</b> for none
    */
    public static <E extends Enum<E> & IipEnum> E valueByModelOrdinal(Class<E> enumType, int modelOrdinal) {
        E result = null;
        // preliminary, currently no lookup
        for (E l : enumType.getEnumConstants()) {
            if (l.getModelOrdinal() == modelOrdinal) {
                result = l;
                break;
            }
        }
        return result;
    }
    
}
