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

package test.de.iip_ecosphere.platform.security.services.kodex;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.security.services.kodex.KodexService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import test.de.iip_ecosphere.platform.transport.JsonUtils;

/**
 * Tests the KODEX service. The utilized JSON framework is just for testing, no production use!
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexServiceTest {

    /**
     * Represents the input data.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class InData {
        
        private String name;
        private String id;
        
        /**
         * Creates an instance.
         * 
         * @param name the name value
         * @param id the id value
         */
        InData(String name, String id) {
            this.name = name;
            this.id = id;
        }
        
        /**
         * Returns the name value.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the id value.
         * 
         * @return the id
         */
        public String getId() {
            return id;
        }
        
    }

    /**
     * Represents the output data.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class OutData {

        private String kip;
        private String name;
        private String id;
        
        /**
         * Creates an instance.
         *
         * @param kip the kip value introduced by KODEX
         * @param name the name value
         * @param id the id value
         */
        OutData(String kip, String name, String id) {
            this.kip = kip;
            this.name = name;
            this.id = id;
        }

        /**
         * Returns the kip value introduced by KODEX.
         * 
         * @return the kip value
         */
        public String getKip() {
            return kip;
        }

        /**
         * Returns the name value.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the id value.
         * 
         * @return the id
         */
        public String getId() {
            return id;
        }

    }
    
    private static class InDataJsonTypeTranslator implements TypeTranslator<InData, String> {

        @Override
        public InData from(String data) throws IOException {
            return null; // shall be filled, not needed here
        }

        @SuppressWarnings("unchecked")
        @Override
        public String to(InData source) throws IOException {
            JSONObject json = new JSONObject();
            json.put("name", source.getName());
            json.put("id", source.getId());
            return json.toJSONString();
        }
        
    }
    
    private static class OutDataJsonTypeTranslator implements TypeTranslator<String, OutData> {

        @Override
        public String from(OutData data) throws IOException {
            return null; // shall be filled, not needed here
        }

        @Override
        public OutData to(String source) throws IOException {
            OutData result;
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(source);
                result = new OutData(JsonUtils.readString(obj, "_kip"), 
                    JsonUtils.readString(obj, "name"), 
                    JsonUtils.readString(obj, "id"));
            } catch (ParseException e) {
                throw new IOException(e.getMessage(), e);
            } catch (ClassCastException e) {
                throw new IOException(e.getMessage(), e);
            }
            return result;
        }
        
    }
    
    /**
     * Tests the KODEX service.
     * 
     * @throws ExecutionException in case of service execution failures
     * @throws IOException in case of I/O related problems
     */
    @Test
    public void testKodexService() throws IOException, ExecutionException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        ReceptionCallback<OutData> rcp = new ReceptionCallback<OutData>() {

            @Override
            public void received(OutData data) {
                Assert.assertTrue(data.getId() != null && data.getId().length() > 0);
                Assert.assertTrue(data.getName() != null && data.getName().length() > 0);
                Assert.assertTrue(data.getKip() != null && data.getKip().length() > 0);
                receivedCount.incrementAndGet();
            }

            @Override
            public Class<OutData> getType() {
                return OutData.class;
            }
        };
        
        YamlService sDesc = new YamlService();
        sDesc.setName("KodexTest");
        sDesc.setVersion(new Version(KodexService.VERSION));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("KodexTest");
        sDesc.setDeployable(true);
        
        KodexService<InData, OutData> service = new KodexService<>(
            new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc);
        service.setState(ServiceState.STARTING);
        service.process(new InData("test", "test"));
        service.process(new InData("test", "test"));
        service.process(new InData("test", "test"));
        service.setState(ServiceState.STOPPING);
        Assert.assertEquals(3, receivedCount.get()); // 3 in, 3 out

        service.activate();
        service.passivate();
    }
    
}
