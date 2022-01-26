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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.PythonAsyncProcessService;
import de.iip_ecosphere.platform.services.environment.PythonSyncProcessService;
import de.iip_ecosphere.platform.services.environment.AbstractPythonProcessService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Tests the generic Python process service {@link AbstractPythonProcessService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonProcessServiceTest {

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
     */
    @Test
    public void testAsyncProcessService() throws ExecutionException, IOException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("Test");
        sDesc.setVersion(new Version("0.0.1"));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("Test");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        pDesc.setExecutable("ForwardingApp.py");
        pDesc.setHomePath("src/test/python");
        sDesc.setProcess(pDesc);
        
        final String typeName = "S"; // same symbolic type name for in/output
        AbstractPythonProcessService service = new PythonAsyncProcessService(sDesc);
        service.registerInputTypeTranslator(String.class, typeName, new InDataTypeTranslator());
        service.registerOutputTypeTranslator(String.class, typeName, new OutDataTypeTranslator());
        service.attachIngestor(String.class, typeName, new DataIngestor<String>() {

            @Override
            public void ingest(String data) {
                receivedCount.incrementAndGet();
            } 
        });
        service.setState(ServiceState.STARTING);
        service.process(typeName, "test");
        service.process(typeName, "test");
        service.process(typeName, "test");
        TimeUtils.sleep(1000);
        
        service.setState(ServiceState.STOPPING);
        Assert.assertEquals(3, receivedCount.get()); // 3 in, 3 out

        service.activate();
        service.passivate();
    }
    
    
    /**
     * Tests the process-based service classes.
     */
    @Test
    public void testSyncProcessService() throws ExecutionException, IOException {
        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("Test");
        sDesc.setVersion(new Version("0.0.1"));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("Test");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        pDesc.setExecutable("ForwardingApp.py");
        pDesc.setHomePath("src/test/python");
        sDesc.setProcess(pDesc);
        
        final String typeName = "S"; // same symbolic type name for in/output
        AbstractPythonProcessService service = new PythonSyncProcessService(sDesc);
        service.registerInputTypeTranslator(String.class, typeName, new InDataTypeTranslator());
        service.registerOutputTypeTranslator(String.class, typeName, new OutDataTypeTranslator());
        service.setState(ServiceState.STARTING);
        Assert.assertEquals("test", service.process(typeName, "test"));
        Assert.assertEquals("test", service.process(typeName, "test"));
        Assert.assertEquals("test", service.process(typeName, "test"));
        service.setState(ServiceState.STOPPING);

        service.activate();
        service.passivate();
    }

    
}
