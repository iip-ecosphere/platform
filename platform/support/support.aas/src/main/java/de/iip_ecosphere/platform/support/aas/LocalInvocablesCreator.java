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

import org.slf4j.LoggerFactory;

/**
 * A local invocables creator for pure local calls. Functions are mapped to service functions 
 * of {@link InvocablesCreator}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalInvocablesCreator implements InvocablesCreator {

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
    public Consumer<Object> createSetter(String name) {
        return o -> {
            try {
                Consumer<Object> tmp = instance.getSetter(name);
                if (null != tmp) {
                    tmp.accept(o);
                }
            } catch (Throwable t) { // catch all, even runtime
                LoggerFactory.getLogger(LocalInvocablesCreator.class).error("Getter " + name + ": " + t.getMessage());
            }
        };
    }

    @Override
    public Function<Object[], Object> createInvocable(String name) {
        return p -> {
            try {
                Function<Object[], Object> tmp = instance.getServiceFunction(name);
                return null == tmp ? null : tmp.apply(p);
            } catch (Throwable t) { // catch all, even runtime
                LoggerFactory.getLogger(LocalInvocablesCreator.class).error("Function " + name + ": " + t.getMessage());
                return null;
            }
        };
    }

    @Override
    public Supplier<Object> createGetter(String name) {
        return () -> {
            try {
                Supplier<Object> tmp = instance.getGetter(name);
                return null == tmp ? null : tmp.get();
            } catch (Throwable t) { // catch all, even runtime
                LoggerFactory.getLogger(LocalInvocablesCreator.class).error("Setter " + name + ": " + t.getMessage());
                return null;
            }
        };
    }
    
    // checkstyle: resume exception type check

}
