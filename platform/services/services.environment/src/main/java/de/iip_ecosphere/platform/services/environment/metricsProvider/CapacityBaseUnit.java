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

package de.iip_ecosphere.platform.services.environment.metricsProvider;

/**
 * This enum represents a capacity base unit.<br>
 * This base units will be used in the MetricsProvider to correctly set the
 * memory and disk capacity base units in a secure and simple way.<br>
 * The base units that can be represented by this enum and, as a result, can be
 * used in the metrics provider are:
 * <ul>
 * <li>BYTES</li>
 * <li>KILOBYTES</li>
 * <li>MEGABYTES</li>
 * <li>GIGABYTES</li>
 * <li>TERABYTES</li>
 * </ul>
 * Each value represented in this enum will provide the value in bytes of a
 * single unit as well as a lower case representation of the base unit.
 * 
 * @author Miguel Gomez
 */
public enum CapacityBaseUnit {

    BYTES {
        @Override
        public double byteValue() {
            return 1.0;
        }
    },
    KILOBYTES {
        @Override
        public double byteValue() {
            return 1024.0;
        }
    },
    MEGABYTES {
        @Override
        public double byteValue() {
            return 1048576.0;
        }
    },
    GIGABYTES {
        @Override
        public double byteValue() {
            return 1073741824.0;
        }
    },
    TERABYTES {
        @Override
        public double byteValue() {
            return 1099511627776.0;
        }
    };

    /**
     * Retrieves the amount of bytes per unit of this CapacityBaseUnit.
     * 
     * @return number of bytes per unit of this CapacityBaseUnit
     */
    public abstract double byteValue();

    /**
     * Retrieves a String representing the lowercase name of this CapacityBaseUnit.
     * 
     * @return name of this capacity base unit in lowercase letters
     */
    public String stringValue() {
        return this.toString().toLowerCase();
    }
}
