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

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.ContainerState;

/**
 * Tests {@link ContainerState}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ContainerStateTest {

    /**
     * Tests {@link ContainerState} transitions.
     */
    @Test
    public void testTransitions() throws ExecutionException {
        try {
            ContainerState.validateTransition(null, null);
            Assert.fail("No exception thrown");
        } catch (ExecutionException e) {
            // shall occur
        }
        try {
            ContainerState.validateTransition(ContainerState.DEPLOYING, ContainerState.MIGRATING);
            Assert.fail("No exception thrown");
        } catch (ExecutionException e) {
            // shall occur
        }
        
        ContainerState.validateTransition(ContainerState.UNKNOWN, ContainerState.AVAILABLE);
        ContainerState.validateTransition(ContainerState.AVAILABLE, ContainerState.DEPLOYING);
        ContainerState.validateTransition(ContainerState.DEPLOYING, ContainerState.DEPLOYED);
        ContainerState.validateTransition(ContainerState.DEPLOYED, ContainerState.FAILED);
        ContainerState.validateTransition(ContainerState.FAILED, ContainerState.DEPLOYED);
        ContainerState.validateTransition(ContainerState.DEPLOYED, ContainerState.MIGRATING);
        ContainerState.validateTransition(ContainerState.MIGRATING, ContainerState.DEPLOYED);
        ContainerState.validateTransition(ContainerState.DEPLOYED, ContainerState.UPDATING);
        ContainerState.validateTransition(ContainerState.UPDATING, ContainerState.DEPLOYED);
        ContainerState.validateTransition(ContainerState.DEPLOYED, ContainerState.STOPPING);
        ContainerState.validateTransition(ContainerState.STOPPING, ContainerState.STOPPED);
        ContainerState.validateTransition(ContainerState.STOPPED, ContainerState.UNDEPLOYING);
        ContainerState.validateTransition(ContainerState.UNDEPLOYING, ContainerState.UNKNOWN);
    }

}
