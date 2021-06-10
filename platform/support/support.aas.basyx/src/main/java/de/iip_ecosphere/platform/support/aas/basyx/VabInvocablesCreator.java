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
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.api.IBaSyxConnector;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;

/**
 * implements an invocables creator for the VAB following the naming conventions of {@link VabOperationsProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VabInvocablesCreator implements InvocablesCreator {

    private IModelProvider provider;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param connector the connector to use
     */
    VabInvocablesCreator(IBaSyxConnector connector) {
        this.provider = new JSONConnector(connector); 
    }
    
    @Override
    public Supplier<Object> createGetter(String name) {
        return () -> {
            VABElementProxy proxy = new VABElementProxy("", provider);
            return proxy.getModelPropertyValue(VabOperationsProvider.PREFIX_STATUS + name);
        };
    }

    @Override
    public Consumer<Object> createSetter(String name) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", provider);
            proxy.setModelPropertyValue(VabOperationsProvider.PREFIX_STATUS + name, params);
        };
    }

    @Override
    public Function<Object[], Object> createInvocable(String name) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", provider);
            return proxy.invokeOperation(VabOperationsProvider.PREFIX_SERVICE + name, params);
        };
    }

}
