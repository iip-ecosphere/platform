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

package de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.net.NetworkManagerSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Basic Yaml configuration options.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceSetup {

    private AasSetup aas = new AasSetup();
    private String serviceProtocol = AasFactory.DEFAULT_PROTOCOL;
    private TransportSetup transport = new TransportSetup();
    private NetworkManagerSetup netMgr = new NetworkManagerSetup();

    /**
     * Returns the AAS setup.
     * 
     * @return the AAs setup
     */
    public AasSetup getAas() {
        return aas;
    }
    
    /**
     * Returns the service administration protocol.
     * 
     * @return the service administration protocol (see {@link AasFactory#getProtocols()}
     */
    public String getServiceProtocol() {
        return serviceProtocol;
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
     * Returns the network manager setup.
     * 
     * @return the network manager setup
     */
    public NetworkManagerSetup getNetMgr() {
        return netMgr;
    }    

    /**
     * Defines the AAS setup. [required by snakeyaml]
     * 
     * @param aas the AAS setup
     */
    public void setAas(AasSetup aas) {
        this.aas = aas;
    }

    /**
     * Defines the service administration protocol. [required by snakeyaml]
     * 
     * @param serviceProtocol the service administration protocol (see {@link AasFactory#getProtocols()}
     */
    public void setServiceProtocol(String serviceProtocol) {
        this.serviceProtocol = serviceProtocol;
    }


    /**
     * Defines the transport setup. [required by snakeyaml]
     * 
     * @param transport the transport setup
     */
    public void setTransport(TransportSetup transport) {
        this.transport = transport;
    }

    
    /**
     * Defines the network manager setup. [required snakeyaml]
     * 
     * @param netMgr the network manager setup
     */
    public void setNetMgr(NetworkManagerSetup netMgr) {
        this.netMgr = netMgr;
    }
}
