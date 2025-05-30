/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2.apps.common;

import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Specific keystore descriptor wrapper for unique injection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AssetServerKeyStoreDescriptor {

    private KeyStoreDescriptor descriptor;

    /**
     * Creates an instance.
     * 
     * @param descriptor the keystore descriptor to create the instance from
     */
    public AssetServerKeyStoreDescriptor(KeyStoreDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    /**
     * Returns the contained descriptor.
     * 
     * @return the descriptor
     */
    public KeyStoreDescriptor getDescriptor() {
        return descriptor;
    }

}
