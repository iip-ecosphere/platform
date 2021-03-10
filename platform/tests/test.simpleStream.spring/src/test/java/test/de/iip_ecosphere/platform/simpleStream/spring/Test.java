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
package test.de.iip_ecosphere.platform.simpleStream.spring;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Defines the test stream to be processed. We assume that a MQTT v5 broker is running.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootApplication
@Import(Configuration.class)
public class Test {
    
    private static ConfigurableApplicationContext ctx;
    @Autowired
    private Configuration config;
    private int ingestCount = 0;
    
    /**
     * Creates the data.
     * 
     * @return the data (generator)
     */
    @Bean
    public Supplier<String> create() {
        return () -> {
            if (config.isDebug()) {
                System.out.println("Ingest " + ingestCount);
            }
            if (ingestCount > config.getIngestCount()) {
                ctx.close();
                System.exit(0);
            }
            ingestCount++;
            return "DATA";
        };
    }
    
    /**
     * Consumes the received data.
     * 
     * @return the data receiver
     */
    @Bean
    public Consumer<String> log() {
        return data -> {
            if (config.isDebug()) {
                System.out.println("Received: " + data);
            }
        };
    }
    
    /**
     * Main function.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // start spring cloud app
        SpringApplication app = new SpringApplication(Test.class);
        ctx = app.run(args);
    }
    
}
