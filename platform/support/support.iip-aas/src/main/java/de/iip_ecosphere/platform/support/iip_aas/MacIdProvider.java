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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * A MAC-based ID provider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MacIdProvider implements IdProvider {

    @Override
    public String provideId() {
        List<String> macAddresses = new ArrayList<String>();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            if (ni == null) { // Ubuntu :(
                // https://stackoverflow.com/questions/23900172/how-to-get-localhost-network-interface-in-java-or-scala
                Enumeration<NetworkInterface> ne = NetworkInterface.getNetworkInterfaces();
                while (ne.hasMoreElements()) {
                    ni = ne.nextElement();                    
                    byte[] hardwareAddress = ni.getHardwareAddress();
                    String[] hexadecimal = new String[hardwareAddress.length];
                    for (int i = 0; i < hardwareAddress.length; i++) {
                        hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                    }
                    macAddresses.add(String.join("", hexadecimal));
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(MacIdProvider.class).error("Obtaining MAC-based device ID: " + e.getMessage());
        }
        String macAddress = null;
        if (macAddresses.size() > 0) {
            // network interfaces seems to change on Ubuntu/Docker after restart; sorting for a bit more dynamism
            // selection criteria such as netmask could improve that
            Collections.sort(macAddresses); 
            macAddress = macAddresses.get(0);
        }
        return macAddress;
    }

    @Override
    public boolean allowsConsoleOverride() {
        return true; // enabled, in particular for debugging
    }

}
