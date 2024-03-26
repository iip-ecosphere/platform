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

/**
 * Implements Names for operations and variables. 
 * 
 * @author Christian Nikolaew
 */
public class ModbusNamespace {

    public static final String NAME_OP_WRITE_SHORT = "w_short";
    public static final String NAME_OP_WRITE_INT = "w_int";
    public static final String NAME_OP_WRITE_FLOAT = "w_float";
    public static final String NAME_OP_WRITE_LONG = "w_long";
    public static final String NAME_OP_WRITE_DOUBLE = "w_double";

    public static final String NAME_VAR_SHORT_VALUE = "short";
    public static final String NAME_VAR_INT_VALUE = "int";
    public static final String NAME_VAR_FLOAT_VALUE = "float";
    public static final String NAME_VAR_LONG_VALUE = "long";
    public static final String NAME_VAR_DOUBLE_VALUE = "double";

    /*
    public static String getName_OP_WriteShort() {
        return NAME_OP_WRITE_SHORT;
    }

    public static String getName_OP_WriteInt() {
        return NAME_OP_WRITE_INT;
    }

    public static String getName_OP_WriteFloat() {
        return NAME_OP_WRITE_FLOAT;
    }

    public static String getName_OP_WriteLong() {
        return NAME_OP_WRITE_LONG;
    }

    public static String getName_OP_WriteDouble() {
        return NAME_OP_WRITE_DOUBLE;
    }

    public static String getName_VAR_ShortValue() {
        return NAME_VAR_SHORT_VALUE;
    }

    public static String getName_VAR_IntValue() {
        return NAME_VAR_INT_VALUE;
    }

    public static String getName_VAR_FloatValue() {
        return NAME_VAR_FLOAT_VALUE;
    }

    public static String getName_VAR_LongValue() {
        return NAME_VAR_LONG_VALUE;
    }

    public static String getName_VAR_DoubleValue() {
        return NAME_VAR_DOUBLE_VALUE;
    }*/
}
