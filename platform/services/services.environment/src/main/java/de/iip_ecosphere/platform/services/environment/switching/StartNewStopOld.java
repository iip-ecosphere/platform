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

package de.iip_ecosphere.platform.services.environment.switching;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceState;

/**
 * Starts the new service first, stops then the old.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StartNewStopOld extends AbstractStrategy {
    
    @Override
    public Service doSwitch(Service sOld, Service sNew) {
        Service result = sOld;
        try {
            getLogger().info("Starting new service {}", sNew.getId());
            sNew.setState(ServiceState.STARTING);
            getLogger().info("Stopping old service {}", sNew.getId());
            sOld.setState(ServiceState.STOPPING);
            result = sNew;
        } catch (ExecutionException e) {
            getLogger().error("Cannot switch to new service {}: {}", sNew.getId(), e.getMessage());
        }
        return result;
    }

}
