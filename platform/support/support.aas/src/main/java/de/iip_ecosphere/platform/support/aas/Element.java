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

/**
 * Basis interface for all AAS elements.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Element {

    /**
     * Returns the short id of the element.
     * 
     * @return the short id
     */
    public String getIdShort();

    /**
     * Accepts and handles a visitor.
     * 
     * @param visitor the visitor
     */
    public void accept(AasVisitor visitor);

    /**
     * Causes cached data to be updated. If an implementation relies on life data/direct queries against the
     * corresponding AAS element, nothing needs to be done. If an implementation caches information, the caches
     * shall be refreshed through this update or reset so that the next query returns fresh data.
     */
    public void update();
    
}
