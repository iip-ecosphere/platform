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
import java.util.ArrayList;
import java.util.List;
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
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13Impl;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13InTranslator;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13OutTranslator;

/**
 * Tests the generic Python process service {@link AbstractPythonProcessService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonProcessServiceTest {

    /**
     * Composes the basic command line arguments for this test. We assume that the service modules are in the 
     * "src/test/python" folder and we set the home folder of the Python process to "src/main/python" where the 
     * service environment is located. This may differ in a real integration, e.g., both parts in one sub-folder 
     * of temp.
     * 
     * @return the basic command line arguments
     */
    private List<String> composeCmdLineArguments() {
        File f = new File("src/test/python");
        List<String> args = new ArrayList<String>();
        args.add("--mode");
        args.add("console");
        args.add("--modulesPath");
        args.add(f.getAbsolutePath());
        args.add("--sid");
        args.add("1234");
        return args;
    }
    
    /**
     * Tests the process-based service classes.
     */
    @Test
    public void testAsyncProcessService() throws ExecutionException, IOException {
        AtomicInteger receivedStringCount = new AtomicInteger(0);
        AtomicInteger receivedRec13Count = new AtomicInteger(0);
        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("Test");
        sDesc.setVersion(new Version("0.0.1"));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("Test");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        //pDesc.setExecutable("ForwardingApp.py");
        pDesc.setHomePath("src/main/python");
        pDesc.setCmdArg(composeCmdLineArguments());
        sDesc.setProcess(pDesc);
        
        
        final String stringTypeName = "S"; // same symbolic type name for in/output
        final String rec13TypeName = "Rec13";
        AbstractPythonProcessService service = new PythonAsyncProcessService(sDesc);
        service.registerInputTypeTranslator(String.class, stringTypeName, TypeTranslators.STRING);
        service.registerOutputTypeTranslator(String.class, stringTypeName, TypeTranslators.STRING);
        service.attachIngestor(String.class, stringTypeName, new DataIngestor<String>() {

            @Override
            public void ingest(String data) {
                receivedStringCount.incrementAndGet();
            } 
        });
        service.registerInputTypeTranslator(Rec13.class, rec13TypeName, new Rec13InTranslator());
        service.registerOutputTypeTranslator(Rec13.class, rec13TypeName, new Rec13OutTranslator());
        service.attachIngestor(Rec13.class, rec13TypeName, new DataIngestor<Rec13>() {

            @Override
            public void ingest(Rec13 data) {
                receivedRec13Count.incrementAndGet();
            } 
        });
        service.setState(ServiceState.STARTING);
        service.process(stringTypeName, "test");
        service.process(stringTypeName, "test");
        service.processQuiet(stringTypeName, "test");
        Rec13 r = new Rec13Impl();
        r.setIntField(10);
        r.setStringField("abba");
        service.process(rec13TypeName, r);
        TimeUtils.sleep(1000);
        
        service.setState(ServiceState.STOPPING);
        Assert.assertEquals(3, receivedStringCount.get()); // 3 in, 3 out
        //Assert.assertEquals(1, receivedStringCount.get()); // 1 in, 1 out // TODO enable

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
        //pDesc.setExecutable("ForwardingApp.py");
        pDesc.setHomePath("src/main/python");
        pDesc.setCmdArg(composeCmdLineArguments());
        sDesc.setProcess(pDesc);
        
        final String stringTypeName = "S"; // same symbolic type name for in/output
        final String rec13TypeName = "Rec13";
        AbstractPythonProcessService service = new PythonSyncProcessService(sDesc);
        service.registerInputTypeTranslator(String.class, stringTypeName, TypeTranslators.STRING);
        service.registerOutputTypeTranslator(String.class, stringTypeName, TypeTranslators.STRING);
        service.registerInputTypeTranslator(Rec13.class, rec13TypeName, new Rec13InTranslator());
        service.registerOutputTypeTranslator(Rec13.class, rec13TypeName, new Rec13OutTranslator());
        
        service.setState(ServiceState.STARTING);
        Assert.assertEquals("test", service.process(stringTypeName, "test"));
        Assert.assertEquals("test", service.process(stringTypeName, "test"));
        Assert.assertEquals("test", service.processQuiet(stringTypeName, "test"));
        Rec13 r = new Rec13Impl();
        r.setIntField(10);
        r.setStringField("abba");
        Assert.assertEquals(r, service.processQuiet(rec13TypeName, r));
        service.setState(ServiceState.STOPPING);

        service.activate();
        service.passivate();
    }

    
}
