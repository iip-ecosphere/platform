/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.ecsRuntime.Configuration;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;

/**
 * A test service factory descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MyEcsFactoryDescriptor implements EcsFactoryDescriptor {

    // do not rename this class! Java Service Loader
    
    @Override
    public MyContainerManager createContainerManagerInstance() {
        return new MyContainerManager();
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration();
    }

}
