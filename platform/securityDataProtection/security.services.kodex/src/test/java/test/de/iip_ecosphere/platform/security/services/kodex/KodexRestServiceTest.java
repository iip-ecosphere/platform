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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

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
    
    private static HttpURLConnection connection;
    
    /**
     * In-data JSON import.
     * 
     * @param file the file to read
     * @return the input
     */
    private static Object inDataJsonImport(String file) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            Object obj = jsonParser.parse(reader);
            return obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get Connection to local server.
     * 
     * @throws IOException in case of I/O related problems
     */
    private static void getConnection() throws IOException {
        URL url = new URL("http://localhost:8000/v1/configs/abcdef/transform");
        connection = (HttpURLConnection) url.openConnection();
    }
    
    /**
     * Post request to transform data.
     * 
     * @param file the file to read
     * @return the received information
     * 
     * @throws IOException in case of I/O related problems
     */
    private static String postTransform(String file) throws IOException {   
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer aabbccdd");
        connection.connect();
        
        OutputStream os = connection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        System.out.println("INPUT: " + inDataJsonImport(file).toString());
        osw.write(inDataJsonImport(file).toString());
        osw.flush();
        osw.close();
        os.close();
        
        System.out.print("SERVER: " + connection.getResponseCode() + " ");
        System.out.println(connection.getResponseMessage());
        
        String result;
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int read = bis.read();
        while (read != -1) {
            buf.write((byte) read);
            read = bis.read();
        }
        result = buf.toString();
        bis.close();
        buf.close();
        return result;
    }
    
    /**
     * Tests the KODEX local server.
     * 
     * @throws IOException if reading test data fails, shall not occur
     * @throws ExecutionException shall not occur 
     */
    @Ignore("Currently 400 bad request")
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
        
        KodexRestService<InData, OutData> service = new KodexRestService<>(
            new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc);
        service.setState(ServiceState.STARTING);
        TimeUtils.sleep(500); // would be nice to wait here for STARTED and to set STARTED when Kodex is really running
        service.process(new InData("test", "test"));
        service.process(new InData("test", "test"));
        service.process(new InData("test", "test"));
        try {
            getConnection();
            String result = postTransform("src/test/resources/data.json");
            System.out.println("RESULT 1: " + result + "\n");
            getConnection();
            String result2 = postTransform("src/test/resources/data.json");
            System.out.println("RESULT 2: " + result2);
        } catch (IOException io) {
            io.printStackTrace();
        }
        service.setState(ServiceState.STOPPING);     
        Assert.assertEquals(3, receivedCount.get());
        service.activate();
        service.passivate();
    }
    
}