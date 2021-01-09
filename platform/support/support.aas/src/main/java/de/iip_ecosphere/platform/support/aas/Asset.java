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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Defines the basic interface of an Asset.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Asset {
    
    /**
     * The builder for {@link Asset}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface AssetBuilder extends Builder<Asset> {
        
    }
    
    // incomplete
    
    /**
     * Returns the type of the asset.
     * 
     * @return the type
     */
    public AssetKind getAssetKind();

    // so far additional
    
    /**
     * The short id of the asset.
     * 
     * @return the short id
     */
    public String getIdShort();

    /**
     * Accepts the given visitor.
     * 
     * @param visitor the visitor instance
     */
    public void accept(AasVisitor visitor);
    
}
