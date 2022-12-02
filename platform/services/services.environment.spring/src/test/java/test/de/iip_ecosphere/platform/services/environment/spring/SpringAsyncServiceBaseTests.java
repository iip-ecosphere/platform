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

package test.de.iip_ecosphere.platform.services.environment.spring;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.spring.SpringAsyncServiceBase;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Tests {@link SpringAsyncServiceBase}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringAsyncServiceBaseTests {
    
    // TODO more tests needed
    
    private static class MyService implements Service {

        private String id;
        
        /**
         * Creates an instance.
         * 
         * @param id the service id
         */
        private MyService(String id) {
            this.id = id;
        }
        
        @Override
        public String getId() {
            return id;
        }

        @Override
        public ServiceState getState() {
            return ServiceState.AVAILABLE;
        }

        @Override
        public void setState(ServiceState state) throws ExecutionException {
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public Version getVersion() {
            return new Version("0.0.0");
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public boolean isDeployable() {
            return false;
        }

        @Override
        public boolean isTopLevel() {
            return true;
        }

        @Override
        public ServiceKind getKind() {
            return ServiceKind.TRANSFORMATION_SERVICE;
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

        @Override
        public void activate() throws ExecutionException {
        }

        @Override
        public void passivate() throws ExecutionException {
        }

        @Override
        public void reconfigure(Map<String, String> values) throws ExecutionException {
        }
        
    }
    
    /**
     * Tests {@link SpringAsyncServiceBase}.
     */
    @Test
    public void testGetAppInstIdSuffix() {
        final String separator = "_";
        // legacy case
        String res = SpringAsyncServiceBase.getAppInstIdSuffix(new MyService("myService"), separator);
        Assert.assertNotNull(res);
        Assert.assertEquals("", res);
        
        final String instId = "007";
        String sId = ServiceBase.composeId("sId", "aId", instId);
        res = SpringAsyncServiceBase.getAppInstIdSuffix(new MyService(sId), separator);
        Assert.assertNotNull(res);
        Assert.assertEquals(separator + instId, res);
    }

}
