/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;

/**
 * implements an invocables creator for HTTPS-based VAB.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VabHttpsInvocablesCreator extends VabInvocablesCreator {

    private static final long serialVersionUID = 8021322086051502297L;
    private String address;
    private BaSyxJerseyHttpsClientFactory factory;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param address the HTTP address to connect to
     * @param factory the client factory
     */
    VabHttpsInvocablesCreator(String address, BaSyxJerseyHttpsClientFactory factory) {
        this.address = address;
        this.factory = factory;
    }
    
    @Override
    protected VABElementProxy createProxy() {
        return new VABElementProxy("", new JSONConnector(new BaSyxHTTPSConnector(address, factory)));
    }

    @Override
    protected String getId() {
        return address;
    }

}
