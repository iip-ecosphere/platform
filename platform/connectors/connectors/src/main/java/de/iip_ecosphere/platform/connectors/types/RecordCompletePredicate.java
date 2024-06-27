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

package de.iip_ecosphere.platform.connectors.types;

import java.util.Map;

/**
 * A predicate to be used when obtaining a complex record field-by-field from an external source.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface RecordCompletePredicate {
    
    /**
     * The default predicate just tells if the field is already in values.
     */
    public static final RecordCompletePredicate DEFAULT = (v, f) -> v.containsKey(f);
    
    /**
     * Returns whether a record with given field-value mapping can be considered to be complete
     * when reading {@code field} without field being yet added to {@code values}.
     * 
     * @param values the values to test
     * @param field the name of the field being in processing
     * @return {@code true} for complete, {@code false} for incomplete
     */
    public boolean isComplete(Map<String, Object> values, String field);

}
