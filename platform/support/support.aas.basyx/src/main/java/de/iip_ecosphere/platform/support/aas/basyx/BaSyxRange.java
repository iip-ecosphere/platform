/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.range.RangeValue;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Range;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Wraps a BaSyx range element.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxRange extends BaSyxDataElement<org.eclipse.basyx.submodel.metamodel.map.submodelelement
    .dataelement.range.Range> implements Range {

    /**
     * The builder for a file data element.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxRangeBuilder implements RangeBuilder {

        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxRange instance;
        
        /**
         * Creates a range element builder.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the idshort
         * @param type the value type
         * @param min the minimum value
         * @param max the maximum value
         */
        BaSyxRangeBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, Type type, 
            Object min, Object max) {
            this.parentBuilder = parentBuilder;
            this.instance = new BaSyxRange(idShort, type, min, max);
        }

        @Override
        public Range build() {
            return parentBuilder.register(instance);
        }

        @Override
        public RangeBuilder setSemanticId(String semanticId) {
            IReference ref = Tools.translateReference(semanticId);
            if (ref != null) {
                instance.getDataElement().setSemanticId(ref);
            }
            return this;
        }

    }

    /**
     * Creates a new range element.
     * 
     * @param idShort the idshort
     * @param type the value type
     * @param min the minimum value
     * @param max the maximum value
     */
    private BaSyxRange(String idShort, Type type, Object min, Object max) {
        super(new org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.range.Range(
            idShort, Tools.translate(type)));
        getDataElement().setValue(new RangeValue(min, max));
    }
    
    /**
     * Crates a wrapper instance.
     * 
     * @param range the BaSyx range instance
     */
    public BaSyxRange(org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.range.Range range) {
        super(range);
    }    
    
    @Override
    public Object getMin() {
        return getDataElement().getMin();
    }

    @Override
    public void setMin(Object min) {
        getDataElement().setValue(new RangeValue(min, getMax()));
    }

    @Override
    public Object getMax() {
        return getDataElement().getMax();
    }

    @Override
    public void setMax(Object max) {
        getDataElement().setValue(new RangeValue(getMin(), max));
    }

    @Override
    public Type getType() {
        return Tools.translate(getDataElement().getValueType());
    }
    
    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitRange(this);
    }

}
