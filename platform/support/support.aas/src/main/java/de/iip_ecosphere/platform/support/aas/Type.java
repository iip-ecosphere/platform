/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

/**
 * Represents an (implementation-independent) AAS (property/variable/value) type.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum Type {

    /**
     * Integer type corresponding to Java for compliance with pre-BaSyx 1.3 code.
     */
    INTEGER, 

    /**
     * Integer type as defined in AAS, may lead to "big" integer in Java.
     */
    AAS_INTEGER,
    NON_POSITIVE_INTEGER,
    NON_NEGATIVE_INTEGER,
    POSITIVE_INTEGER,
    NEGATIVE_INTEGER,
    INT8,
    INT16,
    INT32,
    INT64,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    STRING, 
    LANG_STRING,
    ANY_URI,
    BASE64_BINARY,
    HEX_BINARY,
    NOTATION,
    ENTITY,
    ID,
    IDREF,
    DOUBLE,
    FLOAT, 
    BOOLEAN,
    DURATION, 
    DAY_TIME_DURATION, 
    YEAR_MONTH_DURATION,
    DATE_TIME, 
    DATE_TIME_STAMP, 
    G_DAY, 
    G_MONTH, 
    G_MONTH_DAY, 
    G_YEAR, 
    G_YEAR_MONTH,
    Q_NAME,
    NONE, 
    ANY_TYPE, 
    ANY_SIMPLE_TYPE;
    
}
