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

import java.io.Serializable;

import org.eclipse.basyx.vab.modelprovider.VABElementProxy;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;

/**
 * Implements an abstract invocables creator for the VAB following the naming conventions of 
 * {@link VabOperationsProvider}. Function objects as well as class itself must be serializable for remote deployment.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class VabInvocablesCreator implements InvocablesCreator, Serializable {

    private static final long serialVersionUID = -4388430468665656598L;
    
    /**
     * Creates the element proxy.
     * 
     * @return the element proxy
     */
    protected abstract VABElementProxy createProxy();
    
    @SuppressWarnings("unchecked")
    @Override
    public Supplier<Object> createGetter(String name) {
        return (Supplier<Object> & Serializable) () -> {
            return createProxy().getModelPropertyValue(VabOperationsProvider.PREFIX_STATUS + name);
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Consumer<Object> createSetter(String name) {
        return (Consumer<Object> & Serializable) (params) -> {
            createProxy().setModelPropertyValue(VabOperationsProvider.PREFIX_STATUS + name, params);
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Function<Object[], Object> createInvocable(String name) {
        return (Function<Object[], Object> & Serializable) (params) -> {
            return createProxy().invokeOperation(VabOperationsProvider.PREFIX_SERVICE + name, params);
        };
    }

}
