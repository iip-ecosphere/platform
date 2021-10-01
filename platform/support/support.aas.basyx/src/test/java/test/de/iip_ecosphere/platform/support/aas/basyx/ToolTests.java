/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.Tools;

/**
 * Tests {@link Tools}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ToolTests {

    /**
     * Returns an advanced {@code pos} if characters to be ignored are at {@code pos}.
     * 
     * @param pos the current pos in {@code name}
     * @param name the name to check the characters for advancing for
     * @return {@code pos} or advanced {@code pos}
     */
    private static int advanceIgnored(int pos, String name) {
        while (pos < name.length() && '_' == name.charAt(pos)) {
            pos++;
        }
        return pos;
    }
    
    /**
     * Tests for normalized name equality, i.e., lower case and '_' ignored.
     * 
     * @param name1 the first name to be checked
     * @param name2 the second name to be checked
     * @return {@code true} for equal, {@code false} else
     */
    private static boolean equalsNormalized(String name1, String name2) {
        boolean equals = true;
        int pos1 = 0;
        int pos2 = 0;
        
        pos1 = advanceIgnored(pos1, name1);
        pos2 = advanceIgnored(pos2, name2);
        while (equals && pos1 < name1.length() && pos2 < name2.length()) {
            if (pos1 < name1.length() && pos2 < name2.length()) {
                equals = Character.toLowerCase(name1.charAt(pos1)) == Character.toLowerCase(name2.charAt(pos2));
                pos1++;
                pos2++;
            }
            pos1 = advanceIgnored(pos1, name1);
            pos2 = advanceIgnored(pos2, name2);
        }
        return pos1 == name1.length() && pos2 == name2.length();
    }
        
    /**
     * Asserts whether two enum constants are considered to be qqual.
     * 
     * @param t1 the first constant
     * @param t2 the second constant
     */
    private static void assertEquals(Enum<?> t1, Enum<?> t2)  {
        Assert.assertTrue(t1 + " " + t2, equalsNormalized(t1.name(), t2.name()));
    }
    
    /**
     * Tests the property types.
     */
    @Test
    public void testPropertyType() {
        for (Type t : Type.values()) {
            assertEquals(t, Tools.translate(t));
        }
        for (ValueType t : ValueType.values()) {
            assertEquals(t, Tools.translate(t));
        }
    }
    
    /**
     * Tests the asset kinds.
     */
    @Test
    public void testAssetKind() {
        for (AssetKind k : AssetKind.values()) {
            assertEquals(k, Tools.translate(k));
        }
        for (de.iip_ecosphere.platform.support.aas.AssetKind k 
            : de.iip_ecosphere.platform.support.aas.AssetKind.values()) {
            assertEquals(k, Tools.translate(k));
        }
    }

    /**
     * Tests short id checks.
     */
    @Test
    public void testCheckId() {
        Tools.checkId("id");
        try {
            Tools.checkId(null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkId("");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkId("012ds_");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkId("java.lang.String");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkId("value");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkId("invocationList");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests URN checks.
     */
    @Test
    public void testCheckUrn() {
        Tools.checkUrn("urn");
        try {
            Tools.checkUrn(null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkUrn("");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
    }
    
    /**
     * Tests the get option function.
     */
    @Test
    public void testGetOption() {
        String[] opts1 = new String[] {"a", "b", RegistryBackend.INMEMORY.name()};
        String[] opts2 = new String[] {"a", "b"};
        Assert.assertEquals(RegistryBackend.INMEMORY, 
            Tools.getOption(opts1, RegistryBackend.MONGODB, RegistryBackend.class));
        Assert.assertEquals(RegistryBackend.MONGODB, 
            Tools.getOption(opts2, RegistryBackend.MONGODB, RegistryBackend.class));
    }
    
    /**
     * Tests the translate identifier function.
     */
    @Test
    public void testTranslateIdentifier() {
        assertIIdentifier("aas", IdentifierType.CUSTOM, Tools.translateIdentifier(null, "aas"));
        assertIIdentifier("aas", IdentifierType.CUSTOM, Tools.translateIdentifier("", "aas"));
        assertIIdentifier("xyz", IdentifierType.CUSTOM, Tools.translateIdentifier("xyz", "aas"));
        assertIIdentifier("urn:::AAS:::testMachines#", IdentifierType.IRI, 
            Tools.translateIdentifier("urn:::AAS:::testMachines#", "aas"));
        assertIIdentifier("TEST", IdentifierType.IRI, 
            Tools.translateIdentifier("urnText:TEST", "aas"));
    }
    
    /**
     * Tests {@link Tools#translateReference(String)}.
     */
    @Test
    public void testTranslateReference() {
        Assert.assertNull(Tools.translateReference(null));
        Assert.assertNull(Tools.translateReference(""));
        Assert.assertNull(Tools.translateReference("aas"));
        final String irdi = "0173-1#02-AAV232#002";
        IReference ref = Tools.translateReference("irdi:" + irdi);
        Assert.assertNotNull(ref);
        Assert.assertTrue(ref.getKeys().size() > 0);
        IKey key = ref.getKeys().get(0);
        Assert.assertNotNull(key);
        Assert.assertEquals(KeyElements.PROPERTY, key.getType());
        Assert.assertEquals(false, key.isLocal());
        Assert.assertEquals(irdi, key.getValue());
        Assert.assertEquals(KeyType.IRDI, key.getIdType());
    }

    /**
     * Asserts properties of an identifier.
     * 
     * @param expectedId the expected ID
     * @param expectedType the expected type
     * @param id the id to test
     */
    private static void assertIIdentifier(String expectedId, IdentifierType expectedType, IIdentifier id) {
        Assert.assertNotNull(id);
        Assert.assertEquals(expectedType, id.getIdType());
        Assert.assertEquals(expectedId, id.getId());
    }

}
