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

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.services.Version;

/**
 * Abstract {@link ContainerDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractContainerDescriptor implements ContainerDescriptor {
    
    // TODO basic implementation

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Version getVersion() {
        return new Version();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ContainerState getState() {
        return ContainerState.UNKOWN;
    }
    
}
