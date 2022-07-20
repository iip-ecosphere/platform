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

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.FamilyServiceStub;
import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceState;

/**
 * Tests default service implementations.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultServiceImplTest {

    /**
     * Tests {@link DefaultServiceImpl}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testDefaultServiceImpl() throws ExecutionException {
        DefaultServiceImpl service = new DefaultServiceImpl("simpleStream-create", 
            AbstractService.getResourceAsStream(
                DefaultServiceImplTest.class.getClassLoader(), "deployment.yml"));
        testOps(service);
    }

    /**
     * Some rather simple tests for a service instance.
     * 
     * @param service the service instance
     * @throws ExecutionException shall not occur
     */
    private void testOps(Service service) throws ExecutionException {
        Assert.assertNotNull(service.getId());
        Assert.assertNotNull(service.getDescription());
        Assert.assertNotNull(service.getName());
        Assert.assertNotNull(service.getKind());
        Assert.assertNull(service.getParameterConfigurer("abc"));
        Assert.assertNotNull(service.getState());
        Assert.assertNotNull(service.getVersion());
        Assert.assertTrue(service.isTopLevel());
        Assert.assertTrue(service.isDeployable());

        service.setState(ServiceState.STARTING);
        Assert.assertEquals(ServiceState.RUNNING, service.getState());
        service.migrate("");
        service.passivate();
        service.activate();
        service.switchTo("");
        service.update(null);
    }

    /**
     * Tests {@link DefaultServiceImpl}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testFamilyServiceStub() throws ExecutionException {
        DefaultServiceImpl service = new DefaultServiceImpl("simpleStream-create", 
            AbstractService.getResourceAsStream(
                DefaultServiceImplTest.class.getClassLoader(), "deployment.yml"));
        FamilyServiceStub family = new FamilyServiceStub("simpleStream-create", 
                AbstractService.getResourceAsStream(
                    DefaultServiceImplTest.class.getClassLoader(), "deployment.yml"));
        family.setActiveMemberSupplier(() -> service);
        testOps(family);
        Assert.assertEquals(ServiceState.RUNNING, service.getState());
    }

}
