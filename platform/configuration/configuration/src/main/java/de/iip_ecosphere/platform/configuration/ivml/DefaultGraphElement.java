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
 * {@code name}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class DefaultGraphElement implements IvmlGraphElement {

    public static final int INVALID_POSITION = -1;
    public static final int INVALID_SIZE = -1;
    private IDecisionVariable var;
    
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
        return IvmlUtils.getStringValue(var.getNestedElement("name"), "");
    }

    @Override
    public void setName(String name) {
        // TODO
    }

    @Override
    public IDecisionVariable getVariable() {
        return var;
    }

}
