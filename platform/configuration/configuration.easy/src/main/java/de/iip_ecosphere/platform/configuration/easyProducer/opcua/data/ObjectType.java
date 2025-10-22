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

import java.util.ArrayList;

/**
 * Represents an OPC UA object type (declaration).
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class ObjectType extends BaseType {

    private String type;
    private ArrayList<FieldType> fields;

    // checkstyle: stop parameter number check
    
    /**
     * Creates an OPC UA object type representation/declaration.
     * 
     * @param nodeId      the node id
     * @param browseName  the browse name
     * @param displayName the display name
     * @param description the description
     * @param optional    whether the type is optional
     * @param type        the type of the object
     * @param fields      the fields the object is constituted from
     */
    public ObjectType(String nodeId, String browseName, String displayName, String description, boolean optional, 
        String type, ArrayList<FieldType> fields) {
        super(nodeId, browseName, displayName, description, optional);
        this.type = type;
        this.fields = fields;
    }
    
    // checkstyle: resume parameter number check

    /**
     * Returns the type of the object.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the fields making up the type.
     * 
     * @return the fields
     */
    public ArrayList<FieldType> getFields() {
        return fields;
    }

    /**
     * Changes the fields making up the type.
     * 
     * @param fields the new fields
     */
    public void setFields(ArrayList<FieldType> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tUAObjectType " + getVarName() + " = {\n");
        builder.append("\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\tnodeClass = NodeClass::UAObject,\n");
        builder.append("\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\tdescription = \"" + getDescription() + "\",\n");
        builder.append("\t\toptional = " + isOptional() + ",\n");
        builder.append("\t\ttypeDefinition = refBy(opc" + type + "),\n");
        if (!fields.isEmpty()) {
            builder.append("\t\tfields = {\n\t\t\t");
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
            builder.append("\t\tfields = {\n\t}\n");
        }
        builder.append("\t};\n\n");
        return builder.toString();
    }

}
