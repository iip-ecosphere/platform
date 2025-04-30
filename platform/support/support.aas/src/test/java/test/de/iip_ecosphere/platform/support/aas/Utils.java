package test.de.iip_ecosphere.platform.support.aas;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;

/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

/**
 * Testing utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Utils {

    /**
     * Sets a property value depending on the capabilities of the AAS factory.
     * 
     * @param builder the property builder
     * @param value the value
     * @param getter the getter as invokable
     * @param setter the setter as invokable
     * @return {@code builder} for chaining
     */
    public static PropertyBuilder setValue(PropertyBuilder builder, Object value, Invokable getter, Invokable setter) {
        AasFactory factory = AasFactory.getInstance();
        if (factory.supportsPropertyFunctions()) {
            builder.bind(getter, setter);
        } else {
            builder.setValue(value);
        }
        return builder;
    }
    
    /**
     * Conditional assert depending on whether property functions are supported.
     * 
     * @param property the property
     * @param expectedIf the expected value if supported
     * @param expectedIfNot the expected value if not supported
     * @throws ExecutionException if accessing the property fails
     */
    public static void assertIfPropertyFunction(Property property, Object expectedIf, Object expectedIfNot) 
        throws ExecutionException {
        Assert.assertEquals(AasFactory.getInstance().supportsPropertyFunctions() ? expectedIf : expectedIfNot, 
            property.getValue());
    }

}
