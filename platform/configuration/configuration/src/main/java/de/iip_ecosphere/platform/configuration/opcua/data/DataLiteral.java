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
 * Represents an OPC UA data literal.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
public class DataLiteral {

    private String name;
    private String dataType;
    private String description;

    /**
     * Creates a data literal instance.
     * 
     * @param name the name of the literal
     * @param dataType the data type
     * @param description the description
     */
    public DataLiteral(String name, String dataType, String description) {
        this.name = name;
        this.dataType = dataType;
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
     * Returns the name of the data type.
     * 
     * @return the data type
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Returns the name of the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataLiteral {\n");
        builder.append("\t\t\t\tname = \"" + name + "\",\n");
        builder.append("\t\t\t\ttype = refBy(" + dataType + "),\n");
        builder.append("\t\t\t\tdescription = \"" + description + "\"\n");
        return builder.toString();
        // return "EnumLiteral [name=" + name + ", ordinal=" + ordinal + ",
        // description=" + description + "]\n";
    }

}