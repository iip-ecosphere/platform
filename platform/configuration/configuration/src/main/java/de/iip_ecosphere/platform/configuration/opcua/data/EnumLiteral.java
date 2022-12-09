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
 * @author Jan-Hendrick Cepok, SSE
 */
public class EnumLiteral {

    private String name;
    private String ordinal;
    private String description;

    /**
     * Creates an enumeration literal instance.
     * 
     * @param name the name of the literal
     * @param ordinal the ordinal value
     * @param description the description of the literal
     */
    public EnumLiteral(String name, String ordinal, String description) {
        this.name = name;
        this.ordinal = ordinal;
        this.description = description;
    }

    /**
     * Returns the name of the literal.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ordinal of the literal.
     * 
     * @return the ordinal
     */
    public String getOrdinal() {
        return ordinal;
    }

    /**
     * Returns the description of the literal.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnumLiteral {\n");
        builder.append("\t\t\t\tname = \"" + name + "\",\n");
        builder.append("\t\t\t\tordinal = " + ordinal + ",\n");
        builder.append("\t\t\t\tdescription = \"" + description + "\"\n");
        return builder.toString();
        // return "EnumLiteral [name=" + name + ", ordinal=" + ordinal + ",
        // description=" + description + "]\n";
    }

}
