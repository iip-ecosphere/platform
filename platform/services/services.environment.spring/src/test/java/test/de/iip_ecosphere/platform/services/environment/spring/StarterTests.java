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

package test.de.iip_ecosphere.platform.services.environment.spring;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.spring.Starter;

/**
 * Tests {@link Starter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StarterTests {
    
    /**
     * Tests {@link Starter#parseExternConnections(String[], java.util.function.Consumer)}.
     */
    @Test
    public void testParseExternConnections() {
        String[] args = {"--iip.service.unknown=true", "--Dsystem.property=5", 
            "--spring.cloud.stream.bindings.receiveDecisionResult_AppAas-in-0.binder=external"};
        Set<String> bindings = new HashSet<>();
        Starter.parseExternConnections(args, s -> bindings.add(s));
        Assert.assertTrue(bindings.contains("receiveDecisionResult_AppAas-in-0"));
    }

}