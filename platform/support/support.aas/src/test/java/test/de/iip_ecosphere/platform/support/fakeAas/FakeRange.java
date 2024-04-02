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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Range;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Fake implementation of {@link Range}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeRange extends FakeElement implements Range {

    private Type type;
    private Object min;
    private Object max;

    /**
     * The element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeRangeBuilder implements RangeBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeRange instance;

        /**
         * Creates a range builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param type the value type
         * @param min the minimum value
         * @param max the maximum value
         */
        FakeRangeBuilder(FakeSubmodelElementContainerBuilder parent, String idShort, Type type,
            Object min, Object max) {
            this.parent = parent;
            this.instance = new FakeRange(idShort, type, min, max);
        }

        @Override
        public Range build() {
            return parent.register(instance);
        }

        @Override
        public RangeBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }
        
    }
    
    /**
     * Creates a fake data element.
     * 
     * @param idShort the short id
     * @param type the value type
     * @param min the minimum value
     * @param max the maximum value
     */
    FakeRange(String idShort, Type type, Object min, Object max) {
        super(idShort);
        this.type = type;
        this.min = min;
        this.max = max;
    }
    
    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitRange(this);
    }

    @Override
    public void update() {
    }

    @Override
    public Object getMin() {
        return min;
    }

    @Override
    public void setMin(Object min) {
        this.min = min;
    }

    @Override
    public Object getMax() {
        return max;
    }

    @Override
    public void setMax(Object max) {
        this.max = max;
    }

    @Override
    public Type getType() {
        return type;
    }

}
