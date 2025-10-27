/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package iip.datatypes;

import de.iip_ecosphere.platform.support.ConfiguredName;
import de.iip_ecosphere.platform.support.Ignore;
import de.iip_ecosphere.platform.support.StringUtils;

/**
 * Implements {@link Data}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataImpl implements Data {
    
    @ConfiguredName("iField")
    private int value;

    /**
     * Default constructor. Fields are pre-allocated with default Java values.
     */
    public DataImpl() {
    }

    /**
     * Copy constructor.
     *
     * @param from the instance to copy the values from
     */
    public DataImpl(Data from) {
        this.value = from.getValue();
    }

    @Override
    @Ignore
    public int getValue() {
        return value;
    }

    @Override
    @Ignore
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        hc += Integer.hashCode(getValue());
        return hc;
    }

    @Override
    public boolean equals(Object other) {
        boolean eq;
        if (other instanceof Data) {
            Data oth = (Data) other;
            eq = true;
            eq &= getValue() == oth.getValue();
        } else {
            eq = false;
        }
        return eq;
    }

    @Override
    public String toString() {
        return StringUtils.toStringShortStyle(this);
    }

}
