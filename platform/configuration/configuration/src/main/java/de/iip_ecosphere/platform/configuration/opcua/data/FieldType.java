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
 * Represents an OPC UA field type.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public abstract class FieldType extends BaseType {

    private String dataType;

    // checkstyle: stop parameter number check
    
    /**
     * Creates an OPC UA field type instance.
     * 
     * @param nodeId      the node id
     * @param browseName  the browse name
     * @param displayName the display name
     * @param description the description
     * @param dataType    the type of the field
     * @param optional    the optional status
     */
    public FieldType(String nodeId, String browseName, String displayName, String description, String dataType, 
        boolean optional) {
        super(nodeId, browseName, displayName, description, optional);
        this.dataType = dataType;
    }
    
    // checkstyle: resume parameter number check

    /**
     * Returns the type of the field.
     * 
     * @return the type
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Changes the type of the field.
     * 
     * @param dataType the new type
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UAFieldType {\n");
        builder.append("\t\t\t\tname = \"" + validateVarName(getDisplayname()) + "\",\n");
        builder.append("\t\t\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\t\t\tnodeClass = NodeClass::UAObject,\n");
        builder.append("\t\t\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\t\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\t\t\tdescription = \"" + getDescription() + "\",\n");
        builder.append("\t\t\t\ttype = refBy(" + dataType + ")\n");
        return builder.toString();
    }

}
