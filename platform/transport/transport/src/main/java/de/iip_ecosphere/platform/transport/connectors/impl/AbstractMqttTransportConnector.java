/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package de.iip_ecosphere.platform.transport.connectors.impl;

/**
 * An abstract MQTT transport connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractMqttTransportConnector extends AbstractTransportConnector {

    /**
     * Composes a hierarchical stream name (in the syntax/semantics of the
     * connector).
     * 
     * @param parent the parent name (may be {@link #EMPTY_PARENT} for top-level streams)
     * @param name   the name of the stream
     * @return the composed name
     * @throws IllegalArgumentException in case that the stream name is (structurally) illegal
     */
    public static String composeNames(String parent, String name) {
        return parent != null && parent.length() > 0 ? parent + "/" + name : name;
    }

    @Override
    public String composeStreamName(String parent, String name) {
        return composeNames(parent, name);
    }

}
