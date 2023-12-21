/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;

/**
 * Multi-Language property representation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxRelationshipElement extends BaSyxSubmodelElement implements RelationshipElement {

    private org.eclipse.basyx.submodel.metamodel.map.submodelelement.relationship.RelationshipElement relationship;

    /**
     * Builder for {@link BaSyxProperty}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxRelationshipElementBuilder implements RelationshipElementBuilder {

        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxRelationshipElement instance;

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the property
         * @param first the first reference
         * @param second the second reference
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; {@code first} and {@code second}
         *     must be of type {@link BaSyxReference}
         */
        BaSyxRelationshipElementBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, 
            Reference first, Reference second) {
            if (!(first instanceof BaSyxReference)) {
                throw new IllegalArgumentException("first reference not instanceof BaSyxReference");
            }
            if (!(second instanceof BaSyxReference)) {
                throw new IllegalArgumentException("second reference not instanceof BaSyxReference");
            }
            
            this.parentBuilder = parentBuilder;
            this.instance = new BaSyxRelationshipElement();
            this.instance.relationship = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.relationship.
                RelationshipElement(Tools.checkId(idShort), ((BaSyxReference) first).getReference(), 
                ((BaSyxReference) second).getReference());
        }

        @Override
        public RelationshipElementBuilder setDescription(LangString... description) {
            instance.relationship.setDescription(Tools.translate(description));
            return this;
        }

        @Override
        public RelationshipElementBuilder setSemanticId(String refValue) {
            IReference ref = Tools.translateReference(refValue);
            if (ref != null) {
                instance.relationship.setSemanticId(ref);
            }
            return this;
        }
        
        @Override
        public RelationshipElement build() {
            return parentBuilder.register(instance);
        }

    }

    @Override
    public String getIdShort() {
        return relationship.getIdShort();
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitRelationshipElement(this);
    }

    @Override
    ISubmodelElement getSubmodelElement() {
        return relationship;
    }

    @Override
    public String getSemanticId(boolean stripPrefix) {
        return Tools.translateReference(relationship.getSemanticId(), stripPrefix);
    }

    @Override
    public Map<String, LangString> getDescription() {
        return Tools.translate(relationship.getDescription());
    }

}
