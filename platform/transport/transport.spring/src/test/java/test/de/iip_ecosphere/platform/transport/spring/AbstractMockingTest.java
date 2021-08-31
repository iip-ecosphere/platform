/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport.spring;

import java.util.HashMap;

import org.springframework.cloud.stream.binder.BinderType;
import org.springframework.cloud.stream.binder.BinderTypeRegistry;
import org.springframework.cloud.stream.binder.DefaultBinderTypeRegistry;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.integration.support.utils.IntegrationUtils;

import de.iip_ecosphere.platform.transport.spring.BinderFix;

/**
 * Abstract test class, which mocks certain spring cloud steam beans so that {@link BinderFix} can be instantiated.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractMockingTest {

    /**
     * Creates a default empty binder type registry.
     * 
     * @return the registry instance
     */
    @Bean
    public BinderTypeRegistry createBinderTypeRegistry() {
        return new DefaultBinderTypeRegistry(new HashMap<String, BinderType>());
    }
    
    /**
     * Creates a mocked binding service properties instance.
     * 
     * @return the properties
     */
    @Bean
    public BindingServiceProperties createBindingServiceProperties() {
        return new BindingServiceProperties();
    }

    /**
     * Creates a default conversion service.
     * 
     * @return the conversion service
     */
    @Bean(IntegrationUtils.INTEGRATION_CONVERSION_SERVICE_BEAN_NAME)
    public ConversionService createConversionService() {
        return new DefaultConversionService();
    }
    
}
