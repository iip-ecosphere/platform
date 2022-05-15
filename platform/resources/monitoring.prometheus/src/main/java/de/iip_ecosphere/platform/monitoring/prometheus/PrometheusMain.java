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

package de.iip_ecosphere.platform.monitoring.prometheus;

/**
 * Simple starter class based on a default setup. Workaround until Tomcat clashes with BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrometheusMain {
    
    /**
     * Simple fixed starter class.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        PrometheusLifecycleDescriptor desc = new PrometheusLifecycleDescriptor();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> desc.shutdown()));
        desc.startup(new String[] {});
        System.out.println("Started IIP-Ecopshere monitoring ... Use Control-C to stop.");
    }

}
