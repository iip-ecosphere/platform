/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Utilities to manage Jackson JSON data bindings.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JacksonUtils {

    public static final SimpleAbstractTypeResolver IIP_TYPE_RESOLVER = new SimpleAbstractTypeResolver() {
        
        private static final long serialVersionUID = -3746467806797935401L;

        @Override
        public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
            JavaType result = null;
            // for generated IIP-Ecosphere data interfaces, we can try it with Impl classes
            String className = type.getRawClass().getName();
            if (type.isInterface() && className.startsWith("iip.")) {
                String name = className + "Impl";
                try {
                    Class<?> cls = Class.forName(name);
                    result = config.getTypeFactory().constructType(cls);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (null == result) {
                result = super.findTypeMapping(config, type);
            }
            return result;
        }
            
    };
    
    /**
     * Configures a Jackson object mapper for IIP conventions.
     * 
     * @param mapper the mapper to be configured
     * @return {@code mapper}
     */
    public static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        SimpleModule iipModule = new SimpleModule();
        iipModule.setAbstractTypes(IIP_TYPE_RESOLVER);
        return mapper.registerModule(iipModule);
    }

}
