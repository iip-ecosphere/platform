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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.AbstractDelegatingMultiService;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import test.de.iip_ecosphere.platform.services.environment.AbstractDelegatingServiceTest.MyService;

/**
 * Tests {@link AbstractDelegatingMultiService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractDelegatingMultiServiceTest {

    /**
     * A test delegating service.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class DService extends AbstractDelegatingMultiService<MyService> {

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

        @Override
        protected void processImpl(String inType, String data) throws IOException {
            handleResult(String.class, data, inType); // pass back
        }
        
    }

    
    /**
     * Tests {@link AbstractDelegatingMultiService}.
     */
    @Test
    public void test() {
        final Set<String> received = new HashSet<>();
        MyService service = new MyService(new YamlService());
        DService.setService(service);
        DService dService = new DService(new YamlService());
        
        dService.registerInputTypeTranslator(String.class, "data", TypeTranslators.STRING);
        dService.registerOutputTypeTranslator(String.class, "data", TypeTranslators.STRING);
        dService.attachIngestor(String.class, "data", data -> received.add(data));
        dService.processQuiet("data", "ABC");
        dService.processSyncQuiet("data", "CBA", "data");

        Assert.assertTrue(received.contains("ABC"));
        Assert.assertTrue(received.contains("CBA"));
        
        DService.setService(null);
    }

}
