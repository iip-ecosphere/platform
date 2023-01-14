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

package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsaFake;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.kiServices.rapidminer.rtsaFake.FakeRtsa;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Tests the fake RTSA.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeRtsaTest {
    
    /**
     * Tests the fake RTSA.
     */
    @Test
    public void testFakeRtsa() throws IOException {
        System.setProperty("scoring-agent.baseDir", "src/test/resources");
        FakeRtsa.main(new String[] {});
        TimeUtils.sleep(6000); // wait to be initialized
        // could send/receive a bit data here
    }
    
}
