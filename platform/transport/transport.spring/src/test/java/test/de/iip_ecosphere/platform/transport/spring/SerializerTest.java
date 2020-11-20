/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Brings up a spring application doing nothing rather than testing the automatic registration of serializers during 
 * startup.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = SerializerTest.class)
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "de.iip_ecosphere.platform.transport.spring")
public class SerializerTest {

    /**
     * Tests the automatic registration of serializes via the given configuration and the 
     * startup application listener.
     */
    @Test
    public void testSerializer() {
        SpringApplication.run(SerializerTest.class);
        Assert.assertTrue(SerializerRegistry.hasSerializer(String.class));
    }
    
}
