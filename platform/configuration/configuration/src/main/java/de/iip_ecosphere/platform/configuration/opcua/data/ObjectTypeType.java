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
 * Represents an object meta type.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
public class ObjectTypeType extends BaseType {

    private String documentation;
    // private ArrayList<ObjectType> objects;

    /**
     * Creates an instance.
     * 
     * @param nodeId the node id
     * @param browseName the browse name
     * @param displayName the display name
     * @param description the description
     * @param documentation the documentation
     */
    public ObjectTypeType(String nodeId, String browseName, String displayName, String description,
        String documentation) {
        super(nodeId, browseName, displayName, description);
        this.documentation = documentation;
        // this.objects = objects;
    }

    /**
     * Returns the documentation.
     * 
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tUAObjectTypeType " + getVarName() + " = {\n");
        builder.append("\t\tname = \"" + getVarName() + "\",\n");
        builder.append("\t\t" + formatNodeId(getNodeId()) + "\n");
        builder.append("\t\tnodeClass = NodeClass::UAObjectType,\n");
        builder.append("\t\tbrowseName = \"" + getBrowseName() + "\",\n");
        builder.append("\t\tdisplayName = \"" + getDisplayname() + "\",\n");
        builder.append("\t\tdescription = \"" + getDescription() + "\",\n");
        builder.append("\t\tdocumentation = \"" + documentation + "\"\n");
        /*
         * if(!objects.isEmpty()) { builder.append("\tfields = {\n\t\t"); for(ObjectType
         * o : objects) { builder.append("name = " + o.getVarName());
         * if(o.equals(objects.get(objects.size() - 1))) { builder.append("\t\t}\n"); }
         * else { builder.append("\t\t}, "); } } builder.append("\t}\n"); } else {
         * builder.append("\tfields = {\n\t\n}"); }
         */
        builder.append("\t};\n\n");
        // return "UAObjectType [nodeId=" + getNodeId() + ", browseName=" +
        // getBrowseName() + ", displayName=" + getDisplayname()
        // + ", description=" + getDescription() + ", \nfields=\n" + fields + "\n]";
        return builder.toString();
    }

}