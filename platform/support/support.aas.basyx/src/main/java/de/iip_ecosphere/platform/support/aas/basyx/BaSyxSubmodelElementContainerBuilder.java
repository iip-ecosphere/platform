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

import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * Basic implementation for a container-based model element.
 * 
 * @author Holger Eichelberger, SSE
 */
abstract class BaSyxSubmodelElementContainerBuilder implements SubmodelElementContainerBuilder {

    @Override
    public PropertyBuilder createPropertyBuilder(String idShort) {
        return new BaSyxProperty.BaSyxPropertyBuilder(this, idShort);
    }

    @Override
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
        return new BaSyxReferenceElement.BaSyxReferenceElementBuilder(this, idShort);
    }
    
    @Override
    public OperationBuilder createOperationBuilder(String idShort) {
        return new BaSyxOperation.BaSxyOperationBuilder(this, idShort);
    }
    
    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    abstract BaSyxOperation register(BaSyxOperation operation);
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    abstract BaSyxProperty register(BaSyxProperty property);

    /**
     * Registers a reference element.
     * 
     * @param reference the reference
     * @return {@code reference}
     */
    abstract BaSyxReferenceElement register(BaSyxReferenceElement reference);

    /**
     * Registers a sub-model element collection.
     * 
     * @param collection the collection
     * @return {@code collection}
     */
    abstract BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection);

}
