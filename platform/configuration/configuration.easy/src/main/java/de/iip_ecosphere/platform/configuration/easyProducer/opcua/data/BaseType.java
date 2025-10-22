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

package de.iip_ecosphere.platform.configuration.easyProducer.opcua.data;

/**
 * Representation of an OPC UA type.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public abstract class BaseType {

    private String varName;
    private String nodeId;
    private String browseName;
    private String displayName;
    private String description;
    private boolean optional;

    /**
     * Creates an OPC UA type representation.
     * 
     * @param nodeId      the node id
     * @param browseName  the browse name
     * @param displayname the display name
     * @param description the description
     * @param optional    whether the type is optional
     */
    public BaseType(String nodeId, String browseName, String displayname, String description, boolean optional) {
        this.nodeId = nodeId;
        this.browseName = browseName;
        this.displayName = displayname;
        this.description = description;
        this.optional = optional;
    }

    /**
     * Returns the (IVML) variable name.
     * 
     * @return the variable name
     */
    public String getVarName() {
        return varName;
    }

    /**
     * Defines the (IVML) variable name.
     * 
     * @param varName the variable name
     */
    public void setVarName(String varName) {
        this.varName = validateVarName(varName);
    }

    /**
     * Returns the OPC UA node id.
     * 
     * @return the node id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Returns the OPC UA browse name.
     * 
     * @return the browse name
     */
    public String getBrowseName() {
        return browseName;
    }

    /**
     * Returns the OPC UA display name.
     * 
     * @return the display name
     */
    public String getDisplayname() {
        return displayName;
    }

    /**
     * Returns the OPC UA node description.
     * 
     * @return the node description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the OPC UA optional status.
     * 
     * @return the optional status
     */
    public boolean isOptional() {
        return optional;
    }
    
    /**
     * Formats the respective OPC UA type in IVML.
     * 
     * @return the IVML representation of the OPC UA type
     */
    @Override
    public String toString() {
        return "BaseType [nodeId=" + nodeId + ", browseName=" + browseName + ", displayName=" + displayName
                + ", description=" + description + "]";
    }

    /**
     * Formats a node id in IVML.
     * 
     * @param nodeId the node id
     * @return the IVML representation of a node id
     */
    public static String formatNodeId(String nodeId) {
        if (nodeId.contains("ns=")) {
            String nameSpaceIndex = nodeId.substring(3, nodeId.indexOf(';'));
            String identifier = nodeId.substring(nodeId.indexOf(';') + 3, nodeId.length());
            nodeId = "nodeId = {nameSpaceIndex = " + nameSpaceIndex + ", identifier = " + identifier + "},";
        } else {
            String identifier = nodeId.replace("i=", "");
            nodeId = "nodeId = {nameSpaceIndex = 0, identifier = " + identifier + "},";
        }
        return nodeId;
    }

    /**
     * Validates and fixes a variable name.
     * 
     * @param varName the variable name
     * @return the (potentially modified) variable name
     */
    public static String validateVarName(String varName) {
        varName = varName.replaceAll("[^A-Za-z0-9]", "");
        varName = varName.replace("_", "");
        if (Character.isDigit(varName.charAt(0))) {
            varName = "_" + varName;
        }
        return varName;
    }

}