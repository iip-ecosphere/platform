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

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;

/**
 * Extended server address holder for prometheus.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrometheusServerAddressHolder extends ServerAddressHolder {

    private boolean running = false;

    /**
     * Creates an instance (deserialization).
     */
    public PrometheusServerAddressHolder() {
    }
    
    /**
     * Creates an instance.
     * 
     * @param schema the schema
     * @param host the host name
     * @param port the port
     */
    public PrometheusServerAddressHolder(Schema schema, String host, int port) {
        super(schema, host, port);
    }

    /**
     * Creates an instance from a given instance (serialization).
     * 
     * @param addr the instance to take data from
     */
    public PrometheusServerAddressHolder(ServerAddress addr) {
        super(addr);
    }

    /**
     * Returns whether Prometheus is already running.
     * 
     * @return whether we can assume that it is already running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Changes whether Prometheus is already running. [snakeyaml]
     * 
     * @param running whether we can assume that it is already running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

}
