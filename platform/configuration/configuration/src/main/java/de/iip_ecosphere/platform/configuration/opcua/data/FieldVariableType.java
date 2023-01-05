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
 * Represents an OPC UA field variable type.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class FieldVariableType extends FieldType {

    private String variableType;
    private boolean optional;
    private String level;
    private String rank;
    private String dimensions;

    // checkstyle: stop parameter number check
    
    /**
     * Creates an OPC UA field variable type instance.
     * 
     * @param nodeId       the node id
     * @param browseName   the browse name
     * @param displayName  the display name
     * @param description  the description
     * @param dataType     the type of the field
     * @param variableType the variable type
     * @param optional     whether the type is optional
     * @param level        the level
     * @param rank         the rank
     * @param dimensions   the dimensions
     */
    public FieldVariableType(String nodeId, String browseName, String displayName, String description, String dataType,
        String variableType, boolean optional, String level, String rank, String dimensions) {
        super(nodeId, browseName, displayName, description, dataType, optional);
        this.variableType = variableType;
        this.optional = optional;
        this.level = level;
        this.rank = rank;
        this.dimensions = dimensions;
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
    
    // checkstyle: resume parameter number check

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UAFieldVariableType {\n");
        builder.append("\t\t\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\t\t\tnodeClass = NodeClass::UAVariable,\n");
        builder.append("\t\t\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\t\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\t\t\tdescription = \"" + getDescription() + "\"");
        if (!getDataType().equals("")) {
            if (!getDataType().equals("opcType")) {
                builder.append(",\n\t\t\t\ttype = refBy(" + getDataType() + ")");
            } else {
                builder.append(",\n\t\t\t\ttype = refBy(opcUnknownDataType)");
            }
        }
        builder.append(",\n\t\t\t\ttypeDefinition = refBy(" + variableType + ")");
        builder.append(",\n\t\t\t\toptional = " + optional);
        if (!level.equals("")) {
            builder.append(",\n\t\t\t\taccessLevel = " + level);
        }
        if (!rank.equals("")) {
            builder.append(",\n\t\t\t\tvalueRank = " + rank);
        }
        if (!dimensions.equals("")) {
            builder.append(",\n\t\t\t\tarrayDimensions = " + dimensions);
        }
        builder.append("\n");
        return builder.toString();
    }

}
