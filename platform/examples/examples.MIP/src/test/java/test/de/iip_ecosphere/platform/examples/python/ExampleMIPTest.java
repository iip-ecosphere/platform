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

package test.de.iip_ecosphere.platform.examples.python;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.PythonUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import org.junit.Assert;

/**
 * Basic testing for the Python service implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ExampleMIPTest {

    /**
     * Tests whether the service code can be compiled and basically executed.
     * 
     * @throws IOException shall not occur
     * @throws InterruptedException shall not occur
     */
    @Test
    public void testService() throws IOException, InterruptedException {
        Assert.assertTrue(true);
    }
    
}
