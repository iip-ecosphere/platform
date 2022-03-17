/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/


package de.iip_ecosphere.platform.monitoring.prometheus;

import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;

public class Callback extends AbstractReceptionCallback<TestObject> {
    
    @SuppressWarnings("unused")
    private TestObject data;
    
    /** Callback.
     * 
     */
    public Callback() {
        super(TestObject.class);
    }

    @Override
    public void received(TestObject data) {
        this.data = data;
    }
    
}
