/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.environment.metricsProvider;

import static org.junit.Assert.assertEquals;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.CapacityBaseUnit;

/**
 * Tests {@link CapacityBaseUnit}.
 * 
 * @author Miguel Gomez
 */
public class CapacityBaseUnitTest {

    // Values of the different base units
    private static final double ONE_BYTE = 1.0;
    private static final double ONE_KILO = ONE_BYTE * 1024.0;
    private static final double ONE_MEGA = ONE_KILO * 1024.0;
    private static final double ONE_GIGA = ONE_MEGA * 1024.0;
    private static final double ONE_TERA = ONE_GIGA * 1024.0;

    private static final String SUFFIX_BYTES = "bytes";
    private static final String SUFFIX_KILOS = "kilobytes";
    private static final String SUFFIX_MEGAS = "megabytes";
    private static final String SUFFIX_GIGAS = "gigabytes";
    private static final String SUFFIX_TERAS = "terabytes";
    private static final String RANDOM = "random";

    /**
     * Tests {@link CapacityBaseUnit#BYTES}.
     */
    @Test
    public void testByteValueOfBytes() {
        assertEquals(ONE_BYTE, CapacityBaseUnit.BYTES.byteValue(), 0.0);
    }

    /**
     * Tests {@link CapacityBaseUnit#KILOBYTES}.
     */
    @Test
    public void testByteValueOfKilobytes() {
        assertEquals(ONE_KILO, CapacityBaseUnit.KILOBYTES.byteValue(), 0.0);
    }

    /**
     * Tests {@link CapacityBaseUnit#MEGABYTES}.
     */
    @Test
    public void testByteValueOfMegabytes() {
        assertEquals(ONE_MEGA, CapacityBaseUnit.MEGABYTES.byteValue(), 0.0);
    }

    /**
     * Tests {@link CapacityBaseUnit#GIGABYTES}.
     */
    @Test
    public void testByteValueOfGigabytes() {
        assertEquals(ONE_GIGA, CapacityBaseUnit.GIGABYTES.byteValue(), 0.0);
    }

    /**
     * Tests {@link CapacityBaseUnit#TERABYTES}.
     */
    @Test
    public void testByteValueOfTerabytes() {
        assertEquals(ONE_TERA, CapacityBaseUnit.TERABYTES.byteValue(), 0.0);
    }

    /**
     * Tests {@link CapacityBaseUnit#BYTES}.
     */
    @Test
    public void testStringValueOfBytes() {
        assertEquals(SUFFIX_BYTES, CapacityBaseUnit.BYTES.stringValue());
    }

    /**
     * Tests {@link CapacityBaseUnit#KILOBYTES}.
     */
    @Test
    public void testStringValueOfKilobytes() {
        assertEquals(SUFFIX_KILOS, CapacityBaseUnit.KILOBYTES.stringValue());
    }

    /**
     * Tests {@link CapacityBaseUnit#MEGABYTES}.
     */
    @Test
    public void testStringValueOfMegabytes() {
        assertEquals(SUFFIX_MEGAS, CapacityBaseUnit.MEGABYTES.stringValue());
    }

    /**
     * Tests {@link CapacityBaseUnit#GIGABYTES}.
     */
    @Test
    public void testStringValueOfGigabytes() {
        assertEquals(SUFFIX_GIGAS, CapacityBaseUnit.GIGABYTES.stringValue());
    }

    /**
     * Tests {@link CapacityBaseUnit#TERABYTES}.
     */
    @Test
    public void testStringValueOfTerabytes() {
        assertEquals(SUFFIX_TERAS, CapacityBaseUnit.TERABYTES.stringValue());
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfBytes() {
        assertEquals(CapacityBaseUnit.BYTES, CapacityBaseUnit.valueOf(SUFFIX_BYTES.toUpperCase()));
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfKilobytes() {
        assertEquals(CapacityBaseUnit.KILOBYTES, CapacityBaseUnit.valueOf(SUFFIX_KILOS.toUpperCase()));
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfMegabytes() {
        assertEquals(CapacityBaseUnit.MEGABYTES, CapacityBaseUnit.valueOf(SUFFIX_MEGAS.toUpperCase()));
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfGigabytes() {
        assertEquals(CapacityBaseUnit.GIGABYTES, CapacityBaseUnit.valueOf(SUFFIX_GIGAS.toUpperCase()));
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfterabytes() {
        assertEquals(CapacityBaseUnit.TERABYTES, CapacityBaseUnit.valueOf(SUFFIX_TERAS.toUpperCase()));
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfNull() {
        assertThrows(NullPointerException.class, () -> CapacityBaseUnit.valueOf(null));
    }

    /**
     * Tests {@link CapacityBaseUnit#valueOf(String)}.
     */
    @Test
    public void testValueOfRandomString() {
        assertThrows(IllegalArgumentException.class, () -> CapacityBaseUnit.valueOf(RANDOM.toUpperCase()));
    }

}
