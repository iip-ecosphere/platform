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

package test.de.iip_ecosphere.platform.connectors;

import de.iip_ecosphere.platform.transport.serialization.IipEnum;

/**
 * A test enum with model ordinal.
 * 
 * @author Holger Eichelberger, SSE
 */
enum MyEnum implements IipEnum {
    
    TEST1(10),
    TEST2(20);
    
    private int modelOrdinal;
    
    /**
     * Creates an enum constant.
     * 
     * @param modelOrdinal the model ordinal value to use
     */
    private MyEnum(int modelOrdinal) {
        this.modelOrdinal = modelOrdinal;
    }

    @Override
    public int getModelOrdinal() {
        return modelOrdinal;
    }
    
}