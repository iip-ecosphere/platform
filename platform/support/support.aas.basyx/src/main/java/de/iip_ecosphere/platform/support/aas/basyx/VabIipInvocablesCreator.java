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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;

/**
 * implements an invocables creator for the VAB following the naming conventions of {@link VabIipOperationsProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VabIipInvocablesCreator implements InvocablesCreator {

    private String host;
    private int port;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param host the host to communicate with
     * @param port the port to communicate on
     */
    VabIipInvocablesCreator(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public Supplier<Object> createGetter(String name) {
        return () -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            return proxy.getModelPropertyValue(VabIipOperationsProvider.PREFIX_STATUS + name);
        };
    }

    @Override
    public Consumer<Object> createSetter(String name) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            proxy.setModelPropertyValue(VabIipOperationsProvider.PREFIX_STATUS + name, params);
        };
    }

    @Override
    public Function<Object[], Object> createInvocable(String name) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            return proxy.invokeOperation(VabIipOperationsProvider.PREFIX_SERVICE + name, params);
        };
    }

}
