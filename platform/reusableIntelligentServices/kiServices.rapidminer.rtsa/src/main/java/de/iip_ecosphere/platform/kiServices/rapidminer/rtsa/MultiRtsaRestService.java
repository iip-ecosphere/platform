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

package de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.IOException;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractDelegatingMultiService;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * Multi-type RTSA service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiRtsaRestService extends AbstractDelegatingMultiService<RtsaRestService<String, String>> {
    
    /**
     * Extended RTSA service for multi-type queries.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ExRtsaRestService extends RtsaRestService<String, String> {
        
        /**
         * Creates an instance.
         * 
         * @param yaml the string information as YAML
         */
        public ExRtsaRestService(YamlService yaml) {
            super(TypeTranslators.STRING, TypeTranslators.STRING, null, yaml);
        }
        
        @Override
        protected String adjustRestQuery(String input, String inTypeName) {
            return "{\"" + inTypeName + "\":[" + input + "]}";
        }
        
        @Override
        protected void handleReception(String data) {
            if (data.startsWith("{\"") && data.endsWith("]}")) {
                data = data.substring(2, data.length() - 2);
                int pos = data.indexOf("\"");
                if (pos > 0) {
                    String typeName = data.substring(0, pos);
                    data = data.substring(pos + 1);
                    if (data.startsWith(":[")) {
                        data = data.substring(2);
                        OutTypeInfo<?> info = getImpl().getOutTypeInfo(typeName);
                        if (null != info) {
                            handleResult(info.getType(), data, typeName);
                        } else {
                            LoggerFactory.getLogger(getClass())
                                .error("No output type translator registered for: {}", typeName);
                        }
                    }
                }
            }
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param yaml the service description YAML
     */
    public MultiRtsaRestService(YamlService yaml) {
        super(yaml);
    }

    /**
     * Creates the nested service instance to delegate to.
     * 
     * @param yaml the service description YAML
     * @return the service instance
     */
    protected RtsaRestService<String, String> createService(YamlService yaml) {
        return new ExRtsaRestService(yaml);
    }

    @Override
    protected void processImpl(String inType, String data) throws IOException {
        getService().process(data, inType);
    }

}
