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

import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.IntValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Utilities to access IVML/configuration values.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlUtils {

    /**
     * Returns whether the given {@code var} is of compound type with name {@code typeName}.
     * 
     * @param var the variable
     * @param typeName the type name of the compound
     * @return {@code true} if {@code var} is of a compound type with {@code typeName}, {@code false} else
     */
    public static boolean isOfCompoundType(IDecisionVariable var, String typeName) {
        IDatatype type = var.getDeclaration().getType();
        return TypeQueries.isCompound(type) && typeName.equals(type.getName());
    }

    /**
     * Returns a nested variable, returning <b>null</b> if {@code var} is null.
     * 
     * @param var the variable
     * @param nested the name of the nested variable
     * @return the nested variable or <b>null</b>
     */
    public static IDecisionVariable getNestedSafe(IDecisionVariable var, String nested) {
        return null == var ? null : var.getNestedElement(nested);
    }
    
    /**
     * Returns a string value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static String getStringValue(IDecisionVariable var, String deflt) {
        String result;
        if (var == null) {
            result = deflt;
        } else {
            Value val = var.getValue();
            if (!(val instanceof StringValue)) {
                result = deflt;
            } else {
                result = ((StringValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns an integer value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static int getIntValue(IDecisionVariable var, int deflt) {
        int result;
        if (var == null) {
            result = deflt;
        } else {
            Value val = var.getValue();
            if (!(val instanceof IntValue)) {
                result = deflt;
            } else {
                result = ((IntValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns a Boolean value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static boolean getBooleanValue(IDecisionVariable var, boolean deflt) {
        boolean result;
        if (var == null) {
            result = deflt;
        } else {
            Value val = var.getValue();
            if (!(val instanceof BooleanValue)) {
                result = deflt;
            } else {
                result = ((BooleanValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns the name of {@code var} taking <b>null</b> into account.
     * 
     * @param var the variable
     * @param dflt the default if {@code var} is <b>null</b>
     * @return the name or {@code dflt}
     */
    public static String getVarNameSafe(AbstractVariable var, String dflt) {
        return null == var ? dflt : var.getName();
    }

}
