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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.util.Optional;

import org.junit.Test;

import de.iip_ecosphere.platform.support.JavaBinaryPathDescriptor;
import de.iip_ecosphere.platform.support.JavaUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.junit.Assert;

/**
 * Tests the {@link JavaBinaryPathDescriptor} and {@link JavaUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JavaUtilsTest {
    
    /**
     * Tests the descriptor hook-in.
     */
    @Test
    public void testDescriptor() {
        Optional<JavaBinaryPathDescriptor> desc = ServiceLoaderUtils.findFirst(JavaBinaryPathDescriptor.class);
        Assert.assertTrue(desc.isPresent());
        Assert.assertNotNull(JavaUtils.getJavaBinaryPath());
    }

}
