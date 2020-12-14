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

package de.iip_ecosphere.platform.support.aas.basyx;

/**
 * Internal interface representing an element container.
 * 
 * @author Holger Eichelberger, SSE
 */
interface BaSyxElementContainer {

    /**
     * Returns the sub-model element container builder for this element container for adding elements. May ask 
     * its parent or return the builder directly.
     * 
     * @return the builder
     */
    BaSyxSubmodelElementContainerBuilder<?> getSubmodelElementContainerBuilder();

}
