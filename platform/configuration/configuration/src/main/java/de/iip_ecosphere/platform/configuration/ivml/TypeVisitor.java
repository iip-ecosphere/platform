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

import de.iip_ecosphere.platform.support.aas.Type;
import net.ssehub.easy.varModel.model.datatypes.AnyType;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatypeVisitor;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.MetaType;
import net.ssehub.easy.varModel.model.datatypes.OrderedEnum;
import net.ssehub.easy.varModel.model.datatypes.RealType;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.datatypes.Set;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.datatypes.VersionType;

/**
 * A visitor turning an IVML type into an AAS type.
 * 
 * @author Holger Eichelberger, SSE
 */
class TypeVisitor implements IDatatypeVisitor {

    private Type aasType;
    
    /**
     * Returns the corresponding AAS type and clears this iterator for reuse.
     * 
     * @return the AAS value
     */
    Type getAasType() {
        Type result = aasType;
        aasType = null;
        return result;
    }

    @Override
    public void visitDatatype(IDatatype datatype) {
        aasType = Type.NONE; // shall not occur, project
    }

    @Override
    public void visitAnyType(AnyType datatype) {
        aasType = Type.NONE; // shall not occur, in expressions
    }

    @Override
    public void visitMetaType(MetaType datatype) {
        aasType = Type.NONE; // shall not occur, in expressions
    }

    @Override
    public void visitDerivedType(DerivedDatatype datatype) {
        DerivedDatatype.resolveToBasis(datatype).accept(this);
    }

    @Override
    public void visitSet(Set set) {
        aasType = Type.NONE; // shall not occur, mapped above into submodelelementcollection            
    }

    @Override
    public void visitSequence(Sequence sequence) {
        aasType = Type.NONE; // shall not occur, mapped above into submodelelementcollection            
    }

    @Override
    public void visitReference(Reference reference) {
        aasType = Type.STRING; // initial, may become an AAS reference
    }

    @Override
    public void visitBooleanType(BooleanType type) {
        aasType = Type.BOOLEAN;
    }

    @Override
    public void visitStringType(StringType type) {
        aasType = Type.STRING;
    }

    @Override
    public void visitConstraintType(ConstraintType type) {
        aasType = Type.STRING; // alternative could be sub-structure
    }

    @Override
    public void visitIntegerType(IntegerType type) {
        aasType = Type.INTEGER;
    }

    @Override
    public void visitVersionType(VersionType type) {
        aasType = Type.STRING; // turned into String
    }

    @Override
    public void visitRealType(RealType type) {
        aasType = Type.FLOAT; // no real in AAS
    }

    @Override
    public void visitCompoundType(Compound compound) {
        aasType = Type.NONE; // shall not occur, mapped above into submodelelementcollection            
    }

    @Override
    public void visitEnumType(Enum enumType) {
        aasType = Type.STRING; // map to literal
    }

    @Override
    public void visitOrderedEnumType(OrderedEnum enumType) {
        aasType = Type.STRING; // map to literal
    }
    
}