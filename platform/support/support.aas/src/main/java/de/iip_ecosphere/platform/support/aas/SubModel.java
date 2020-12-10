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

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;

/**
 * Represents an AAS sub-model.
 * 
 * @author Holger Eichelberger, SSE
*/
public interface SubModel extends Element, HasSemantics, Identifiable, Qualifiable, HasDataSpecification, HasKind, 
    ElementContainer {

    /**
     * Encapsulated logic to build a sub-model.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SubModelBuilder {
        
        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public AasBuilder getParentBuilder();
        
        /**
         * Creates a builder for a contained property.
         * 
         * @param idShort the short name of the property
         * @return the property builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public PropertyBuilder createPropertyBuilder(String idShort);

        /**
         * Creates a builder for a contained reference element.
         * 
         * @param idShort the short name of the reference element
         * @return the reference element builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public ReferenceElementBuilder createReferenceElementBuilder(String idShort);

        /**
         * Creates a builder for a contained operation.
         * 
         * @param idShort the short name of the operation
         * @return the property builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public OperationBuilder createOperationBuilder(String idShort);
        
        /**
         * Builds the instance.
         * 
         * @return the sub-model instance
         */
        public SubModel build();
        
        /**
         * Creates a reference on the sub-model under construction.
         * 
         * @return the reference
         */
        public Reference createReference();

    }
    
    /**
     * Returns a property with the given name.
     * 
     * @param idShort the short id of the property
     * @return the property, <b>null</b> for none
     */
    public Property getProperty(String idShort);

    /**
     * Returns a reference element with the given name.
     * 
     * @param idShort the short id of the reference element
     * @return the property, <b>null</b> for none
     */
    public ReferenceElement getReferenceElement(String idShort);

    /**
     * Returns an operation with the given name and the given number of arguments.
     * 
     * @param idShort the short id of the property
     * @param numArgs the number of arguments regardless whether they are in/out/inout
     * @return the property, <b>null</b> for none
     */
    public Operation getOperation(String idShort, int numArgs);
    
    /**
     * Returns an operation with the given name and the given number of arguments.
     * 
     * @param idShort the short id of the property
     * @param inArgs the number of ingoing arguments/variables
     * @param outArgs the number of outgoing arguments/variables
     * @param inOutArgs the number of in/outgoing arguments/variables
     * @return the property, <b>null</b> for none
     */
    public Operation getOperation(String idShort, int inArgs, int outArgs, int inOutArgs);

}
