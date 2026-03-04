/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package iip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import de.iip_ecosphere.platform.support.FileUtils;

/**
 * Simple Starter for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    // checkstyle: stop exception type check

    /**
     * Runs the starter.
     * 
     * @param args first argument may be the name of the test file to be written, else "AppStarter.test" in temp 
     *     is assumed
     */
    public static void main(String[] args) {
        File tmpFile;
        if (args.length > 0) {
            tmpFile = new File(args[0]);
        } else {
            tmpFile = new File(FileUtils.getTempDirectory(), "AppStarter.test");
        }
        tmpFile.delete();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            System.out.println("Context loader:  " + Thread.currentThread().getContextClassLoader());            
            System.out.println("Own classloader: " + Starter.class.getClassLoader());            
            try { // some tests also involving parent classloader delegation
                System.out.println(loader.loadClass("org.springframework.core.Constants"));
                System.out.println(loader.getResource("META-INF/spring.factories"));
                System.out.println(loader.getResource("de/iip_ecosphere/platform/services/environment/spring/"
                    + "metricsProvider/MetricsProviderRestService.class"));
                System.out.println(loader.loadClass("de.iip_ecosphere.platform.transport.connectors."
                    + "TransportParameter"));
            } catch (Throwable t) {
                t.printStackTrace(System.out);
            }
            System.out.println("Writing to file  " + tmpFile);
            Files.writeString(tmpFile.toPath(), "iip.Starter", StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    // checkstyle: resume exception type check

}
