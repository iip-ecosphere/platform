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

package de.iip_ecosphere.platform.support.aas.types.common;

import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * A delegating type representat for {@link SubmodelElementContainerBuilder}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class DelegatingSubmodelElementContainerBuilder extends AbstractDelegatingBuilder 
    implements SubmodelElementContainerBuilder {

    /**
     * Creates a delegating builder without parent.
     */
    protected DelegatingSubmodelElementContainerBuilder() {
    }

    /**
     * Creates a delegating builder with parent.
     * 
     * @param parent the parent
     */
    protected DelegatingSubmodelElementContainerBuilder(AbstractDelegatingBuilder parent) {
        super(parent);
    }
    
}
