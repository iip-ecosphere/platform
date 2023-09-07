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

package de.iip_ecosphere.platform.libs.ads;

import java.io.File;

import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import de.iip_ecosphere.platform.support.OsUtils;

/**
 * Provides access to the {@link TcAds} library.
 * 
 * @author Alexander Weber, SSE
 * @author Holger Eichelberger, SSE
 */
public class Ads {

    private static TcAds instance;
    
    /**
     * Returns the instance of the {@link TcAds} library.
     * 
     * @return the instance or <b>null</b> if it cannot be loaded
     */
    public static TcAds getInstance() {
        return instance;
    }
    
    static {
        String adsPath = OsUtils.getPropertyOrEnv("iip.libs.ads");
        // possible, check default locations
        if (null == adsPath) {
            adsPath = "./src/main/resources"; // fallback for now
        }
        try {
            //System.setProperty("jna.debug_load", "true");
            String name = "TcAds";
            if (Platform.isWindows()) {
                name = "TcAdsDll";
                if (Platform.is64Bit()) {
                    adsPath += "/win32-x86-64";
                } else {
                    adsPath += "/win32-x86-32";
                }
            } else if (Platform.isLinux()) {
                name = "ads";
                adsPath += "/linux-x86-64"; // so far no 32 bit so
            }
            File f = new File(adsPath);
            System.out.println("Loading ADS library " + name + " from " + f.getAbsolutePath());
            System.setProperty("jna.library.path", f.getAbsolutePath());
            instance = Native.load(name, TcAds.class);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Cannot load ADS library");
            instance = null;
        }
    }
    
}
