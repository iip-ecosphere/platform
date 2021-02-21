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

package de.iip_ecosphere.platform.connectors;

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * Selects a protocol adapter based on the given machine data.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public interface AdapterSelector<O, I, CO, CI> {

    /**
     * Returns the responsible protocol adapter for southbound output.
     * 
     * @param data the data object
     * @return the protocol adapter (must not be <b>null</b>)
     */
    public ProtocolAdapter<O, I, CO, CI> selectSouthOutput(O data); 

    /**
     * Returns the responsible protocol adapter for northbound input.
     * 
     * @param data the data object
     * @return the protocol adapter (must not be <b>null</b>)
     */
    public ProtocolAdapter<O, I, CO, CI> selectNorthInput(CI data); 

}
