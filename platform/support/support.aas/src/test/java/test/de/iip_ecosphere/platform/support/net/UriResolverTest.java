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

package test.de.iip_ecosphere.platform.support.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.net.UriResolver;

/**
 * Tests {@link UriResolver}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class UriResolverTest {

    private static final String TEST_TEXT = "TEST!";
    
    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange ex) throws IOException {
            String reqUri = ex.getRequestURI().toString();
            if (reqUri.endsWith("/resolutionTest.txt")) {
                byte [] response = TEST_TEXT.getBytes();
                ex.sendResponseHeaders(200, response.length);
                OutputStream os = ex.getResponseBody();
                os.write(response);
                os.close();
            } else {
                ex.sendResponseHeaders(404, 0);
            }
        }
    }
    
    /**
     * Tests {@link UriResolver}.
     * 
     * @throws IOException shall not occur if successful
     * @throws URISyntaxException shall not occur if successful
     */
    @Test
    public void testUriResolution() throws IOException, URISyntaxException {
        File f = new File("target/resolutionTest.txt");
        FileUtils.copyFile(new File("src/test/resolutionTest.txt"), f);
        File resolved = UriResolver.resolveToFile(f.toURI(), null);
        Assert.assertNotNull(resolved);
        Assert.assertTrue(resolved.exists());
        Assert.assertTrue(resolved.isFile());
        Assert.assertEquals(f.length(), resolved.length());
        FileUtils.deleteQuietly(resolved);

        ServerAddress addr = new ServerAddress(Schema.HTTP); // localhost, ephemeral
        HttpServer server = HttpServer.create(new InetSocketAddress(addr.getPort()), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        URI uri = new URI("http://localhost:" + addr.getPort() + "/test/resolutionTest1.txt");
        try {
            resolved = UriResolver.resolveToFile(uri, null);
            Assert.fail("no exception");
        } catch (IOException e) {
            // this is ok
        }

        File dir = Files.createTempDirectory("iip").toFile();
        uri = new URI("http://localhost:" + addr.getPort() + "/test/resolutionTest.txt");
        resolved = UriResolver.resolveToFile(uri, dir);
        Assert.assertNotNull(resolved);
        Assert.assertTrue(resolved.exists());
        Assert.assertTrue(resolved.isFile());
        Assert.assertEquals(TEST_TEXT.length(), resolved.length());
        FileUtils.deleteQuietly(dir);

        resolved = UriResolver.resolveToFile(uri, null);
        Assert.assertNotNull(resolved);
        Assert.assertTrue(resolved.exists());
        Assert.assertTrue(resolved.isFile());
        Assert.assertEquals(TEST_TEXT.length(), resolved.length());
        FileUtils.deleteQuietly(resolved);
        
        server.stop(0);
    }

}
