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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.net.LocalNetworkManagerImpl;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerDescriptor;

/**
 * A default network manager descriptor for JLS loading if a local network manager is needed, which delegates
 * to an AAS-based network manager provided with the default IIP-AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalNetworkManagerWithParentAas implements NetworkManagerDescriptor {

    @Override
    public NetworkManager createInstance() {
        NetworkManager result;
        try {
            result = new LocalNetworkManagerImpl(new NetworkManagerAasClient());
            LoggerFactory.getLogger(getClass()).info("AAS-based network manager installed");
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).warn("Cannot create AAS-based network manager, falling back to "
                + "local network manager: " + e.getMessage());
            result = new LocalNetworkManagerImpl();
        }
        return result;
    }

}
