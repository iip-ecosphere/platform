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

package de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Setup for the service environment.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EnvironmentSetup extends AbstractSetup {
    
    private TransportSetup transport = new TransportSetup();
    private AasSetup aas = new AasSetup();

    /**
     * Returns the transport setup.
     * 
     * @return the transport setup
     */
    public TransportSetup getTransport() {
        return transport;
    }

    /**
     * Returns the AAS setup.
     * 
     * @return the AAS setup
     */
    public AasSetup getAas() {
        return aas;
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
     * Changes the AAS setup.
     * 
     * @param aas the AAS setup
     * @deprecated use {@link #setAas(AasSetup)} instead
     */
    @Deprecated
    public void setAasSetup(AasSetup aas) {
        this.aas = aas;
    }

    /**
     * Changes the AAS setup.
     * 
     * @param aas the AAS setup
     */
    public void setAas(AasSetup aas) {
        this.aas = aas;
    }

}
