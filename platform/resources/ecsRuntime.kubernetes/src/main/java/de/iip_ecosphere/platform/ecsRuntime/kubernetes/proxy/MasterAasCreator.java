/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Creates a test AAS.
 * 
 * @author Holger Eichelberger, Ahmad Alamoush, SSE
 */
public class MasterAasCreator {
    
    public static final String URN_AAS = "urn:::AAS:::MasterK8SAas#";
    public static final String AAS_NAME = "MasterK8SAas";
    public static final String AAS_SUBMODEL_NAME = "MasterK8SAasService";
    public static final String AAS_SUBMODEL_PROPERTY_NAME = "name";
    public static final String AAS_SUBMODEL_PROPERTY_VERSION = "version";
    public static final String AAS_SUBMODEL_PROPERTY_DESCRIPTION = "description";
    public static final String AAS_SUBMODEL_OPERATION_SEND_TO_K8S = "sendToK8S";
    
    public static final String VAB_PROPERTY_NAME = AAS_SUBMODEL_PROPERTY_NAME;
    public static final String VAB_PROPERTY_VERSION = AAS_SUBMODEL_PROPERTY_VERSION;
    public static final String VAB_PROPERTY_DESCRIPTION = AAS_SUBMODEL_PROPERTY_DESCRIPTION;
    public static final String VAB_OPERATION_SEND_TO_K8S = AAS_SUBMODEL_OPERATION_SEND_TO_K8S;

    /**
     * Creates an AAS for testing.
     * 
     * @param addr the server address (schema ignored)
     * 
     * @return the AAS
     */
    public static Aas createAas(ServerAddress addr) {
        AasFactory factory = AasFactory.getInstance();
        InvocablesCreator iCreator = factory.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, 
            addr.getHost(), addr.getPort());
        AasBuilder aasBuilder = factory.createAasBuilder(AAS_NAME, URN_AAS);
        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(AAS_SUBMODEL_NAME, null);
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_NAME)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(VAB_PROPERTY_NAME), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_VERSION)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(VAB_PROPERTY_VERSION), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_DESCRIPTION)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(VAB_PROPERTY_DESCRIPTION), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_SEND_TO_K8S)
            .setInvocable(iCreator.createInvocable(VAB_OPERATION_SEND_TO_K8S))
            .build();
        smBuilder.build();
        return aasBuilder.build();
    }
    
}
