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
 * Represents an OPC UA literal.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public abstract class Literal {

    private String name;
    private String description;

    /**
     * Creates a literal instance.
     * 
     * @param name        the name of the literal
     * @param description the description
     */
    public Literal(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of the literal.
     * 
     * @return the literal
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of the literal description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Formats the OPC UA literal in IVML.
     * 
     * @return the IVML representation of the OPC UA literal
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Literal {\n");
        builder.append("\t\t\t\tname = \"" + name + "\",\n");
        builder.append("\t\t\t\tdescription = \"" + description + "\"\n");
        return builder.toString();
    }

}
