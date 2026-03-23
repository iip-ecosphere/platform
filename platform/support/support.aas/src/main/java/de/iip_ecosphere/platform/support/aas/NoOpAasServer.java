/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.io.IOException;

import de.iip_ecosphere.platform.support.NoOpServer;

/**
 * An AAS server instance that does noting.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NoOpAasServer extends NoOpServer implements AasServer {

    @Override
    public void deploy(Aas aas) throws IOException {
    }

    @Override
    public void deploy(Aas aas, Submodel submodel) throws IOException {
    }
    
    @Override
    public AasServer start() {
        return this;
    }

}
