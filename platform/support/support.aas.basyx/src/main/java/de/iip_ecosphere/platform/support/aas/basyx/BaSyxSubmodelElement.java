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

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;

import de.iip_ecosphere.platform.support.aas.SubmodelElement;

/**
 * Implements an abstract BaSyx sub-model element wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class BaSyxSubmodelElement implements SubmodelElement {

    /**
     * Returns the implementing sub-model element.
     * 
     * @return the submodel element
     */
    abstract ISubmodelElement getSubmodelElement();
    
}
