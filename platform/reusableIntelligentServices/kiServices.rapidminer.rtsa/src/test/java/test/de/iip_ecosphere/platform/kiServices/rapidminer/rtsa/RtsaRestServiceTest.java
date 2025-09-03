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

package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.kiServices.rapidminer.rtsa.MultiRtsaRestService;
import de.iip_ecosphere.platform.kiServices.rapidminer.rtsa.RtsaRestService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.setup.InstalledDependenciesSetup;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Tests the RTSA local server. 
 * 
 * @author Holger Eichelberger
 */
public class RtsaRestServiceTest {
    
    /**
     * Customized RTSA REST service for testing the integration without RTSA.
     *
     * @param <I> the input type
     * @param <O> the output type
     * @author Holger Eichelberger, SSE
     */
    private static class FakeRtsaRestService<I, O> extends RtsaRestService<I, O> {

        /**
         * Creates an instance of the service with the required type translators to/from JSON.
         * 
         * @param inTrans the input translator
         * @param outTrans the output translator
         * @param callback called when a processed item is received from the service
         * @param yaml the service description
         */
        public FakeRtsaRestService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans,
            ReceptionCallback<O> callback, YamlService yaml) {
            super(inTrans, outTrans, callback, yaml);
        }

        @Override
        protected String getMainClass(File rtsaPath) {
            return "de.iip_ecosphere.platform.kiServices.rapidminer.rtsaFake.FakeRtsa";
        }
        
        @Override
        protected String getClasspath(File rtsaPath) {
            return getFakeClasspath(rtsaPath);
        }
        
        @Override
        protected String getBaseDir(File rtsaPath) {
            return FileUtils.getResolvedPath(new File("."), "target/fake/rtsa"); 
        }
        
    }
    
    private static class FakeMultiRtsaRestService extends MultiRtsaRestService {
        
        /**
         * Extended RTSA service for multi-type queries.
         * 
         * @author Holger Eichelberger, SSE
         */
        protected class FakeExRtsaRestService extends ExRtsaRestService {

            /**
             * Creates a fake service instance.
             * 
             * @param yaml the YAML service info
             */
            public FakeExRtsaRestService(YamlService yaml) {
                super(yaml);
            }

            @Override
            protected String getMainClass(File rtsaPath) {
                return "de.iip_ecosphere.platform.kiServices.rapidminer.rtsaFake.FakeRtsa";
            }

            @Override
            protected String getClasspath(File rtsaPath) {
                return getFakeClasspath(rtsaPath);
            }

            @Override
            protected String getBaseDir(File rtsaPath) {
                return FileUtils.getResolvedPath(new File("."), "target/fake/rtsa"); 
            }

        }

        /**
         * Creates a fake service instance.
         * 
         * @param yaml the YAML service info
         */
        public FakeMultiRtsaRestService(YamlService yaml) {
            super(yaml);
        }

        @Override
        protected ExRtsaRestService createService(YamlService yaml) {
            return new FakeExRtsaRestService(yaml);
        }

    }
    
    /**
     * Returns the fake service classpath.
     * 
     * @param rtsaPath the RTSA path
     * @return the fake service classpath
     */
    private static String getFakeClasspath(File rtsaPath) {
        String result = "";
        // generated by maven, required due to dependencies, fake RTSA.zip not available in this phase
        File f = new File("target/test-classes/classpath");
        f = new File("target/fake/rtsa/lib");
        File zip = new File("target/fake/RTSA-" + RtsaRestService.VERSION + ".zip");
        if (!f.exists() || f.lastModified() < zip.lastModified()) {
            LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Unpacking {}", zip);
            File zipT = new File("target/fake/rtsa");
            try {
                zipT.mkdirs();
                FileInputStream zipF = new FileInputStream(zip);
                ZipUtils.extractZip(zipF, zipT.toPath());
                zipF.close();
            } catch (IOException e) {
                LoggerFactory.getLogger(RtsaRestServiceTest.class).error("Cannot unpack {}: {}", zip, e.getMessage());
            }
            File depl = new File("target/fake/myRtsaexample-0.1.0.zip");
            try {
                FileUtils.copyFile(depl, new File(new File(zipT, "deployments"), depl.getName()));
            } catch (IOException e) {
                LoggerFactory.getLogger(RtsaRestServiceTest.class).error("Cannot copy {}: {}", depl, e.getMessage());
            }
        }
        File[] jars = f.listFiles();
        if (f.exists() && null != jars) {
            for (File j : jars) {
                result += File.pathSeparator + j.getAbsolutePath();
            }
        } else {
            LoggerFactory.getLogger(RtsaRestServiceTest.class).error("Cannot complete test classpath");
        }                    
        return result;
    }

    
    /**
     * Initialization of environment, search for Java 8 required for RTSA.
     */
    @BeforeClass
    public static void startup() {
        if (!SystemUtils.IS_JAVA_1_8) {
            String prop = System.getProperty("iip.test.java8", null);
            if (prop != null) {
                File java8 = new File(prop);
                Assert.assertTrue("Java8 binary " + prop + " does not exist", java8.exists());
                Assert.assertTrue("Java8 binary " + prop + " is not a file", java8.isFile());
                InstalledDependenciesSetup.getInstance().setLocation(InstalledDependenciesSetup.KEY_JAVA_8, java8);
            } else {
                Assert.fail("JAVA8_HOME not specified.");
            }
        }
    }
    
    /**
     * Processes {@code data} on {@code service} and logs the sent input.
     * 
     * @param service the service instance
     * @param data the input data
     * @throws IOException if processing/serializing the input data fails
     */
    private static void process(RtsaRestService<InData, OutData> service, InData data) throws IOException {
        LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Input: {"
            + "id=" + data.getId() + " value1=" + data.getValue1() + " value2 = " + data.getValue2() + "}");
        service.process(data);
    }

    /**
     * Processes {@code data} on {@code service} and logs the sent input.
     * 
     * @param service the service instance
     * @param data the input data
     * @throws IOException if processing/serializing the input data fails
     */
    private static void process(MultiRtsaRestService service, InData data) throws IOException {
        LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Input: {"
            + "id=" + data.getId() + " value1=" + data.getValue1() + " value2 = " + data.getValue2() + "}");
        try {
            service.process("data", data);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    /**
     * Forces to use the fake RTSA if desired.
     * 
     * @return {@code true} for forcing fake RTSA, {@code false} for real RTSA if available (default)
     */
    private boolean forceFake() {
        return Boolean.valueOf(System.getProperty("iip.rtsa.forceFake", "false")); // just for debugging
    }
    
    /**
     * Tests the RTSA service.
     * 
     * @throws IOException if reading test data fails, shall not occur
     * @throws ExecutionException shall not occur 
     */
    @Test(timeout = 60000)
    public void testRtsaRestService() throws IOException, ExecutionException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        ReceptionCallback<OutData> rcp = new ReceptionCallback<OutData>() {

            @Override
            public void received(OutData data) {
                // don't care for the values as long we received something
                receivedCount.incrementAndGet();
                LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Received result: id=" + data.getId() 
                    + " value1=" + data.getValue1() + " value2=" + data.getValue2() + " confidence = " 
                    + data.getConfidence() + " prediction=" + data.isPrediction());
            }

            @Override
            public Class<OutData> getType() {
                return OutData.class;
            }
        };

        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("RtsaRestTest");
        sDesc.setVersion(new Version(RtsaRestService.VERSION));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("RtsaRestTest");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        sDesc.setProcess(pDesc);
        
        RtsaRestService<InData, OutData> service;
        File rtsa = new File("src/main/resources/rtsa");
        if (rtsa.exists() && !forceFake()) {
            service = new RtsaRestService<>(
                new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc);
        } else {
            service = new FakeRtsaRestService<>(
                new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc);
        }
        service.setState(ServiceState.STARTING);
        while (service.getState() != ServiceState.RUNNING) {
            TimeUtils.sleep(500);
        }
        process(service, new InData(1, 1.3, 3));
        process(service, new InData(1, 1.5, 2));
        process(service, new InData(1, 1.7, 4));

        int count = 0;
        while (receivedCount.get() == 0 && count < 20) { // wait max. for 20*200 ms
            TimeUtils.sleep(200);
            count++;
        }
        
        TimeUtils.sleep(500);
        LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Stopping service");
        service.setState(ServiceState.STOPPING);
        while (service.getState() != ServiceState.STOPPED) {
            TimeUtils.sleep(500);
        }
        TimeUtils.sleep(500);
        Assert.assertTrue(receivedCount.get() >= 1); //relaxed
    }
    
    /**
     * Tests the multi-type RTSA service.
     * 
     * @throws IOException if reading test data fails, shall not occur
     * @throws ExecutionException shall not occur 
     */
    @Test(timeout = 60000)
    public void testMultiRtsaRestService() throws IOException, ExecutionException {
        AtomicInteger receivedCount = new AtomicInteger(0);

        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("RtsaRestTest");
        sDesc.setVersion(new Version(RtsaRestService.VERSION));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("RtsaRestTest");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        sDesc.setProcess(pDesc);
        
        MultiRtsaRestService service;
        File rtsa = new File("src/main/resources/rtsa");
        if (rtsa.exists() && !forceFake()) {
            service = new MultiRtsaRestService(sDesc);
        } else {
            service = new FakeMultiRtsaRestService(sDesc);
        }
        service.registerInputTypeTranslator(InData.class, "data", new InDataJsonTypeTranslator());
        service.registerOutputTypeTranslator(OutData.class, "data", new OutDataJsonTypeTranslator());
        service.attachIngestor(OutData.class, "data", data -> {
            
            receivedCount.incrementAndGet();
            LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Received result: id=" + data.getId() 
                + " value1=" + data.getValue1() + " value2=" + data.getValue2() + " confidence = " 
                + data.getConfidence() + " prediction=" + data.isPrediction());
            
        });
        service.setState(ServiceState.STARTING);
        while (service.getState() != ServiceState.RUNNING) {
            TimeUtils.sleep(500);
        }
        process(service, new InData(1, 1.3, 3));
        process(service, new InData(1, 1.5, 2));
        process(service, new InData(1, 1.7, 4));

        int count = 0;
        while (receivedCount.get() == 0 && count < 20) { // wait max. for 20*200 ms
            TimeUtils.sleep(200);
            count++;
        }
        
        TimeUtils.sleep(500);
        LoggerFactory.getLogger(RtsaRestServiceTest.class).info("Stopping service");
        service.setState(ServiceState.STOPPING);
        while (service.getState() != ServiceState.STOPPED) {
            TimeUtils.sleep(500);
        }
        TimeUtils.sleep(500);
        Assert.assertTrue(receivedCount.get() >= 1); //relaxed
    }

}