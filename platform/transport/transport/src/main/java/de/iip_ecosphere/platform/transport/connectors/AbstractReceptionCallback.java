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
package de.iip_ecosphere.platform.transport.connectors;

/**
 * An abstract reception callback.
 * 
 * @param <T> the type of the data for the callback
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractReceptionCallback<T> implements ReceptionCallback<T> {

    private Class<T> type;

    /**
     * Creates the callback instance.
     * 
     * @param type the type of the data
     */
    protected AbstractReceptionCallback(Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
