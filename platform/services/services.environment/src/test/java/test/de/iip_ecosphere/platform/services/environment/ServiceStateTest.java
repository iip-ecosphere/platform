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

package test.de.iip_ecosphere.platform.services.environment;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.ServiceState;

/**
 * Tests {@link ServiceState}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceStateTest {

    /**
     * Tests {@link ServiceState} transitions.
     */
    @Test
    public void testTransitions() throws ExecutionException {
        try {
            ServiceState.validateTransition(null, null);
            Assert.fail("No exception thrown");
        } catch (ExecutionException e) {
            // shall occur
        }
        try {
            ServiceState.validateTransition(ServiceState.DEPLOYING, ServiceState.MIGRATING);
            Assert.fail("No exception thrown");
        } catch (ExecutionException e) {
            // shall occur
        }
        
        ServiceState.validateTransition(ServiceState.UNKNOWN, ServiceState.AVAILABLE);
        ServiceState.validateTransition(ServiceState.AVAILABLE, ServiceState.DEPLOYING);
        ServiceState.validateTransition(ServiceState.DEPLOYING, ServiceState.CREATED);
        ServiceState.validateTransition(ServiceState.CREATED, ServiceState.STARTING);
        ServiceState.validateTransition(ServiceState.STARTING, ServiceState.RUNNING);
        ServiceState.validateTransition(ServiceState.RUNNING, ServiceState.FAILED);
        ServiceState.validateTransition(ServiceState.FAILED, ServiceState.RECOVERING);
        ServiceState.validateTransition(ServiceState.RECOVERING, ServiceState.RECOVERED);
        ServiceState.validateTransition(ServiceState.RECOVERED, ServiceState.RUNNING);
        ServiceState.validateTransition(ServiceState.RUNNING, ServiceState.RECONFIGURING);
        ServiceState.validateTransition(ServiceState.RECONFIGURING, ServiceState.RUNNING);
        ServiceState.validateTransition(ServiceState.RUNNING, ServiceState.PASSIVATING);
        ServiceState.validateTransition(ServiceState.PASSIVATING, ServiceState.PASSIVATED);
        ServiceState.validateTransition(ServiceState.PASSIVATED, ServiceState.MIGRATING);
        ServiceState.validateTransition(ServiceState.MIGRATING, ServiceState.ACTIVATING);
        ServiceState.validateTransition(ServiceState.ACTIVATING, ServiceState.RUNNING);
        ServiceState.validateTransition(ServiceState.RUNNING, ServiceState.STOPPING);
        ServiceState.validateTransition(ServiceState.STOPPING, ServiceState.STOPPED);
        ServiceState.validateTransition(ServiceState.STOPPED, ServiceState.AVAILABLE);
        ServiceState.validateTransition(ServiceState.AVAILABLE, ServiceState.UNDEPLOYING);
    }

}
