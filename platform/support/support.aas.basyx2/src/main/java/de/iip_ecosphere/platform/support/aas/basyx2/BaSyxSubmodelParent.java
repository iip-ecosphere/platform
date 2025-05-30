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

package de.iip_ecosphere.platform.support.aas.basyx2;

import de.iip_ecosphere.platform.support.aas.basyx2.AbstractAas.BaSyxAbstractAasBuilder;

/**
 * Represents the parent instance of a sub-model. Due to the two different AAS types in BaSyx, this
 * cannot just be an AAS instance rather than a pseudo instance being able to provide the correct 
 * operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface BaSyxSubmodelParent {
    
    /**
     * Creates an AAS builder on parent level.
     * 
     * @return the AAS builder
     */
    public BaSyxAbstractAasBuilder createAasBuilder();
    
}