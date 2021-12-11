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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;

/**
 * A MAC-based ID provider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MacIdProvider implements IdProvider {

    @Override
    public String provideId() {
        String macAddress = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            if (ni == null) { // Ubuntu :(
                // https://stackoverflow.com/questions/23900172/how-to-get-localhost-network-interface-in-java-or-scala
                Enumeration<NetworkInterface> ne = NetworkInterface.getNetworkInterfaces();
                while (ne.hasMoreElements()) {
                    ni = ne.nextElement();
                    break;
                }
            }
            if (null != ni) {
                byte[] hardwareAddress = ni.getHardwareAddress();
                String[] hexadecimal = new String[hardwareAddress.length];
                for (int i = 0; i < hardwareAddress.length; i++) {
                    hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                }
                macAddress = String.join("", hexadecimal);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(MacIdProvider.class).error("Obtaining MAC-based device ID: " + e.getMessage());
        }
        return macAddress;
    }

    @Override
    public boolean allowsConsoleOverride() {
        return true; // enabled, in particular for debugging
    }

}
