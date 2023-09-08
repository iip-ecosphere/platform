/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.environment;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.Assert;

import de.iip_ecosphere.platform.services.environment.AbstractDelegatingService;
import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Tests {@link AbstractDelegatingService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractDelegatingServiceTest {

    /**
     * A test service.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class MyService extends AbstractService {

        // checkstyle: stop parameter number check
        
        /**
         * Creates an abstract service.
         * 
         * @param id the id of the service
         * @param name the name of the service
         * @param version the version of the service
         * @param description a description of the service, may be empty
         * @param isDeployable whether the service is decentrally deployable
         * @param isTopLevel whether the service is a top-level (non-nested) service
         * @param kind the service kind
         */
        MyService(String id, String name, Version version, String description, boolean isDeployable,
            boolean isTopLevel, ServiceKind kind) {
            super(id, name, version, description, isDeployable, isTopLevel, kind);
        }

        // checkstyle: resume parameter number check

        /**
         * Creates an abstract service from YAML information.
         * 
         * @param yaml the service information as read from YAML
         * @see #initializeFrom(YamlService)
         */
        MyService(YamlService yaml) {
            super(yaml);
        }
        
        @Override
        public void migrate(String resourceId) throws ExecutionException {
        }

        @Override
        public void update(URI location) throws ExecutionException {
        }

        @Override
        public void switchTo(String targetId) throws ExecutionException {
        }
        
    }
    
    /**
     * A test delegating service.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class DService extends AbstractDelegatingService<MyService> {

        private static MyService service;

        /**
         * Creates a service instance always delegating to {@code #service}.
         * 
         * @param yaml the YAML service description
         */
        public DService(YamlService yaml) {
            super(yaml);
        }

        /**
         * Sets the service to delegate to.
         * 
         * @param svc the new service
         */
        static void setService(MyService svc) {
            service = svc;
        }

        @Override
        protected MyService createService(YamlService yaml) {
            return service;
        }
        
    }
    
    /**
     * Tests {@link AbstractDelegatingService}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void test() throws ExecutionException {
        MyService service = new MyService("id", "name", new Version("1.23"), "desc", true, true, 
            ServiceKind.SOURCE_SERVICE);
        DService.setService(service);
        DService dService = new DService(new YamlService());
        
        Assert.assertEquals(service.getId(), dService.getId());
        Assert.assertEquals(service.getName(), dService.getName());
        Assert.assertEquals(service.getVersion(), dService.getVersion());
        Assert.assertEquals(service.getDescription(), dService.getDescription());
        Assert.assertEquals(service.isDeployable(), dService.isDeployable());
        Assert.assertEquals(service.isTopLevel(), dService.isTopLevel());
        Assert.assertEquals(service.getKind(), dService.getKind());
        Assert.assertEquals(service.getState(), dService.getState());
        service.setState(ServiceState.STARTING);
        Assert.assertEquals(service.getState(), dService.getState());
        dService.migrate("");
        dService.update(null);
        dService.activate();
        dService.passivate();
        dService.reconfigure(new HashMap<>());
        dService.switchTo("");
        
        DService.setService(null);
    }

}
