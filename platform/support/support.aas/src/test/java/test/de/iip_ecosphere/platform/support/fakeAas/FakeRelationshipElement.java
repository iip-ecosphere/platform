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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;

/**
 * Implements a fake relationship element for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeRelationshipElement extends FakeElement implements RelationshipElement {

    private FakeReference first;
    private FakeReference second;
    
    /**
     * A fake relationship builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeRelationshipElementBuilder implements RelationshipElementBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeRelationshipElement instance;
        
        /**
         * Creates the fake property builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param first the first element
         * @param second the second element
         */
        FakeRelationshipElementBuilder(FakeSubmodelElementContainerBuilder parent, String idShort, 
            Reference first, Reference second) {
            this.parent = parent;
            this.instance = new FakeRelationshipElement(idShort, (FakeReference) first, (FakeReference) second);
        }
 
        @Override
        public RelationshipElementBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }
        
        @Override
        public RelationshipElementBuilder setDescription(LangString... description) {
            instance.setDescription(description);
            return this;
        }
        
        @Override
        public RelationshipElement build() {
            return parent.register(instance);
        }

    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     * @param first the first element
     * @param second the second element
     */
    protected FakeRelationshipElement(String idShort, FakeReference first, FakeReference second) {
        super(idShort);
        this.first = first;
        this.second = second;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitRelationshipElement(this);
    }

    @Override
    public Reference getFirst() {
        return first;
    }

    @Override
    public Reference getSecond() {
        return second;
    }

}
