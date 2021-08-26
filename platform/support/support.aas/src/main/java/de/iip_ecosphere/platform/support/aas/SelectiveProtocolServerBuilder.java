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
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Server;

/**
 * A delegating protocol server that switches based on predicates between two server builders, 
 * e.g., a local and a remote builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SelectiveProtocolServerBuilder implements ProtocolServerBuilder {

    private Predicate<String> propSelector;
    private Predicate<String> opSelector;
    private ProtocolServerBuilder builder1;
    private ProtocolServerBuilder builder2;

    /**
     * Creates the builder instance.
     * 
     * @param propSelector selector for properties based on the property name; {@code true} as return value
     *   mandates {@code builder1}, {@code false} {@code builder2}
     * @param opSelector selector for operations based on the operation name; {@code true} as return value
     *   mandates {@code builder1}, {@code false} {@code builder2}
     * @param builder1 the first builder
     * @param builder2 the second builder
     */
    public SelectiveProtocolServerBuilder(Predicate<String> propSelector, Predicate<String> opSelector,
        ProtocolServerBuilder builder1, ProtocolServerBuilder builder2) {
        this.propSelector = propSelector;
        this.opSelector = opSelector;
        this.builder1 = builder1;
        this.builder2 = builder2;
    }

    @Override
    public Server build() {
        return builder1.build();
    }

    @Override
    public ProtocolServerBuilder defineOperation(String name, Function<Object[], Object> function) {
        if (opSelector.test(name)) {
            builder1.defineOperation(name, function);
        } else {
            builder2.defineOperation(name, function);
        }
        return this;
    }

    @Override
    public ProtocolServerBuilder defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
        if (propSelector.test(name)) {
            builder1.defineProperty(name, get, set);
        } else {
            builder2.defineProperty(name, get, set);
        }
        return this;
    }

    @Override
    public PayloadCodec createPayloadCodec() {
        return builder1.createPayloadCodec();
    }

}
