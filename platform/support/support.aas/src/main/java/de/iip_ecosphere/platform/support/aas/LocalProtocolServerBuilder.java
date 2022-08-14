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

import de.iip_ecosphere.platform.support.NoOpServer;
import de.iip_ecosphere.platform.support.Server;

/**
 * A local protocol server builder for pure local calls.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalProtocolServerBuilder implements ProtocolServerBuilder {

    private OperationsProvider instance;

    /**
     * Creates local protocol server builder.
     * 
     * @param instance the instance holding the operations (to be modified as a side effect)
     */
    public LocalProtocolServerBuilder(OperationsProvider instance) {
        this.instance = instance;
    }

    @Override
    public Server build() {
        return new NoOpServer();
    }

    @Override
    public ProtocolServerBuilder defineOperation(String name, Function<Object[], Object> function) {
        instance.defineServiceFunction(name, function);
        return this;
    }

    @Override
    public ProtocolServerBuilder defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
        instance.defineProperty(name, get, set);
        return this;
    }

    @Override
    public PayloadCodec createPayloadCodec() {
        return new SerialPayloadCodec();
    }

    @Override
    public boolean isAvailable(String host) {
        return true; // NoOpServer
    }

}
