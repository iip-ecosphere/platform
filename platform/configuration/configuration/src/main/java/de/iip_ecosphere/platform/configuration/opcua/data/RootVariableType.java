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
 * Represents an OPC UA root variable.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class RootVariableType extends BaseType {

    private String dataType;
    private String variableType;
    private String level;
    private String rank;
    private String dimensions;
    private String rootParent;
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates an instance.
     * 
     * @param nodeId       the node id
     * @param browseName   the browse name
     * @param displayName  the display name
     * @param description  the description
     * @param dataType     the data type
     * @param variableType the variable type
     * @param optional     whether the type is optional
     * @param level        the level
     * @param rank         the rank
     * @param dimensions   the dimensions of the variable
     * @param rootParent   the root parent
     */
    public RootVariableType(String nodeId, String browseName, String displayName, String description, String dataType,
        String variableType, boolean optional, String level, String rank, String dimensions, String rootParent) {
        super(nodeId, browseName, displayName, description, optional);
        this.dataType = dataType;
        this.variableType = variableType;
        this.level = level;
        this.rank = rank;
        this.dimensions = dimensions;
        this.rootParent = rootParent;
    }
    
    // checkstyle: resume parameter number check

    /**
     * Returns the data type.
     *
     * @return the data type
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * Returns the variable type.
     *
     * @return the variable type
     */
    public String getVariableType() {
        return variableType;
    }

    /**
     * Returns the level.
     *
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Returns the rank.
     *
     * @return the rank
     */
    public String getRank() {
        return rank;
    }

    /**
     * Returns the dimensions.
     *
     * @return the dimensions
     */
    public String getDimensions() {
        return dimensions;
    }
    
    /**
     * Returns the root parent.
     * 
     * @return the root parent
     */
    public String getRootParent() {
        return rootParent;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tUARootVariableType " + getVarName() + " = {\n");
        builder.append("\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\tnodeClass = NodeClass::UAVariable,\n");
        builder.append("\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\tdescription = \"" + getDescription() + "\",\n");
        builder.append("\t\toptional = " + isOptional());
        if (!getDataType().equals("")) {
            if (!getDataType().equals("opcType")) {
                builder.append(",\n\t\ttype = refBy(" + getDataType() + ")");
            } else {
                builder.append(",\n\t\ttype = refBy(opcUnknownDataType)");
            }
        }
        builder.append(",\n\t\ttypeDefinition = refBy(" + getVariableType() + ")");
        builder.append(",\n\t\trootParent = refBy(" + rootParent + ")");
        if (!level.equals("")) {
            builder.append(",\n\t\taccessLevel = " + level);
        }
        if (!rank.equals("")) {
            builder.append(",\n\t\tvalueRank = " + rank);
        }
        if (!dimensions.equals("")) {
            builder.append(",\n\t\tarrayDimensions = " + dimensions);
        }
        builder.append("\n");
        builder.append("\t};\n\n");
        return builder.toString();
    }

}