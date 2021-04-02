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

package test.de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.TypedDataDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * A test service descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceDescriptor extends AbstractServiceDescriptor<MyArtifactDescriptor> {

    /**
     * A simple data descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DataDesc implements TypedDataDescriptor {

        private String name;
        private String description;
        private Class<?> type;

        /**
         * Creates a data descriptor.
         * 
         * @param name the name
         * @param description the description
         * @param type the type
         */
        private DataDesc(String name, String description, Class<?> type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public String getDescription() {
            return description;
        }
        
    }

    /**
     * A simple connector descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ConnectorDesc extends DataDesc implements TypedDataConnectorDescriptor {

        /**
         * Creates a data descriptor.
         * 
         * @param name the name
         * @param description the description
         * @param type the type
         */
        private ConnectorDesc(String name, String description, Class<?> type) {
            super(name, description, type);
        }

    }
    
    private static int count = 0;
    
    /**
     * Creates an instance. Call {@link #setClassification(ServiceKind, boolean)} afterwards.
     * 
     * @param id the service id
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    protected MyServiceDescriptor(String id, String name, String description, Version version) {
        super(id, name, description, version);
        addParameter(new DataDesc("NAME", "reconfigures the name", String.class));
        addInputDataConnector(new ConnectorDesc("conn-" + count, "", Integer.TYPE));
        count++;
        addOutputDataConnector(new ConnectorDesc("conn-" + count, "", Integer.TYPE));
    }
    
}