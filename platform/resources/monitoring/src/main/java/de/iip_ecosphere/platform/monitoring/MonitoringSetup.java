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

package de.iip_ecosphere.platform.monitoring;

import java.io.IOException;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Basic setup for monitoring.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoringSetup extends AbstractSetup {

    private static MonitoringSetup instance;

    private AasSetup aas = new AasSetup();
    private TransportSetup transport = new TransportSetup();

    /**
     * Returns the AAS setup.
     * 
     * @return the AAS setup
     */
    public AasSetup getAas() {
        return aas;
    }

    /**
     * Returns the transport setup.
     * 
     * @return the transport setup
     */
    public TransportSetup getTransport() {
        return transport;
    }

    /**
     * Defines the AAS setup. [snakeyaml]
     * 
     * @param aas the AAS setup
     */
    public void setAas(AasSetup aas) {
        this.aas = aas;
    }

    /**
     * Defines the transport setup.
     * 
     * @param transport the transport setup
     */
    public void setTransport(TransportSetup transport) {
        this.transport = transport;
    }

    /**
     * Reads a {@link MonitoringSetup} instance from {@link AbstractSetup#DEFAULT_FNAME) in the root folder 
     * of the jar/classpath. 
     *
     * @return the configuration instance
     * @see #readFromYaml(Class)
     */
    public static MonitoringSetup readConfiguration() throws IOException {
        return readFromYaml(MonitoringSetup.class);
    }

    /**
     * Returns the setup instance.
     * 
     * @return the instance
     */
    public static MonitoringSetup getInstance() {
        if (null == instance) {
            try {
                instance = readConfiguration();
            } catch (IOException e) {
                instance = new MonitoringSetup();
            }
        }
        return instance;
    }

}
