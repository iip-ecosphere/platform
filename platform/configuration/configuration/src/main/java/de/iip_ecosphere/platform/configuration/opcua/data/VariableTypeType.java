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
 * Represents a variable meta-type.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
public class VariableTypeType extends BaseType {

    private String documentation;
    private String dataType;

    // checkstyle: stop parameter number check
    
    /**
     * Creates an instance.
     * 
     * @param nodeId the node id
     * @param browseName the browse name
     * @param displayName the display name
     * @param description the description
     * @param documentation the documentation
     * @param dataType the data type
     */
    public VariableTypeType(String nodeId, String browseName, String displayName, String description,
        String documentation, String dataType) {
        super(nodeId, browseName, displayName, description);
        this.documentation = documentation;
        this.dataType = dataType;
    }
    
    // checkstyle: resume parameter number check

    /**
     * Returns the documentation.
     * 
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Returns the data type.
     * 
     * @return the data type
     */
    public String getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tUAVariableTypeType " + getVarName() + " = {\n");
        builder.append("\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\tnodeClass = NodeClass::UAVariableType,\n");
        builder.append("\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\tdocumentation = \"" + documentation + "\"");
        if (!dataType.equals("")) {
            if (!dataType.equals("opcType")) {
                builder.append(",\n\t\ttype = refBy(" + dataType + ")");
            } else {
                builder.append(",\n\t\ttype = refBy(opcUnknownDataType)");
            }
        }
        builder.append("\n\t};\n\n");
        return builder.toString();
    }

}
