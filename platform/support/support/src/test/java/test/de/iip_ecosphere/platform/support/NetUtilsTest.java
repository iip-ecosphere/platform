/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Tests {@link NetUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetUtilsTest {

    /**
     * Tests {@link NetUtils#getEphemeralPort()}.
     */
    @Test
    public void testEphemeralPort() {
        // if there is no free port, it probably makes no sense to run the tests at all
        Assert.assertTrue(NetUtils.getEphemeralPort() > 0);
    }
    
    /**
     * Tests {@link NetUtils#getOwnIP()}.
     */
    @Test
    public void testOwnIP() {
        Assert.assertTrue(NetUtils.isIpV4Addess(NetUtils.getOwnIP(null)));
        Assert.assertTrue(NetUtils.isIpV4Addess(NetUtils.getOwnIP(NetUtils.NO_MASK)));
        Assert.assertTrue(NetUtils.isIpV4Addess(NetUtils.getOwnIP("255.255.255.255")));
        Assert.assertTrue(NetUtils.isIpV4Addess(NetUtils.getOwnIP("^255.255.255.255")));
    }

    /**
     * Tests {@link NetUtils#getOwnHostname()}.
     */
    @Test
    public void testOwnHostname() {
        String host = NetUtils.getOwnHostname();
        Assert.assertNotNull(host);
        Assert.assertTrue(host.length() > 0);
    }
    
    /**
     * A simple test server runnable, accepting and closing a connection.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ServerRunnable implements Runnable {

        private int port;
        private boolean running = true;
        
        /**
         * Creates a server runnable.
         * 
         * @param port the port to listen on
         */
        private ServerRunnable(int port) {
            this.port = port;
        }
        
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server is listening on port " + port);
                while (running) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected");
                    socket.close();
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
            }
        }
        
        /**
         * Stops the server.
         */
        private void stop() {
            running = false;
        }
        
    }
    
    /**
     * Tests {@link NetUtils#isAvailable(String, int)}.
     */
    @Test
    public void testIsAvailable() {
        String host = ServerAddress.LOCALHOST;
        int port = NetUtils.getEphemeralPort();
        Assert.assertFalse(NetUtils.isAvailable(host, port));

        port = NetUtils.getEphemeralPort();
        ServerRunnable sr = new ServerRunnable(port);
        new Thread(sr).start();
        
        int trials = 0;
        while (!NetUtils.isAvailable(host, port) && trials < 10) {
            TimeUtils.sleep(500);
            trials++;
        }
        Assert.assertTrue(trials < 10); // there shall be a connection
        sr.stop();
    }
    
    /**
     * Tests {@link NetUtils#isOwnAddress(String)}.
     */
    @Test
    public void testIsOwnAddress() {
        Assert.assertTrue(NetUtils.isOwnAddress(ServerAddress.LOCALHOST));
        Assert.assertTrue(NetUtils.isOwnAddress("127.0.0.1"));
        Assert.assertFalse(NetUtils.isOwnAddress("192.168.2.255")); // broadcast
    }
    
    /**
     * Tests {@link NetUtils#isIpV4Addess(String)}.
     */
    @Test
    public void testIsIpV4Address() {
        Assert.assertTrue(NetUtils.isIpV4Addess("127.0.0.1"));
        Assert.assertFalse(NetUtils.isIpV4Addess("127.0.abc.1"));
        Assert.assertFalse(NetUtils.isIpV4Addess(""));
        Assert.assertFalse(NetUtils.isIpV4Addess(null));
    }
    
    /**
     * Tests {@link NetUtils#getNetMask(String, String)}.
     */
    @Test
    public void testGetNetMask() {
        Assert.assertEquals("255.255.255.255", NetUtils.getNetMask("255.255.255.255", ""));
        Assert.assertEquals("255.255.255.255", NetUtils.getNetMask("", "255.255.255.255"));
        Assert.assertEquals("127.0.0.255", NetUtils.getNetMask("", "127.0.0.1"));
        Assert.assertEquals("", NetUtils.getNetMask("", ""));
    }
    
    /**
     * Tests {@link NetUtils#isInContainer()}.
     */
    @Test
    public void isInContainer() {
        System.out.println("In container: " + NetUtils.isInContainer()); // we don't know where we are
    }
    
    /**
     * Tests {@link NetUtils#createURL(String)}.
     */
    @Test
    public void testCreateUrl() {
        try {
            NetUtils.createURL("");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        final String text = "https://oktoflow.de";
        URL url = NetUtils.createURL(text);
        Assert.assertEquals(text, url.toString());
    }
    
}
