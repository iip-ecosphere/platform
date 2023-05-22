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
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Some network utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetUtils {
    
    public static final String NO_MASK = "";
    private static String ip = null;
    
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
     * @param netMask the net mask, regular expression; if {@link #NO_MASK}, empty or <b>null</b>, 
     *     return {@link #getOwnIP()}
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
            } catch (SocketException | PatternSyntaxException e) {
                // ignore, care for your input
            }
        }
        return result;
    }
    
    /**
     * Returns whether {@code host} is one of the addresses of this computer.
     * 
     * @param host the IP address/host name to look for
     * @return {@code true} if {@code host} is one of the own addresses, {@code false} else
     */
    public static boolean isOwnAddress(String host) {
        boolean isOwn = false;
        try {
            InetAddress givenHost = InetAddress.getByName(host);
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (!isOwn && ifs.hasMoreElements()) {
                NetworkInterface ni = ifs.nextElement();
                for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
                    if (addr.getAddress().equals(givenHost)) {
                        isOwn = true;
                        break;
                    }
                }
            }
        } catch (SocketException | UnknownHostException e) {
            // ignore, care for your input
        }
        return isOwn;
    }
    
    /**
     * Returns the preferred own network address.
     * 
     * @return the preferred own network address
     */
    public static String getOwnIP() {
        if (null == ip) {
            //https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
                if ("0.0.0.0".equals(ip)) { // strange, happened in a docker container in a VM
                    ip = findFallbackIP();
                }
            } catch (UnknownHostException | SocketException | UncheckedIOException e) {
                ip = "127.0.0.1";
            }
        }
        return ip;
    }

    /**
     * Finds a fallback IP address. Must have an IPv4/IPv6 address (docker addresses so far have only 
     * 
     * @return a fallback IP address, may be {@code 127.0.0.1} if none was found
     * @see #isContainerIp(String)
     */
    private static String findFallbackIP() {
        String result = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                String ipV4 = null;
                String ipV6 = null;
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getAddress().length == 4) { // heuristic: docker container only had one
                            ipV4 = inetAddress.getHostAddress();
                        } else {
                            ipV6 = inetAddress.getHostAddress();
                        }
                    }
                }
                if (ipV4 != null && ipV6 != null && !isContainerIp(ipV4)) { 
                    result = ipV4;
                }
            }
        } catch (SocketException e) {
            // ignore
        }
        return result;
    }

    /**
     * Returns whether {@code address} is a container address. Currently, we only consider ipv4 addresses.
     * 
     * @param address the address
     * @return {@code true} for a container address, {@code false} else
     */
    public static boolean isContainerIp(String address) {
        return address.startsWith("172.17."); // is docker, may need configuration options
    }
    
    /**
     * Returns whether we are running inside a container.
     * 
     * @return {@code true} for container, {@code false} else
     * @see #isContainerIp(String)
     */
    public static boolean isInContainer() {
        boolean inContainer = isRunningInsideDocker();
        if (!inContainer) {
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        if (isContainerIp(inetAddress.getHostAddress())) {
                            inContainer = true;
                            break;
                        }
                    }
                    if (inContainer) {
                        break;
                    }
                }
            } catch (SocketException e) {
                // ignore
            }
        }
        return inContainer;
    }

    /**
     * Returns whether this JVM is running inside docker.
     * 
     * @return {@code true} for docker environment, {@code false} else
     */
    private static Boolean isRunningInsideDocker() {
        //https://stackoverflow.com/questions/52580008/how-does-java-application-know-it-is-running
        // -within-a-docker-container
        
        try (Stream<String> stream =
            Files.lines(Paths.get("/proc/1/cgroup"))) {
            return stream.anyMatch(line -> line.contains("/docker"));
        } catch (IOException e) {
            return false;
        }
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
    
    /**
     * Returns whether {@code port} on {@code host} is available and some process is listening.
     * 
     * @param host the host name
     * @param port the port number
     * @return {@code true} for available, {@code false} else
     */
    public static boolean isAvailable(String host, int port) {
        boolean result = false;
        try {
            Socket sock = new Socket(host, port);          
            sock.close();
            result = true;
        } catch (IOException e) {         
            /*if (e.getMessage().contains("refused")) {
                result = false;
            }*/
        }
        return result;
    }
    
    /**
     * Returns whether the given string is an IPv4 address.
     * 
     * @param address the address to validate
     * @return {@code true} for a valid address, {@code false} else
     */
    public static boolean isIpV4Addess(String address) {
        return InetAddressValidator.getInstance().isValidInet4Address(address);
    }
    
    /**
     * Returns a netmask for {@link #getOwnIP(String)}.
     * 
     * @param netMask the netmask to use
     * @param fallbackIp the fallback IP address to be turned into a class C net (search) mask
     * @return the netmask, the modified fallback address or an empty string
     */
    public static String getNetMask(String netMask, String fallbackIp) {
        String result;
        if (isIpV4Addess(netMask)) {
            result = netMask;
        } else if (isIpV4Addess(fallbackIp)) {
            int pos = fallbackIp.lastIndexOf('.');
            result = fallbackIp.substring(0, pos + 1) + "255"; 
        } else {
            result = "";
        }
        return result;
    }

}
