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
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnector;

/**
 * implements an invocables creator for TCP-VAB.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VabHttpInvocablesCreator extends VabInvocablesCreator {

    private static final long serialVersionUID = 2161996616248269342L;
    private String address;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param address the HTTP address to connect to
     */
    VabHttpInvocablesCreator(String address) {
        this.address = address;
    }
    
    @Override
    protected VABElementProxy createProxy() {
        return new VABElementProxy("", new JSONConnector(new HTTPConnector(address)));
    }

    @Override
    protected String getId() {
        return address;
    }

}
