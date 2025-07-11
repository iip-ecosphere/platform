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

package test.de.iip_ecosphere.platform.support.aas.basyx2;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.basyx2.Tools;

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
    /*private static int advanceIgnored(int pos, String name) {
        while (pos < name.length() && '_' == name.charAt(pos)) {
            pos++;
        }
        return pos;
    }*/
    
    /**
     * Tests for normalized name equality, i.e., lower case and '_' ignored.
     * 
     * @param name1 the first name to be checked
     * @param name2 the second name to be checked
     * @return {@code true} for equal, {@code false} else
     */
    /*private static boolean equalsNormalized(String name1, String name2) {
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
    }*/
        
    /**
     * Asserts whether two enum constants are considered to be qqual.
     * 
     * @param t1 the first constant
     * @param t2 the second constant
     */
    /*private static void assertEquals(Enum<?> t1, Enum<?> t2)  {
        Assert.assertTrue(t1 + " " + t2, equalsNormalized(t1.name(), t2.name()));
    }*/

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
        /*try {
            Tools.checkId("value");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            Tools.checkId("invocationList");
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }*/
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
     * Tests the translate identifier function.
     */
    @Test
    public void testTranslateIdentifier() {
        // TODO
        /*assertIIdentifier("aas", IdentifierType.CUSTOM, Tools.translateIdentifier(null, "aas"));
        assertIIdentifier("aas", IdentifierType.CUSTOM, Tools.translateIdentifier("", "aas"));
        assertIIdentifier("xyz", IdentifierType.CUSTOM, Tools.translateIdentifier("xyz", "aas"));
        assertIIdentifier("urn:::AAS:::testMachines#", IdentifierType.IRI, 
            Tools.translateIdentifier("urn:::AAS:::testMachines#", "aas"));
        assertIIdentifier("TEST", IdentifierType.IRI, 
            Tools.translateIdentifier("urnText:TEST", "aas"));*/
    }
    

    /**
     * Asserts properties of an identifier.
     * 
     * @param expectedId the expected ID
     * @param expectedType the expected type
     * @param id the id to test
     */
    /*private static void assertIIdentifier(String expectedId, IdentifierType expectedType, IIdentifier id) {
        Assert.assertNotNull(id);
        Assert.assertEquals(expectedType, id.getIdType());
        Assert.assertEquals(expectedId, id.getId());
    }*/
    
    /**
     * Tests {@link Tools#translateReference(String)}.
     */
    @Test
    public void testTranslateReference() {
        Assert.assertNull(Tools.translateReference((String) null));
        Assert.assertNull(Tools.translateReference(""));
        Assert.assertNull(Tools.translateReference("aas"));
        final String irdi = "0173-1#02-AAV232#002";
        org.eclipse.digitaltwin.aas4j.v3.model.Reference ref = Tools.translateReference("irdi:" + irdi);
        Assert.assertNotNull(ref);
        Assert.assertTrue(ref.getKeys().size() > 0);
        // TODO 
        /*IKey key = ref.getKeys().get(0);
        Assert.assertNotNull(key);
        Assert.assertEquals(KeyElements.CONCEPTDESCRIPTION, key.getType());
        Assert.assertEquals(false, key.isLocal());
        Assert.assertEquals(irdi, key.getValue());
        Assert.assertEquals(KeyType.IRDI, key.getIdType());*/
        
        Assert.assertNull(Tools.translateReference(null, false));
        String tmp = Tools.translateReference(ref, false);
        Assert.assertNotNull(tmp);
        Assert.assertEquals("irdi:" + irdi, tmp);
        tmp = Tools.translateReference(ref, true);
        Assert.assertNotNull(tmp);
        Assert.assertEquals(irdi, tmp);
        
        final String iri = "https://admin-shell.io/ZVEI/TechnicalData/GeneralInformation/1/1";
        ref = Tools.translateReference("iri:" + iri);
        Assert.assertNotNull(ref);
        Assert.assertTrue(ref.getKeys().size() > 0);
        // TODO
        /*
        key = ref.getKeys().get(0);
        Assert.assertNotNull(key);
        Assert.assertEquals(KeyElements.CONCEPTDESCRIPTION, key.getType());
        Assert.assertEquals(false, key.isLocal());
        Assert.assertEquals(iri, key.getValue());
        Assert.assertEquals(KeyType.IRI, key.getIdType());*/

        tmp = Tools.translateReference(ref, false);
        Assert.assertNotNull(tmp);
        Assert.assertEquals("iri:" + iri, tmp);
        
        tmp = Tools.translateReference(ref, true);
        Assert.assertNotNull(tmp);
        Assert.assertEquals(iri, tmp);
    }
    
    /**
     * Tests translating calendar data forth and back.
     */
    @Test
    public void testTranslateDataCalendar() {
        Calendar now = Calendar.getInstance();
        String tmpBaSyx = Tools.translateValueToBaSyx(DataTypeDefXsd.DATE_TIME, now);
        Object tmp = Tools.translateValueFromBaSyx(tmpBaSyx, DataTypeDefXsd.DATE_TIME);
        Assert.assertTrue(tmp instanceof Calendar);
        Assert.assertEquals(now.getTimeInMillis(), ((Calendar) tmp).getTimeInMillis());
    }

    /**
     * Tests translating date data forth and back.
     */
    @Test
    public void testTranslateDataCalendarDate() {
        Date now = Calendar.getInstance().getTime();
        String tmpBaSyx = Tools.translateValueToBaSyx(DataTypeDefXsd.DATE_TIME, now);
        Object tmp = Tools.translateValueFromBaSyx(tmpBaSyx, DataTypeDefXsd.DATE_TIME);
        Assert.assertTrue(tmp instanceof Calendar);
        Assert.assertEquals(now.getTime(), ((Calendar) tmp).getTimeInMillis());
    }

}
