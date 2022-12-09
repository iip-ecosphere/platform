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

package test.de.iip_ecosphere.platform.configuration.opcua;

import java.util.ArrayList;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.opcua.data.EnumLiteral;
import de.iip_ecosphere.platform.configuration.opcua.data.EnumType;
import de.iip_ecosphere.platform.configuration.opcua.data.FieldType;
import de.iip_ecosphere.platform.configuration.opcua.data.ObjectType;
import de.iip_ecosphere.platform.configuration.opcua.data.VariableType;

/**
 * Tests the {@link ObjectType}.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
public class ObjectTypeTest {

    /**
     * Tests the {@link ObjectType} by output.
     */
    @Test
    public void testObjectType() {
        ArrayList<FieldType> fields = new ArrayList<FieldType>();
        FieldType field = new FieldType("ns=1;i=5019", "1:TestField", "TestField", "This is a test field.",
            "SpecificEnumerationType");
        VariableType variable = new VariableType("ns=1;i=5020", "1:TestVariable", "TestVariable",
            "This is a test variable.", "TestDataType", "TestVarType", false, "1", "1", "1");
        fields.add(field);
        fields.add(field);
        fields.add(variable);
        ObjectType object = new ObjectType("ns=1;i=5018", "1:TestObject", "TestObject", "This is a test object.",
            "TestObjectType", fields);
        object.setVarName("opc" + object.getDisplayname());
        ArrayList<EnumLiteral> literals = new ArrayList<EnumLiteral>();
        EnumLiteral literal0 = new EnumLiteral("TestLiteral", "0", "This is a test literal.");
        EnumLiteral literal1 = new EnumLiteral("TestLiteral", "1", "This is a test literal.");
        EnumLiteral literal2 = new EnumLiteral("TestLiteral", "2", "This is a test literal.");
        literals.add(literal0);
        literals.add(literal1);
        literals.add(literal2);
        EnumType enumeration = new EnumType("ns=1;i=5022", "1:TestEnum", "TestEnum", "This is a test enumeration.",
            "TestDocumentation", literals);
        object.setVarName("opc" + field.getDataType());
        System.out.print(object.toString());
        System.out.print(enumeration.toString());
    }
    
}
