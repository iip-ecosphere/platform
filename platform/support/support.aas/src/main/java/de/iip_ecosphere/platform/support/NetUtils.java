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

package de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Some network utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetUtils {
    
    /**
     * Returns a free ephemeral port. Such a port may be used for testing, e.g. to avoid clashes between 
     * multiple subsequent tests using the same static port.
     * 
     * @return the port number
     */
    public static int getEphemeralPort() {
        int result = 0;
        try {
            ServerSocket s = new ServerSocket(0);
            result = s.getLocalPort();
            s.close(); 
        } catch (IOException e) {
        }
        return result;
    }
    
    /**
     * Returns the preferred own network address.
     * 
     * @return the preferred own network address
     */
    public static String getOwnIP() {
        //https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        String ip = "";
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            ip = "127.0.0.1";
        }
        return ip;
    }

}
