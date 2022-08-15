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

package de.iip_ecosphere.platform.support.iip_aas;

import de.iip_ecosphere.platform.support.aas.IdentifierType;

/**
 * Pre-defined IRDI for unit/concept identification in format for the AAS abstraction based on {@link IdentifierType} 
 * and {@link Eclass}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Irdi {

    /**
     * Denotes a generic identifier {@link Eclass#IRDI_PROPERTY_IDENTIFIER}.
     */
    public static final String AAS_IRDI_PROPERTY_IDENTIFIER 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_PROPERTY_IDENTIFIER;
    
    /**
     * Denotes a software version {@link Eclass#IRDI_PROPERTY_SOFTWARE_VERSION}.
     */
    public static final String AAS_IRDI_PROPERTY_SOFTWARE_VERSION 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_PROPERTY_SOFTWARE_VERSION;

    /**
     * Denotes a software name {@link Eclass#IRDI_PROPERTY_SOFTWARE_NAME}.
     */
    public static final String AAS_IRDI_PROPERTY_SOFTWARE_NAME 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_PROPERTY_SOFTWARE_NAME;

    
    /**
     * Denotes a byte as a unit {@link Eclass#IRDI_UNIT_BYTE}.
     */
    public static final String AAS_IRDI_UNIT_BYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_BYTE;

    /**
     * Denotes the unit kilobyte on base 1000 {@link Eclass#IRDI_UNIT_KILOBYTE}.
     */
    public static final String AAS_IRDI_UNIT_KILOBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_KILOBYTE;

    /**
     * Denotes the unit megabyte on base 1000 {@link Eclass#IRDI_UNIT_MEGABYTE}.
     */
    public static final String AAS_IRDI_UNIT_MEGABYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MEGABYTE;

    /**
     * Denotes the unit gigabyte on base 1000 {@link Eclass#IRDI_UNIT_GIGABYTE}.
     */
    public static final String AAS_IRDI_UNIT_GIGABYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_GIGABYTE;

    /**
     * Denotes the unit terabyte on base 1000 {@link Eclass#IRDI_UNIT_TERABYTE}.
     */
    public static final String AAS_IRDI_UNIT_TERABYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_TERABYTE;

    
    /**
     * Denotes the unit megabyte per second {@link Eclass#IRDI_UNIT_MEGABYTE_PER_SECOND}.
     */
    public static final String AAS_IRDI_UNIT_MEGABYTE_PER_SECOND 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MEGABYTE_PER_SECOND;

    /**
     * Denotes the unit gigabyte per second {@link Eclass#IRDI_UNIT_GIGABYTE_PER_SECOND}.
     */
    public static final String AAS_IRDI_UNIT_GIGABYTE_PER_SECOND 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_GIGABYTE_PER_SECOND;

    
    /**
     * Denotes the unit kilobyte on base 1024 {@link Eclass#IRDI_UNIT_KIBIBYTE}.
     */
    public static final String AAS_IRDI_UNIT_KIBIBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_KIBIBYTE;

    /**
     * Denotes the unit megabyte on base 1024 {@link Eclass#IRDI_UNIT_MEBIBYTE}.
     */
    public static final String AAS_IRDI_UNIT_MEBIBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MEBIBYTE;

    /**
     * Denotes the unit gigabyte on base 1024 {@link Eclass#IRDI_UNIT_GIBIBYTE}.
     */
    public static final String AAS_IRDI_UNIT_GIBIBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_GIBIBYTE;

    /**
     * Denotes the unit terabyte on base 1024 {@link Eclass#IRDI_UNIT_TEBIBYTE}.
     */
    public static final String AAS_IRDI_UNIT_TEBIBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_TEBIBYTE;

    /**
     * Denotes the unit exabyte on base 1024 {@link Eclass#IRDI_UNIT_EXBIBYTE}.
     */
    public static final String AAS_IRDI_UNIT_EXBIBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_EXBIBYTE;

    /**
     * Denotes the unit petabyte on base 1024 {@link Eclass#IRDI_UNIT_PEBIBYTE}.
     */
    public static final String AAS_IRDI_UNIT_PEBIBYTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_PEBIBYTE;

    /**
     * Denotes the unit percent {@link Eclass#IRDI_UNIT_PERCENT}.
     */
    public static final String AAS_IRDI_UNIT_PERCENT 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_PERCENT;


    /**
     * Unit of oscillations per second {@link Eclass#IRDI_UNIT_HERTZ}.
     */
    public static final String AAS_IRDI_UNIT_HERTZ 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_HERTZ;

    /**
     * Unit of kilohertz {@link Eclass#IRDI_UNIT_KILOHERTZ}.
     */
    public static final String AAS_IRDI_UNIT_KILOHERTZ 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_KILOHERTZ;

    /**
     * Unit of megahertz {@link Eclass#IRDI_UNIT_MEGAHERTZ}.
     */
    public static final String AAS_IRDI_UNIT_MEGAHERTZ 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MEGAHERTZ;

    /**
     * Unit of gigahertz {@link Eclass#IRDI_UNIT_GIGAHERTZ}.
     */
    public static final String AAS_IRDI_UNIT_GIGAHERTZ 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_GIGAHERTZ;

    /**
     * Unit of terahertz {@link Eclass#IRDI_UNIT_TERAHERTZ}.
     */
    public static final String AAS_IRDI_UNIT_TERAHERTZ 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_TERAHERTZ;

    
    /**
     * Unit of time in {@link Eclass#IRDI_UNIT_PICOSECOND}.
     */
    public static final String AAS_IRDI_UNIT_PICOSECOND 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_PICOSECOND;
    
    /**
     * Unit of time in {@link Eclass#IRDI_UNIT_MICROSECOND}.
     */
    public static final String AAS_IRDI_UNIT_MICROSECOND 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MICROSECOND;
    
    /**
     * Unit of time in {@link Eclass#IRDI_UNIT_MILLISECOND}.
     */
    public static final String AAS_IRDI_UNIT_MILLISECOND 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MILLISECOND;

    /**
     * Unit of time in {@link Eclass#IRDI_UNIT_SECOND}.
     */
    public static final String AAS_IRDI_UNIT_SECOND 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_SECOND;

    /**
     * Unit of time in {@link Eclass#IRDI_UNIT_MINUTE}.
     */
    public static final String AAS_IRDI_UNIT_MINUTE 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MINUTE;

    /**
     * Unit of time in {@link Eclass#IRDI_UNIT_HOUR}.
     */
    public static final String AAS_IRDI_UNIT_HOUR 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_HOUR;

    
    /**
     * Unit of length in {@link Eclass#IRDI_UNIT_MILLIMETER}.
     */
    public static final String AAS_IRDI_UNIT_MILLIMETER 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_MILLIMETER;

    /**
     * Unit of length in {@link Eclass#IRDI_UNIT_CENTIMETER}.
     */
    public static final String AAS_IRDI_UNIT_CENTIMETER 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_CENTIMETER;

    /**
     * Unit of length in {@link Eclass#IRDI_UNIT_METER}.
     */
    public static final String AAS_IRDI_UNIT_METER 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_METER;
    
    /**
     * Unit of length in {@link Eclass#IRDI_UNIT_KILOMETER}.
     */
    public static final String AAS_IRDI_UNIT_KILOMETER 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_KILOMETER;

    
    /**
     * Unit of length per time in {@link Eclass#IRDI_UNIT_KILOMETERPERHOUR}.
     */
    public static final String AAS_IRDI_UNIT_KILOMETERPERHOUR 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_KILOMETERPERHOUR;

    
    /**
     * Unit of temperature in {@link Eclass#IRDI_UNIT_DEGREE_CELSIUS}.
     */
    public static final String AAS_IRDI_UNIT_DEGREE_CELSIUS 
        = IdentifierType.IRDI_PREFIX + Eclass.IRDI_UNIT_DEGREE_CELSIUS;

}
