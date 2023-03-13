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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static final String ENV_SUPPORTED_APPIDS = "IIP_SUPPORTED_APPIDS";
    public static final String PROPERTY_SUPPORTED_APPIDS = "iip.supportedAppIds";
    private AasSetup aas = new AasSetup();
    private String serviceProtocol = AasFactory.DEFAULT_PROTOCOL;
    private TransportSetup transport = new TransportSetup();
    private NetworkManagerSetup netMgr = new NetworkManagerSetup();
    private List<String> serviceCmdArgs = new ArrayList<>();
    private List<String> supportedAppIds = initialSupportedAppIds();

    /**
     * Tries to read the supported App ids from {@link #PROPERTY_SUPPORTED_APPIDS} as environment variable or
     * java system property.
     * 
     * @return the initial App ids, may be empty
     */
    private static List<String> initialSupportedAppIds() {
        List<String> result;
        String tmp = System.getenv(ENV_SUPPORTED_APPIDS);
        if (null == tmp) {
            tmp = "";
        }
        tmp = System.getProperty(PROPERTY_SUPPORTED_APPIDS, tmp);
        if (tmp.trim().length() > 0) {
            result = Arrays.asList(tmp.split(","));
        } else {
            result = new ArrayList<>();
        }
        return result;
    }
    
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
     * Returns additional global service command arguments, e.g., for testing.
     * 
     * @return additional service command arguments
     */
    public List<String> getServiceCmdArgs() {
        return serviceCmdArgs;
    }

    /**
     * Returns the supported App Ids.
     * 
     * @return the supported App Ids, may be empty for no restriction
     */
    public List<String> getSupportedAppIds() {
        return supportedAppIds;
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
    
    /**
     * Additional global service command arguments, e.g., for testing. [snakeyaml]
     * 
     * @param serviceCmdArgs the additional command arguments
     */
    public void setServiceCmdArgs(List<String> serviceCmdArgs) {
        if (null == serviceCmdArgs) {
            serviceCmdArgs = new ArrayList<>();
        }
        this.serviceCmdArgs = serviceCmdArgs;
    }
    
    /**
     * Changes the supported App Ids. [snakeyaml]
     * 
     * @param supportedAppIds the supported App Ids, may be empty for no restriction
     */
    public void setSupportedAppIds(List<String> supportedAppIds) {
        if (null == supportedAppIds) {
            supportedAppIds = new ArrayList<>();
        }
        this.supportedAppIds = supportedAppIds;
    }


}
