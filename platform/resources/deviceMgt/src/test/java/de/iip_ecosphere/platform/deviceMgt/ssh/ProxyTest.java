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

package de.iip_ecosphere.platform.deviceMgt.ssh;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

import static de.iip_ecosphere.platform.deviceMgt.ssh.SocketUtils.mockSocket;
import static org.mockito.Mockito.*;

/**
 * Tests the SSH proxy.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class ProxyTest {

    public static final String A_MESSAGE = "test";

    /**
     * Tests that input data goes to output data.
     * 
     * @throws IOException IO problems shall not occur
     * @throws InterruptedException timeouts shall not occur
     */
    @Test
    public void proxy_withDateOnInputStream_proxiesDataToOutputStream() throws IOException, InterruptedException {
        // Create simple mock Sockets
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        // Create mock OutputStreams which collects every byte into an byte array and link it to socket
        SocketUtils.MockOutputStream outputStream = new SocketUtils.MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        // Link MockInputStream to socket and send a message
        when(inSocket.getInputStream()).thenReturn(new SocketUtils.MockInputStream(A_MESSAGE));

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertEquals(A_MESSAGE, outputStream.getAsString());
    }

    /**
     * Tests that a proxy without input stream does not proxy.
     * 
     * @throws IOException IO problems shall not occur
     */
    @Test
    public void proxy_withoutInputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        SocketUtils.MockOutputStream outputStream = new SocketUtils.MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        when(inSocket.getInputStream()).thenReturn(null);

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertNull(outputStream.getAsString());
    }

    /**
     * Tests that a proxy without output stream does not proxy.
     * 
     * @throws IOException IO problems shall not occur
     */
    @Test
    public void proxy_withoutOutputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        when(outSocket.getOutputStream()).thenReturn(null);

        InputStream mockInputStream = mock(InputStream.class);
        when(inSocket.getInputStream()).thenReturn(mockInputStream);

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        // Verify that the InputStream is never read by Proxy
        verify(mockInputStream, times(0)).read();
    }

    /**
     * Tests that a proxy with errors on the input stream does not proxy.
     * 
     * @throws IOException IO problems shall not occur
     */
    @Test
    public void proxy_withThrowingInputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        SocketUtils.MockOutputStream outputStream = new SocketUtils.MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        when(inSocket.getInputStream()).thenThrow(new IOException());

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertNull(outputStream.getAsString());
    }

    /**
     * Tests that a proxy with errors on the output stream does not proxy.
     * 
     * @throws IOException IO problems shall not occur
     */
    @Test
    public void proxy_withThrowingOutputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        InputStream mockInputStream = mock(InputStream.class);
        when(inSocket.getInputStream()).thenReturn(mockInputStream);

        when(inSocket.getOutputStream()).thenThrow(new IOException());

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        verify(mockInputStream, times(0)).read();
    }


}