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

import de.iip_ecosphere.platform.support.aas.AasFactory.ProtocolCreator;

/**
 * A simple, customizable protocol creator for {@link LocalInvocablesCreator} and {@link LocalProtocolServerBuilder}.
 * Creates by default operation providers of type {@link SimpleOperationsProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleLocalProtocolCreator implements ProtocolCreator {

    private OperationsProvider lastProvider;
    
    @Override
    public ProtocolServerBuilder createProtocolServerBuilder(int port) {
        return new LocalProtocolServerBuilder(lastProvider);
    }
    
    @Override
    public InvocablesCreator createInvocablesCreator(String host, int port) {
        lastProvider = createOperationsProvider();
        return new LocalInvocablesCreator(lastProvider);
    }

    /**
     * Creates a new operations provider instance.
     * 
     * @return the operations provider instance
     */
    protected OperationsProvider createOperationsProvider() {
        return new SimpleOperationsProvider();
    }
    
}
