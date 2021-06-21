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

package test.de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.Service;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests parts of the service interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceTest {

    /**
     * Tests part of the service interface.
     */
    @Test
    public void testService() {
        Service instance = AbstractService.createInstance(
            "test.de.iip_ecosphere.platform.services.environment.MyService", Service.class);
        Assert.assertNotNull(instance);
    }
    
}
