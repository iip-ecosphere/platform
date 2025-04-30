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

package de.iip_ecosphere.platform.support.aas;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import java.io.Serializable;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Invokable.GetterInvokable;
import de.iip_ecosphere.platform.support.aas.Invokable.OperationInvokable;
import de.iip_ecosphere.platform.support.aas.Invokable.SetterInvokable;

/**
 * A local invocables creator for pure local calls. Functions are mapped to service functions 
 * of {@link InvocablesCreator}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalInvocablesCreator implements InvocablesCreator, Serializable {

    private static final long serialVersionUID = -3267383035483812825L;
    private OperationsProvider instance;

    /**
     * Creates a local invocables creator.
     * 
     * @param instance the operations provider instance holding the operations
     */
    public LocalInvocablesCreator(OperationsProvider instance) {
        this.instance = instance;
    }

    // checkstyle: stop exception type check

    @Override
    public Invokable createSetter(String name) {
        return (SetterInvokable & Serializable) (o -> {
            try {
                Consumer<Object> tmp = instance.getSetter(name);
                if (null != tmp) {
                    tmp.accept(o);
                }
            } catch (Throwable t) { // catch all, even runtime
                LoggerFactory.getLogger(LocalInvocablesCreator.class).error("Getter " + name + ": " + t.getMessage());
            }
        });
    }

    @Override
    public Invokable createInvocable(String name) {
        return (OperationInvokable & Serializable) (p -> {
            try {
                Function<Object[], Object> tmp = instance.getServiceFunction(name);
                return null == tmp ? null : tmp.apply(p);
            } catch (Throwable t) { // catch all, even runtime
                LoggerFactory.getLogger(LocalInvocablesCreator.class).error("Function " + name + ": " + t.getMessage());
                return null;
            }
        });
    }

    @Override
    public Invokable createGetter(String name) {
        return (GetterInvokable & Serializable) (() -> {
            try {
                Supplier<Object> tmp = instance.getGetter(name);
                return null == tmp ? null : tmp.get();
            } catch (Throwable t) { // catch all, even runtime
                LoggerFactory.getLogger(LocalInvocablesCreator.class).error("Setter " + name + ": " + t.getMessage());
                return null;
            }
        });
    }
    
    // checkstyle: resume exception type check

}
