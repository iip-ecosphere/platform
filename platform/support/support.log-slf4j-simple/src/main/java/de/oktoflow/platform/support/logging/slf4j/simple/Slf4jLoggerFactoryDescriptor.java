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

package de.oktoflow.platform.support.logging.slf4j.simple;

import de.iip_ecosphere.platform.support.logging.ILoggerFactory;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The SLF4j plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Slf4jLoggerFactoryDescriptor extends SingletonPluginDescriptor<ILoggerFactory> {

    /**
     * oktoflow logger factory delegating to SLF4j.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Slf4jLoggerFactory implements ILoggerFactory {

        @Override
        public Logger createLogger(String name) {
            return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(name));
        }
        
    }
    
    /**
     * Creates the descriptor.
     */
    public Slf4jLoggerFactoryDescriptor() {
        super("log-slf4j-simple", null, ILoggerFactory.class, p -> new Slf4jLoggerFactory());
    }
    
}
