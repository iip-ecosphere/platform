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

package test.de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * Implements a fake reference element holding a reference.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeReferenceElement extends FakeElement implements ReferenceElement {
   
    private Reference value;
    
    static class FakeReferenceElementBuilder implements ReferenceElementBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeReferenceElement instance;

        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        FakeReferenceElementBuilder(FakeSubmodelElementContainerBuilder parent, String idShort) {
            this.parent = parent;
            this.instance = new FakeReferenceElement(idShort, null);
        }
        
        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return parent;
        }

        @Override
        public ReferenceElementBuilder setValue(Reference value) {
            instance.value = value;
            return this;
        }

        @Override
        public ReferenceElement build() {
            parent.register(instance);
            return instance;
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the short id
     * @param value the reference value
     */
    protected FakeReferenceElement(String idShort, Reference value) {
        super(idShort);
        this.value = value;
    }
    
    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitReferenceElement(this);
    }

    @Override
    public Reference getValue() {
        return value;
    }

}
