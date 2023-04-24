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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.EnvironmentSetup;
import de.iip_ecosphere.platform.services.environment.PythonAsyncProcessService;
import de.iip_ecosphere.platform.services.environment.PythonSyncProcessService;
import de.iip_ecosphere.platform.services.environment.PythonWsProcessService;
import de.iip_ecosphere.platform.services.environment.AbstractPythonProcessService;
import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.services.environment.YamlServer;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13Impl;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13InTranslator;
import test.de.iip_ecosphere.platform.services.environment.pythonEnv.Rec13OutTranslator;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Tests the generic Python process service {@link AbstractPythonProcessService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonProcessServiceTest {

    private String stringParam = null;
    
    /**
     * Composes the basic command line arguments for this test. We assume that the service modules are in the 
     * "src/test/python" folder and we set the home folder of the Python process to "src/main/python" where the 
     * service environment is located. This may differ in a real integration, e.g., both parts in one sub-folder 
     * of temp.
     * 
     * @param serviceId the service id to use
     * @return the basic command line arguments
     */
    private List<String> composeCmdLineArguments(String serviceId) {
        return composeCmdLineArguments(serviceId, "console", -1);
    }

    /**
     * Composes the basic command line arguments for this test. We assume that the service modules are in the 
     * "src/test/python" folder and we set the home folder of the Python process to "src/main/python" where the 
     * service environment is located. This may differ in a real integration, e.g., both parts in one sub-folder 
     * of temp.
     * 
     * @param serviceId the service id to use
     * @param mode the service environment mode, e.g., "console" or "REST"
     * @param port the port for "REST", ignored if zero or negative
     * @return the basic command line arguments
     */
    private List<String> composeCmdLineArguments(String serviceId, String mode, int port) {
        File f = new File("src/test/python");
        List<String> args = new ArrayList<String>();
        args.add("--mode");
        args.add(mode);
        if (port > 0) {
            args.add("--port");
            args.add(String.valueOf(port));
        }
        args.add("--modulesPath");
        args.add(f.getAbsolutePath());
        args.add("--sid");
        args.add(serviceId);
        return args;
    }

    /**
     * Tests the process-based service classes.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testAsyncProcessService() throws ExecutionException, IOException {
        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("Test");
        sDesc.setVersion(new Version("0.0.1"));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("Test");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        pDesc.setHomePath("src/main/python");
        pDesc.setCmdArg(composeCmdLineArguments("1234"));
        sDesc.setProcess(pDesc);
        
        testService(new PythonAsyncProcessService(sDesc));
    }

    /**
     * Tests the Websocket-based service classes.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testWsProcessService() throws ExecutionException, IOException {
        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("Test");
        sDesc.setVersion(new Version("0.0.1"));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("Test");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        pDesc.setHomePath("src/main/python");
        pDesc.setCmdArg(composeCmdLineArguments("1234"));
        sDesc.setProcess(pDesc);
        
        testService(new PythonWsProcessService(sDesc));
    }

    /**
     * Tests the Python service environment integration.
     * 
     * @param service the service instance to test
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    private void testService(PythonAsyncProcessService service) throws ExecutionException, IOException {
        AtomicInteger receivedStringCount = new AtomicInteger(0);
        AtomicInteger receivedRec13Count = new AtomicInteger(0);
        final String stringTypeName = "S"; // same symbolic type name for in/output
        final String rec13TypeName = "Rec13";

        service.enableFileDeletion(false); // do not remove src/main/python!
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
        service.addParameterConfigurer(c -> AbstractService.addConfigurer(
            c, "myParam", String.class, TypeTranslators.STRING, v -> stringParam = v));
        Assert.assertNotNull(service.getParameterConfigurer("myParam"));
        Map<String, String> rcf = new HashMap<>();
        service.getParameterConfigurer("myParam").addValue(rcf, "VALUE");
        service.reconfigure(rcf); // set default values
        Assert.assertEquals("VALUE", stringParam); // "service" local value, set directly
        service.setState(ServiceState.STARTING);
        service.process(stringTypeName, "test");
        service.process(stringTypeName, "test");
        service.processQuiet(stringTypeName, "test");
        rcf.clear();
        rcf.put("myParam", "VALUE-R");
        service.reconfigure(rcf); // set runtime values
        Assert.assertEquals("VALUE-R", stringParam); // "service" local value, set directly
        Rec13 r = new Rec13Impl();
        r.setIntField(10);
        r.setStringField("abba");
        service.process(rec13TypeName, r);
        TimeUtils.sleep(1000);
        
        Assert.assertTrue(service.getAvgResponseTime() >= 0); // no real heavy-weight calculation in Python
        
        service.setState(ServiceState.STOPPING);
        Assert.assertEquals(3, receivedStringCount.get()); // 3 in, 3 out
        //Assert.assertEquals(1, receivedStringCount.get()); // 1 in, 1 out // TODO enable

        service.activate();
        service.passivate();
    }

    /**
     * Extension of python async process service to count the number of processed items for asserting.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class CountingAsyncPythonProcessService extends PythonAsyncProcessService {

        private int countProcess;
        
        /**
         * Creates an instance.
         * 
         * @param yaml the YAML specification
         */
        public CountingAsyncPythonProcessService(YamlService yaml) {
            super(yaml);
        }

        @Override
        public <I> void process(String inType, I data) throws ExecutionException {
            super.process(inType, data);
            countProcess++;
        }
        
        /**
         * Returns the number of processed items.
         * 
         * @return the number
         */
        public int getCountProcess() {
            return countProcess;
        }
        
    }
    
    /**
     * Tests an client-server communication with {@link PythonAsyncProcessService}.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testAsyncClientServer() throws IOException, ExecutionException {
        NotificationMode mo = ActiveAasBase.setNotificationMode(NotificationMode.NONE); // no AAS here
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        Server qpid = new TestQpidServer(broker);
        qpid.start();
        EnvironmentSetup setup = EnvironmentSetup.readFromYaml(EnvironmentSetup.class, 
            new FileInputStream("src/test/resources/envSetup.yml"));
        setup.getTransport().setPort(broker.getPort());
        Supplier<TransportSetup> su = Transport.setTransportSetup(() -> setup.getTransport());

        final String transportChannel = "myServer";
        YamlServer yServer = new YamlServer();
        yServer.setId("test-py"); // as in .py
        yServer.setHost("localhost");
        yServer.setPort(NetUtils.getEphemeralPort());
        yServer.setDescription("");
        yServer.setVersion(new Version(0, 0, 1));
        yServer.setStarted(false);
        yServer.setTransportChannel(transportChannel);
        yServer.setCmdArg(composeCmdLineArguments("test-py"));
        yServer.setHomePath("src/main/python");
        CountingAsyncPythonProcessService server = new CountingAsyncPythonProcessService(yServer.toService());
        server.setState(ServiceState.STARTING);
        
        YamlService yService = new YamlService();
        yService.setDescription("");
        yService.setId("1234"); // as in .py
        yService.setTransportChannel(transportChannel);
        yService.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        yService.setTopLevel(true);
        yService.setVersion(new Version(0, 0, 2));
        YamlProcess yProcess = new YamlProcess();
        yProcess.setHomePath("src/main/python");
        yProcess.setCmdArg(composeCmdLineArguments("1234"));
        yProcess.setStarted(false);
        yService.setProcess(yProcess);
        CountingAsyncPythonProcessService client = new CountingAsyncPythonProcessService(yService);
        client.setState(ServiceState.STARTING);

        TimeUtils.sleep(1000);

        Assert.assertEquals(ServiceState.RUNNING, client.getState());
        Assert.assertEquals(ServiceState.RUNNING, server.getState());

        TimeUtils.sleep(1000);
        server.process("*SERVER", "S-TEST".getBytes()); // -> server
        TimeUtils.sleep(1000);
        client.process("*SERVER", "C-TEST".getBytes()); // -> client -> server
        TimeUtils.sleep(1000);
        
        Assert.assertEquals(2, server.getCountProcess()); // the two above
        Assert.assertEquals(1, client.getCountProcess()); // the client pings back on the second

        TimeUtils.sleep(1000);

        client.setState(ServiceState.STOPPING);
        server.setState(ServiceState.STOPPING);

        TimeUtils.sleep(2000);

        Transport.releaseConnector(false); // don't stay off, allow reconnect
        qpid.stop(true);
        ActiveAasBase.setNotificationMode(mo);
        Transport.setTransportSetup(su);
    }
    
    /**
     * Tests the process-based service classes.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
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
        pDesc.setHomePath("src/main/python");
        pDesc.setCmdArg(composeCmdLineArguments("1234"));
        sDesc.setProcess(pDesc);
        
        final String stringTypeName = "S"; // same symbolic type name for in/output
        final String rec13TypeName = "Rec13";
        AbstractPythonProcessService service = new PythonSyncProcessService(sDesc);
        service.registerInputTypeTranslator(String.class, stringTypeName, TypeTranslators.STRING);
        service.registerOutputTypeTranslator(String.class, stringTypeName, TypeTranslators.STRING);
        service.registerInputTypeTranslator(Rec13.class, rec13TypeName, new Rec13InTranslator());
        service.registerOutputTypeTranslator(Rec13.class, rec13TypeName, new Rec13OutTranslator());
        
        service.setState(ServiceState.STARTING);
        Assert.assertEquals("test", service.processSync(stringTypeName, "test", stringTypeName));
        Assert.assertEquals("test", service.processSync(stringTypeName, "test", stringTypeName));
        Assert.assertEquals("test", service.processSyncQuiet(stringTypeName, "test", stringTypeName));
        Rec13 r = new Rec13Impl();
        r.setIntField(10);
        r.setStringField("abba");
        Assert.assertEquals(r, service.processSyncQuiet(rec13TypeName, r, rec13TypeName));
        service.setState(ServiceState.STOPPING);

        service.activate();
        service.passivate();
    }

    
}
