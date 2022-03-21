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

package test.de.iip_ecosphere.platform.security.services.kodex;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.security.services.kodex.KodexRestService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * Tests the KODEX local server. The utilized REST framework is just for testing, no production use!
 * 
 * @author Marcel Nöhre
 */
public class KodexRestServiceTest {
    
    /**
     * Processes {@code data} on {@code service} and logs the sent input.
     * 
     * @param service the service instance
     * @param data the input data
     * @throws IOException if processing/serializing the input data fails
     */
    private static void process(KodexRestService<InData, OutData> service, InData data) throws IOException {
        LoggerFactory.getLogger(KodexRestServiceTest.class).info("Input: {"
            + "id=\"" + data.getId() + "\" name=\"" + data.getName() + "\"}");
        service.process(data);
    }
    
    /**
     * Tests the KODEX local server.
     * 
     * @throws IOException if reading test data fails, shall not occur
     * @throws ExecutionException shall not occur 
     */
    @Test
    public void testKodexRestService() throws IOException, ExecutionException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        ReceptionCallback<OutData> rcp = new ReceptionCallback<OutData>() {

            @Override
            public void received(OutData data) {
                Assert.assertTrue(data.getId() != null && data.getId().length() > 0);
                Assert.assertTrue(data.getName() != null && data.getName().length() > 0);
                Assert.assertTrue(data.getKip() != null && data.getKip().length() > 0);
                receivedCount.incrementAndGet();
                LoggerFactory.getLogger(KodexRestServiceTest.class).info("Received result: {kip=\"" + data.getKip() 
                    + "\" id=\"" + data.getId() + "\" name=\"" + data.getName() + "\"}");
            }

            @Override
            public Class<OutData> getType() {
                return OutData.class;
            }
        };

        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("KodexRestTest");
        sDesc.setVersion(new Version(KodexRestService.VERSION));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("KodexRestTest");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        pDesc.setExecutablePath(new File("./src/main/resources/"));
        pDesc.setHomePath(new File("./src/test/resources"));
        sDesc.setProcess(pDesc);
        
        // just that the constructor is called, throw away
        new KodexRestService<>(new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc);
        // test implementation
        KodexRestService<InData, OutData> service = new KodexRestService<>(
            new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc, "example-data.yml");
        service.setState(ServiceState.STARTING);
        service.setState(ServiceState.RUNNING);
        process(service, new InData("test", "test"));
        process(service, new InData("test", "test"));
        process(service, new InData("test", "test"));
        TimeUtils.sleep(500);
        LoggerFactory.getLogger(KodexRestServiceTest.class).info("Stopping service, may take two minutes on Windows");
        service.setState(ServiceState.STOPPING);     
        Assert.assertEquals(3, receivedCount.get());
        LoggerFactory.getLogger(KodexRestServiceTest.class).info("Activating/Passivating");
        service.activate();
        LoggerFactory.getLogger(KodexRestServiceTest.class).info(
            "Passivating service, may take two minutes on Windows");
        service.passivate();
    }
}