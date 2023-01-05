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
 * Represents an OPC UA root object.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class RootObjectType extends ObjectType {

    private String rootParent;

    // checkstyle: stop parameter number check
    
    /**
     * Creates an OPC UA root object type representation/declaration.
     * 
     * @param nodeId      the node id
     * @param browseName  the browse name
     * @param displayName the display name
     * @param description the description
     * @param optional    whether the type is optional
     * @param type        the type of the object
     * @param rootParent  the root parent
     * @param fields      the fields the object is constituted from
     */
    public RootObjectType(String nodeId, String browseName, String displayName, String description, boolean optional, 
        String type, String rootParent, ArrayList<FieldType> fields) {
        super(nodeId, browseName, displayName, description, optional, type, fields);
        this.rootParent = rootParent;
    }
    
    // checkstyle: resume parameter number check

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
        builder.append("\tUARootObjectType " + getVarName() + " = {\n");
        builder.append("\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\tnodeClass = NodeClass::UAObject,\n");
        builder.append("\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\tdescription = \"" + getDescription() + "\",\n");
        builder.append("\t\toptional = " + isOptional() + ",\n");
        builder.append("\t\ttypeDefinition = refBy(" + getType() + "),\n");
        builder.append("\t\trootParent = refBy(" + rootParent + "),\n");
        if (!getFields().isEmpty()) {
            builder.append("\t\tfields = {\n\t\t\t");
            ArrayList<FieldType> fields = getFields();
            for (FieldType f : fields) {
                builder.append(f.toString());
                if (f.equals(fields.get(fields.size() - 1))) {
                    builder.append("\t\t\t}\n");
                } else {
                    builder.append("\t\t\t}, ");
                }
            }
            builder.append("\t\t}\n");
        } else {
            builder.append("\t\tfields = {\n\t\t}\n");
        }
        builder.append("\t};\n\n");
        return builder.toString();
    }

}