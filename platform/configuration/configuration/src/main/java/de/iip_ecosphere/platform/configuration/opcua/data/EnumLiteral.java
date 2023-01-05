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

package de.iip_ecosphere.platform.configuration.opcua.data;

/**
 * Represents an OPC UA enumeration literal.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class EnumLiteral extends Literal {

    private String ordinal;

    /**
     * Creates an enumeration literal instance.
     * 
     * @param name        the name of the literal
     * @param ordinal     the ordinal value
     * @param description the description of the literal
     */
    public EnumLiteral(String name, String ordinal, String description) {
        super(name, description);
        this.ordinal = ordinal;
    }

    /**
     * Returns the ordinal.
     * 
     * @return the ordinal
     */
    public String getOrdinal() {
        return ordinal;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnumLiteral {\n");
        builder.append("\t\t\t\tname = \"" + getName() + "\",\n");
        builder.append("\t\t\t\tordinal = " + ordinal + ",\n");
        builder.append("\t\t\t\tdescription = \"" + getDescription() + "\"\n");
        return builder.toString();
    }

}
