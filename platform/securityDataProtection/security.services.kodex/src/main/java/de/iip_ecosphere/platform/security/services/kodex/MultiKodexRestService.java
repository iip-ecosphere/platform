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

package de.iip_ecosphere.platform.security.services.kodex;

import java.io.IOException;

import de.iip_ecosphere.platform.services.environment.AbstractDelegatingMultiService;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Multi-type KODEX Rest service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiKodexRestService extends AbstractDelegatingMultiService<KodexRestService<String, String>> {
    
    private String dataSpec;
    
    /**
     * Extended KODEX service for multi-type queries.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ExKodexRestService extends KodexRestService<String, String> {
        
        /**
         * Creates an instance.
         * 
         * @param yaml the string information as YAML
         * @param dataSpec name of the data spec file (within the process home path) to pass to KODEX; related files 
         *     such as api or actions must be there as well and referenced from the data spec file 
         */
        public ExKodexRestService(YamlService yaml, String dataSpec) {
            super(TypeTranslators.STRING, TypeTranslators.STRING, null, yaml, dataSpec);
        }
        
        @Override
        protected String adjustRestQuery(String input, String inTypeName) {
            return "{\"" + inTypeName + "\":[" + input + "]}";
        }
        
        @Override
        protected void handleReception(String data) {
            // simple approach for now, generically parsing to JSON would be better
            data = data.replace("{\"data\":{\"errors\":[],", "");
            data = data.replace(",\"messages\":[],\"warnings\":[]}}", "");
            if (data.startsWith("\"") && data.endsWith("]")) {
                data = data.substring(1, data.length() - 1);
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
     * @param dataSpec name of the data spec file (within the process home path) to pass to KODEX; related files such 
     *     as api or actions must be there as well and referenced from the data spec file 
     */
    public MultiKodexRestService(YamlService yaml, String dataSpec) {
        super(yaml);
        this.dataSpec = dataSpec;
        assignService(yaml); // force late 
    }

    @Override
    protected void initService(YamlService yaml) {
        // prevent initial creation as dataSpec is not yet set in parent constructor
    }

    /**
     * Creates the nested service instance to delegate to.
     * 
     * @param yaml the service description YAML
     * @return the service instance
     */
    protected KodexRestService<String, String> createService(YamlService yaml) {
        return new ExKodexRestService(yaml, dataSpec);
    }

    @Override
    protected void processImpl(String inType, String data) throws IOException {
        getService().process(data, inType);
    }

}

