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

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;

/**
 * Implements the reference element wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxReferenceElement extends BaSyxSubmodelElement implements ReferenceElement {
    
    private org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement reference;
    
    /**
     * Implements the reference element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxReferenceElementBuilder implements ReferenceElementBuilder {
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxReferenceElement instance;
        private org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement reference;
        
        /**
         * Creates a builder instance.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id of the reference element
         */
        BaSyxReferenceElementBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            instance = new BaSyxReferenceElement();
            reference = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement();
            reference.setIdShort(idShort);
        }
        
        @Override
        public BaSyxSubmodelElementContainerBuilder<?> getParentBuilder() {
            return parentBuilder;
        }

        @Override
        public ReferenceElement build() {
            instance.reference = reference;
            return updateInBuild(true, parentBuilder.register(instance));
        }

        @Override
        public ReferenceElementBuilder setValue(Reference value) {
            if (!(value instanceof BaSyxReference)) {
                throw new IllegalArgumentException("value must be of type reference");
            }
            reference.setValue(((BaSyxReference) value).getReference()); 
            return this;
        }
        
        @Override
        public ReferenceElementBuilder setSemanticId(String refValue) {
            return Tools.setSemanticId(this, refValue, reference);
        }

        @Override
        public ReferenceElementBuilder setDescription(LangString... description) {
            reference.setDescription(Tools.translate(description));
            return this;
        }
        
    }
    
    /**
     * Creates an instance. Prevents external access.
     */
    private BaSyxReferenceElement() {
    }
    
    /**
     * Creates an instance and directly sets the reference.
     * 
     * @param reference the reference
     */
    BaSyxReferenceElement(org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement reference) {
        this.reference = reference;
    }
    
    /**
     * Returns the BaSyx reference element.
     * 
     * @return the BaSyx reference element
     */
    org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement getReferenceElement() {
        return reference;
    }

    // checkstyle: stop exception type check

    @Override
    public Reference getValue() {
        return new BaSyxReference(reference.getValue());
    }

    // checkstyle: resume exception type check

    @Override
    org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement getSubmodelElement() {
        return reference;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitReferenceElement(this);
    }

}
