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

/**
 * Relevant units, properties and concepts as defined in Eclass as basis for semantic ids in AAS. In normal cases,
 * please do not use this class rather than {@link Irdi}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Eclass {

    /**
     * Denotes a generic identifier (as string). Use is not completely compliant as we do not use an URI. No other 
     * form of identifier found.
     */
    public static final String IRDI_PROPERTY_IDENTIFIER = "0173-1#02-ABF809#001";

    /**
     * Denotes a software version (as string).
     */
    public static final String IRDI_PROPERTY_SOFTWARE_VERSION = "0173-1#02-AAM737#002";

    /**
     * Denotes a software name (as string).
     */
    public static final String IRDI_PROPERTY_SOFTWARE_NAME = "0173-1#02-AAO247#002";

    
    /**
     * Basic unit of a bit.
     */
    public static final String IRDI_UNIT_BYTE = "0173-1#05-AAA766#003";
    
    /**
     * Unit of kilobyte based on 1000 {@link #IRDI_UNIT_BYTE}.
     */
    public static final String IRDI_UNIT_KILOBYTE = "0173-1#05-AAA654#005";
    
    /**
     * Unit of megabyte based on 1000 {@link #IRDI_UNIT_KILOBYTE}.
     */
    public static final String IRDI_UNIT_MEGABYTE = "0173-1#05-AAA042#005";

    /**
     * Unit of gigabyte based on 1000 {@link #IRDI_UNIT_MEGABYTE}.
     */
    public static final String IRDI_UNIT_GIGABYTE = "0173-1#05-AAB066#003";

    /**
     * Unit of tera based on 1000 {@link #IRDI_UNIT_GIGABYTE}.
     */
    public static final String IRDI_UNIT_TERABYTE = "0173-1#05-AAB067#003";

    /**
     * {@link #IRDI_UNIT_MEGABYTE} per second.
     */
    public static final String IRDI_UNIT_MEGABYTE_PER_SECOND = "0173-1#05-AAB072#002";

    /**
     * {@link #IRDI_UNIT_GIGABYTE} per second.
     */
    public static final String IRDI_UNIT_GIGABYTE_PER_SECOND = "0173-1#05-AAA517#003";

    /**
     * Unit of kilobyte based on 1024 {@link #IRDI_UNIT_BYTE}.
     */
    public static final String IRDI_UNIT_KIBIBYTE = "0173-1#05-AAA072#002";

    /**
     * Unit of megabyte based on 1024 {@link #IRDI_UNIT_KILOBYTE}.
     */
    public static final String IRDI_UNIT_MEBIBYTE = "0173-1#05-AAA444#002";

    /**
     * Unit of gigabyte based on 1024 {@link #IRDI_UNIT_MEGABYTE}.
     */
    public static final String IRDI_UNIT_GIBIBYTE = "0173-1#05-AAA215#002";

    /**
     * Unit of terabyte based on 1024 {@link #IRDI_UNIT_GIBIBYTE}.
     */
    public static final String IRDI_UNIT_TEBIBYTE = "0173-1#05-AAA283#002";

    /**
     * Unit of exabyte based on 1024 {@link #IRDI_UNIT_TEBIBYTE}.
     */
    public static final String IRDI_UNIT_EXBIBYTE = "0173-1#05-AAA584#002";

    /**
     * Unit of petabyte based on 1024 {@link #IRDI_UNIT_EXBIBYTE}.
     */
    public static final String IRDI_UNIT_PEBIBYTE = "0173-1#05-AAA600#002";

    
    /**
     * Unit of percent.
     */
    public static final String IRDI_UNIT_PERCENT = "0173-1#05-AAA129#003";
    
    
    /**
     * Unit of oscillations per second.
     */
    public static final String IRDI_UNIT_HERTZ = "0173-1#05-AAA351#003";

    /**
     * Unit of 1000 oscillations per {@link #IRDI_UNIT_HERTZ}.
     */
    public static final String IRDI_UNIT_KILOHERTZ = "0173-1#05-AAA033#003";

    /**
     * Unit of 1000 oscillations per {@link #IRDI_UNIT_KILOHERTZ}.
     */
    public static final String IRDI_UNIT_MEGAHERTZ = "0173-1#05-AAA581#003";

    /**
     * Unit of 1000 oscillations per {@link #IRDI_UNIT_MEGAHERTZ}.
     */
    public static final String IRDI_UNIT_GIGAHERTZ = "0173-1#05-AAA505#003";

    /**
     * Unit of 1000 oscillations per {@link #IRDI_UNIT_TERAHERTZ}.
     */
    public static final String IRDI_UNIT_TERAHERTZ = "0173-1#05-AAA190#003";

    
    /**
     * Unit of time in ps.
     */
    public static final String IRDI_UNIT_PICOSECOND = "0173-1#05-AAA095#004";
    
    /**
     * Unit of time in ys.
     */
    public static final String IRDI_UNIT_MICROSECOND = "0173-1#05-AAA496#003";
    
    /**
     * Unit of time in ms.
     */
    public static final String IRDI_UNIT_MILLISECOND = "0173-1#05-AAA114#003";

    /**
     * Unit of time in s.
     */
    public static final String IRDI_UNIT_SECOND = "0173-1#05-AAA203#003";

    /**
     * Unit of time in min.
     */
    public static final String IRDI_UNIT_MINUTE = "0173-1#05-AAA109#003";

    /**
     * Unit of time in hour.
     */
    public static final String IRDI_UNIT_HOUR = "0173-1#05-AAA390#003";

    
    /**
     * Unit of length in mm.
     */
    public static final String IRDI_UNIT_MILLIMETER = "0173-1#05-AAA480#003";

    /**
     * Unit of length in cm.
     */
    public static final String IRDI_UNIT_CENTIMETER = "0173-1#05-AAA008#003";

    /**
     * Unit of length in cm.
     */
    public static final String IRDI_UNIT_METER = "0173-1#05-AAA551#003";
    
    /**
     * Unit of length in km.
     */
    public static final String IRDI_UNIT_KILOMETER = "0173-1#05-AAA595#003";

    
    /**
     * Unit of length per time in km/h.
     */
    public static final String IRDI_UNIT_KILOMETERPERHOUR = "0173-1#05-AAA208#003";

}
