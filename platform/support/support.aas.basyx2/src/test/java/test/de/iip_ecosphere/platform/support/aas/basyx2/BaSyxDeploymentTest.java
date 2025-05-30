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

package test.de.iip_ecosphere.platform.support.aas.basyx2;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Tests deployment scenarios.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentTest extends test.de.iip_ecosphere.platform.support.aas.DeploymentTest {
    
    @Override
    protected Endpoint createDependentEndpoint(ServerAddress address, String endpoint) {
        return new Endpoint(address.getSchema(), endpoint); // turn to emphemeral, v3 requires 4 servers
    }
    
}
