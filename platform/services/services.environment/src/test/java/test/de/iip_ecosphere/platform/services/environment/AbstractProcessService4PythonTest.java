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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import de.iip_ecosphere.platform.services.environment.AbstractStringProcessService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Tests {@link AbstractProcessService} and {@link AbstractStringProcessService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractProcessService4PythonTest {

    /**
     * Defines a simple string-in-string-out test service.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestService extends AbstractStringProcessService<String, String> {

        /**
         * Creates an instance of the service with the required type translators.
         * 
         * @param inTrans the input translator
         * @param outTrans the output translator
         * @param callback called when data from the service is available
         * @param yaml the service description 
         */
        protected TestService(TypeTranslator<String, String> inTrans, TypeTranslator<String, String> outTrans,
            ReceptionCallback<String> callback, YamlService yaml) {
            super(inTrans, outTrans, callback, yaml);
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
        public void reconfigure(Map<String, String> values) throws ExecutionException {
        }

        @Override
        protected int getWaitTimeBeforeDestroy() {
            return 2000;
        }

        @Override
        protected ServiceState start() throws ExecutionException {
            YamlProcess sSpec = getProcessSpec();
            File exe = selectNotNull(sSpec, s -> s.getExecutablePath(), new File("python3")); 
            File home = selectNotNull(sSpec, s -> s.getHomePath(), new File("src/test/python"));
            
            List<String> args = new ArrayList<>();
            args.add("ForwardingApp.py");
            addProcessSpecCmdArg(args);
            createAndConfigureProcess(exe, true, home, args);
            return null; 
        }
        
    }

    /**
     * In-data type translator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class InDataTypeTranslator implements TypeTranslator<String, String> {

        @Override
        public String from(String data) throws IOException {
            return null; // shall be filled, not needed here
        }

        @Override
        public String to(String source) throws IOException {
            return source;
        }
        
    }

    /**
     * Out-data type translator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class OutDataTypeTranslator implements TypeTranslator<String, String> {

        @Override
        public String from(String data) throws IOException {
            return null; // shall be filled, not needed here
        }

        @Override
        public String to(String source) throws IOException {
            return source; // just pass-through
        }
        
    }

    /**
     * Tests the process-based service classes.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testProcessService() throws ExecutionException, IOException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        ReceptionCallback<String> rcp = new ReceptionCallback<String>() {

            @Override
            public void received(String data) {
                Assert.assertTrue(data != null && data.length() > 0);
                receivedCount.incrementAndGet();
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        };

        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("Test");
        sDesc.setVersion(new Version("0.0.1"));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("Test");
        sDesc.setDeployable(true);
        // no process information needed, defaults sufficient
        
        TestService service = new TestService(new InDataTypeTranslator(), new OutDataTypeTranslator(), rcp, sDesc);
        service.setState(ServiceState.STARTING);
        service.process("test");
        service.process("test");
        service.process("test");
        service.setState(ServiceState.STOPPING);
        Assert.assertEquals(3, receivedCount.get()); // 3 in, 3 out

        service.activate();
        service.passivate();
    }
    
    /**
     * Tests the naming support methods.
     */
    @Test
    public void testNamingSupport() {
        Assert.assertTrue(AbstractProcessService.getOsArch(true).length() > 0);
        Assert.assertTrue(AbstractProcessService.getOsArch(false).length() > 0);
        Assert.assertNotNull(AbstractProcessService.getExecutableSuffix()); // linux == 0
        String exec = AbstractProcessService.getExecutableName("test", "1.2.3");
        Assert.assertNotNull(exec);
        Assert.assertTrue(exec.indexOf("test") >= 0);
        Assert.assertTrue(exec.indexOf("1.2.3") >= 0);
    }
    
}

