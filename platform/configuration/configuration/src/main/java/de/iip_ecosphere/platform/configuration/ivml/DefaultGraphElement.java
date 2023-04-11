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

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphElement;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Default graph element implementation. {@link #getName()} is bound against the nested variable 
 * {@link #getNameVarName()}. Shall serve for a more generic mapping, to be part of EASY-Producer, thus, customizable.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class DefaultGraphElement implements IvmlGraphElement {

    public static final int INVALID_POSITION = 0;
    public static final int INVALID_SIZE = -1;
    private IDecisionVariable var;
    private String name = "";
    
    /**
     * Creates a graph element.
     * 
     * @param var the underlying decision variable
     */
    protected DefaultGraphElement(IDecisionVariable var) {
        this.var = var;
    }

    @Override
    public String getName() {
        return getStringValue(getNameVarName(), name);
    }
    
    /**
     * Returns the IVML variable name of the name of the element.
     * 
     * @return the variable name
     */
    protected String getNameVarName() {
        return "name";
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public IDecisionVariable getVariable() {
        return var;
    }

    /**
     * Returns a String value from a nested value of {@link #var}, giving rise to a local value if not considered 
     * invalid (<b>null</b> or empty).
     * 
     * @param name the name of the nested variable of {@link #var}
     * @param value the local value
     * @return the value
     */
    protected String getStringValue(String name, String value) {
        String result;
        if (null == value || value.length() == 0) {
            result = IvmlUtils.getStringValue(IvmlUtils.getNestedSafe(var, name), value);
        } else {
            result = value;
        }
        return result;
    }

    /**
     * Returns an int value from a nested value of {@link #var}, giving rise to a local value if not considered invalid.
     * 
     * @param name the name of the nested variable of {@link #var}
     * @param value the local value
     * @param invalid when to consider {@code value} as invalid
     * @return the value
     */
    protected int getIntValue(String name, int value, int invalid) {
        int result;
        if (value == invalid) {
            result = IvmlUtils.getIntValue(IvmlUtils.getNestedSafe(var, name), value);
        } else {
            result = value;
        }
        return result;
    }

}
