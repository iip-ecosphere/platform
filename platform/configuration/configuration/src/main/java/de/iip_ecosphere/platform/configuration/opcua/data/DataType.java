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

import java.util.ArrayList;

/**
 * Represents an OPC UA data type.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
public class DataType extends BaseType {

    private String documentation;
    private ArrayList<DataLiteral> literals;

    // checkstyle: stop parameter number check
    
    /**
     * Creates an OPC UA data type instance.
     * 
     * @param nodeId the node id
     * @param browseName the browse name
     * @param displayName the display name
     * @param description the description
     * @param documentation the documentation of the type
     * @param literals the literals of this data type
     */
    public DataType(String nodeId, String browseName, String displayName, String description, String documentation,
        ArrayList<DataLiteral> literals) {
        super(nodeId, browseName, displayName, description);
        this.documentation = documentation;
        this.literals = literals;
    }

    // checkstyle: resume parameter number check

    /**
     * Returns the documentation of this data type.
     * 
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Returns the literals of this data type.
     * 
     * @return the literals
     */
    public ArrayList<DataLiteral> getLiterals() {
        return literals;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tUADataType " + getVarName() + " = {\n");
        builder.append("\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\tnodeClass = NodeClass::UADataType,\n");
        builder.append("\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\tdescription = \"" + getDescription() + "\",\n");
        builder.append("\t\tdocumentation = \"" + getDocumentation() + "\"");
        builder.append(",\n\t\tliterals = {\n");
        if (!literals.isEmpty()) {
            builder.append("\t\t\t");
            for (DataLiteral l : literals) {
                builder.append(l.toString());
                if (l.equals(literals.get(literals.size() - 1))) {
                    builder.append("\t\t\t}\n");
                } else {
                    builder.append("\t\t\t}, ");
                }
            }
        }
        builder.append("\t\t}\n");
        builder.append("\t};\n\n");
        return builder.toString();
        // return "EnumType [nodeId=" + getNodeId() + ", browseName=" + getBrowseName()
        // + ", displayName=" + getDisplayname()
        // + ", description=" + getDescription() + ", \nliterals=\n" + literals + "\n]";
    }

}