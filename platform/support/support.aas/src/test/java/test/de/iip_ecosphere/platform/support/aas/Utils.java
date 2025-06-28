package test.de.iip_ecosphere.platform.support.aas;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Property;

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
