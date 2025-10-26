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
import de.iip_ecosphere.platform.support.json.JsonIgnore;

/**
 * A data interface for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfiguredName("Data")
public interface Data {

    /**
     * Returns the value. [JSON]
     * 
     * @return the value
     */
    @JsonIgnore
    public int getValue();
    
    /**
     * Changes the value. [JSON]
     * 
     * @param value the new value
     */
    @JsonIgnore
    public void setValue(int value);
    
}
