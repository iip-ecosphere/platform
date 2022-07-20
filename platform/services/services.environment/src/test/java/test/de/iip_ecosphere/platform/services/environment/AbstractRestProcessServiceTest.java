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

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.AbstractRestProcessService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import spark.Route;
import spark.Spark;

/**
 * Tests {@link AbstractRestProcessService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractRestProcessServiceTest {

    private static final String PATH = "/services/repeater";
    private static final String TEST_REST = "{\"field\":\"abc\"}";
    private int port;

    /**
     * Simple service.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyRestProcessService extends AbstractRestProcessService<String, String> {

        /**
         * Creates an instance of the service with the required type translators.
         * 
         * @param inTrans the input translator
         * @param outTrans the output translator
         * @param callback called when data from the service is available
         * @param yaml the service description 
         */
        protected MyRestProcessService(TypeTranslator<String, String> inTrans, TypeTranslator<String, String> outTrans,
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
        protected String getApiPath() {
            return "http://localhost:" + port + PATH;
        }

        @Override
        protected String getBearerToken() {
            return null;
        }

        @Override
        protected String adjustRestQuery(String input) {
            return input;
        }

        @Override
        protected String adjustRestResponse(String response) {
            return response;
        }
        
        @Override
        protected ServiceState start() throws ExecutionException {
            setupConnectionManager();
            return ServiceState.RUNNING;
        }

        
    }
    
    /**
     * Simple identity translator. We do not care for the data here.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyTypeTranslator implements TypeTranslator<String, String> {

        @Override
        public String from(String data) throws IOException {
            return data;
        }

        @Override
        public String to(String data) throws IOException {
            return data;
        }
        
    }
    
    /**
     * Replacement for Java 11 {@code String.lines()}.
     * 
     * @param string the string to stream
     * @return the streamed string in terms of individual lines
     */
    public static Stream<String> lines(String string) {
        return Stream.of(string.replace("\r\n", "\n").split("\n"));
    }
    
    /**
     * Tests {@link AbstractRestProcessService}.
     * 
     * @throws ExecutionException shall not occur if successful
     */
    @Test
    public void testRestProcess() throws ExecutionException {
        Route route = (req, res) -> { 
            String request = lines(req.body()).collect(Collectors.joining("\n"));
            res.body(request);
            res.status(200);
            return res.body();
        };

        port = NetUtils.getEphemeralPort();
        Spark.port(port);
        post(PATH, route);
        get(PATH, route);
        put(PATH, route);
        delete(PATH, route);
        Spark.awaitInitialization();

        AtomicInteger rcv = new AtomicInteger();
        ReceptionCallback<String> callback = new ReceptionCallback<String>() {

            @Override
            public void received(String data) {
                Assert.assertEquals(TEST_REST, data);
                rcv.incrementAndGet();
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        };
        
        YamlService yaml = new YamlService();
        yaml.setDeployable(true);
        yaml.setTopLevel(true);
        yaml.setName("testRest");
        yaml.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        yaml.setDescription("");
        yaml.setVersion(new Version("0.0.1"));
        yaml.setId("testRest");
        
        MyRestProcessService service = new MyRestProcessService(
            new MyTypeTranslator(), new MyTypeTranslator(), callback, yaml);
        service.setState(ServiceState.STARTING);
        service.processQuiet(TEST_REST);
        TimeUtils.sleep(1000);
        Assert.assertTrue(rcv.get() > 0);
        service.setState(ServiceState.STOPPING);
        Spark.stop();
        Spark.awaitStop();
    }
    
}
