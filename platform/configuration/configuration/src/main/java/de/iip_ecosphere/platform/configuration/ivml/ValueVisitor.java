/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.ivml;

import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ConstraintValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.IValueVisitor;
import net.ssehub.easy.varModel.model.values.IntValue;
import net.ssehub.easy.varModel.model.values.MetaTypeValue;
import net.ssehub.easy.varModel.model.values.NullValue;
import net.ssehub.easy.varModel.model.values.RealValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.VersionValue;
import net.ssehub.easy.varModel.persistency.StringProvider;

/**
 * A visitor turning an IVML value into an AAS value.
 * 
 * @author Holger Eichelberger, SSE
 */
class ValueVisitor implements IValueVisitor {
    
    private Object aasValue;
    
    /**
     * Returns the corresponding AAS value and clears this iterator for reuse.
     * 
     * @return the AAS value
     */
    Object getAasValue() {
        Object result = aasValue;
        aasValue = null;
        return result;
    }

    @Override
    public void visitConstraintValue(ConstraintValue value) {
        aasValue = StringProvider.toIvmlString(value.getValue());
    }

    @Override
    public void visitEnumValue(EnumValue value) {
        aasValue = value.getValue().getName();
    }

    @Override
    public void visitStringValue(StringValue value) {
        aasValue = value.getValue();
    }

    @Override
    public void visitCompoundValue(CompoundValue value) {
        // no value, mapped above
    }

    @Override
    public void visitContainerValue(ContainerValue value) {
        // no value, mapped above
    }

    @Override
    public void visitIntValue(IntValue value) {
        aasValue = value.getValue();
    }

    @Override
    public void visitRealValue(RealValue value) {
        aasValue = value.getValue();
    }

    @Override
    public void visitBooleanValue(BooleanValue value) {
        aasValue = value.getValue();
    }

    @Override
    public void visitReferenceValue(ReferenceValue referenceValue) {
        if (referenceValue.getValue() != null) {
            aasValue = referenceValue.getValue().getName();
        } else if (referenceValue.getValueEx() != null) {
            aasValue = null; // do not represent this
        } else {
            aasValue = null; // undefined
        }
    }

    @Override
    public void visitMetaTypeValue(MetaTypeValue value) {
        aasValue = null; // shall not occur, in expressions
    }

    @Override
    public void visitNullValue(NullValue value) {
        aasValue = null;
    }

    @Override
    public void visitVersionValue(VersionValue value) {
        aasValue = value.getValue().toString();
    }
    
}