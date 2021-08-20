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

import de.iip_ecosphere.platform.support.aas.basyx.basyx.BaSyxConnector;

/**
 * implements an invocables creator for TCP-VAB.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VabTcpInvocablesCreator extends VabInvocablesCreator {

    // experimental feature, just keep open if explicitly desired
    private static final boolean VAB_KEEP_OPEN = Boolean.valueOf(System.getProperty("vab.keep.open", "false"));
    
    private static final long serialVersionUID = 4353249016693669189L;
    private String host;
    private int port;
    private String id;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param host the host name to connect to
     * @param port the port number to connect to
     */
    VabTcpInvocablesCreator(String host, int port) {
        this.host = host;
        this.port = port;
        id = host + ":" + port;
    }
    
    @Override
    protected VABElementProxy createProxy() {
        return new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port, VAB_KEEP_OPEN)));
    }

    @Override
    protected String getId() {
        return id;
    }

}
