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
 * @author Jan-Hendrik Cepok, SSE
 */
public class DataLiteral extends Literal {

    private String dataType;

    /**
     * Creates a data literal instance.
     * 
     * @param name        the name of the literal
     * @param dataType    the data type
     * @param description the description
     */
    public DataLiteral(String name, String dataType, String description) {
        super(name, description);
        this.dataType = dataType;
    }

    /**
     * Returns the name of the data type.
     * 
     * @return the data type
     */
    public String getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataLiteral {\n");
        builder.append("\t\t\t\tname = \"" + getName() + "\",\n");
        builder.append("\t\t\t\ttype = refBy(" + dataType + "),\n");
        builder.append("\t\t\t\tdescription = \"" + getDescription() + "\"\n");
        return builder.toString();
    }

}