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
import java.io.UncheckedIOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
     * Returns the own IP address filtered by the given decimal netMask/regular expression.
     * 
     * @param netMask the net mask, regular expression; if empty or <b>null</b>, return {@link #getOwnIP()}
     * @return the IP
     */
    public static String getOwnIP(String netMask) {
        String result = "127.0.0.1";
        if (null == netMask || netMask.length() == 0) {
            result = getOwnIP();
        } else {
            netMask = netMask.replaceAll("255", "\\\\d{1,3}");
            netMask = netMask.replaceAll("\\.", "\\\\.");
            if (!netMask.startsWith("^")) {
                netMask = "^" + netMask;
            }
            boolean found = false;
            try {
                Pattern pat = Pattern.compile(netMask);
                Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
                while (ifs.hasMoreElements()) {
                    NetworkInterface ni = ifs.nextElement();
                    for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
                        String tmp = addr.getAddress().getHostAddress();
                        if (pat.matcher(tmp).matches()) {
                            result = tmp;
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            } catch (SocketException e) {
            } catch (PatternSyntaxException e) {
            }
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
        } catch (UnknownHostException | SocketException | UncheckedIOException e) {
            ip = "127.0.0.1";
        }
        return ip;
    }
    
    /**
     * Returns the own hostname.
     * 
     * @return the hostname, potentially with a fallback to "localhost"
     */
    public static String getOwnHostname() {
        String result; 
        try {
            result = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            result = "localhost";
        }
        return result;
    }

}
