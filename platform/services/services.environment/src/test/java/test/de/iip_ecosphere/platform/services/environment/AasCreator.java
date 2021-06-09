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

package test.de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

import static de.iip_ecosphere.platform.services.environment.ServiceMapper.*;

/**
 * Creates a test AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasCreator {
    
    public static final String URN_AAS = "urn:::AAS:::AiTestAas#";
    public static final String AAS_NAME = "AiTestAas";
    public static final String AAS_SUBMODEL_NAME = "AiService";
    public static final String AAS_SUBMODEL_PROPERTY_ID = "id";
    public static final String AAS_SUBMODEL_PROPERTY_NAME = "name";
    public static final String AAS_SUBMODEL_PROPERTY_VERSION = "version";
    public static final String AAS_SUBMODEL_PROPERTY_DESCRIPTION = "description";
    public static final String AAS_SUBMODEL_PROPERTY_STATE = "state";
    public static final String AAS_SUBMODEL_PROPERTY_KIND = "kind";
    public static final String AAS_SUBMODEL_PROPERTY_DEPLOYABLE = "deployable";
    public static final String AAS_SUBMODEL_OPERATION_PASSIVATE = "passivate";
    public static final String AAS_SUBMODEL_OPERATION_ACTIVATE = "activate";
    public static final String AAS_SUBMODEL_OPERATION_SETSTATE = "setState";
    
    /**
     * Creates an AAS for testing.
     * 
     * @param addr the server address (schema ignored)
     * @param service the service to create the AAS for (qualified naming)
     * @return the AAS
     */
    public static Aas createAas(ServerAddress addr, Service service) {
        AasFactory factory = AasFactory.getInstance();
        InvocablesCreator iCreator = factory.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, 
            addr.getHost(), addr.getPort());
        AasBuilder aasBuilder = factory.createAasBuilder(AAS_NAME, URN_AAS);
        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(AAS_SUBMODEL_NAME, null);
        
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_ID)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_ID)), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_NAME)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_NAME)), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_VERSION)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_VERSION)), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_DESCRIPTION)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_DESCRIPTION)), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_STATE)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_STATE)), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_KIND)
            .setType(Type.STRING)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_KIND)), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_DEPLOYABLE)
            .setType(Type.BOOLEAN)
            .bind(iCreator.createGetter(getQName(service, NAME_PROP_DEPLOYABLE)), InvocablesCreator.READ_ONLY)
            .build();
        
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_ACTIVATE)
            .setInvocable(iCreator.createInvocable(getQName(service, NAME_OP_ACTIVATE)))
            .addOutputVariable("result", Type.STRING) // through JsonResultWrapper
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_PASSIVATE)
            .setInvocable(iCreator.createInvocable(getQName(service, NAME_OP_PASSIVATE)))
            .addOutputVariable("result", Type.STRING) // through JsonResultWrapper
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_SETSTATE)
            .setInvocable(iCreator.createInvocable(getQName(service, NAME_OP_SET_STATE)))
            .addInputVariable("state", Type.STRING)
            .addOutputVariable("result", Type.STRING) // through JsonResultWrapper
            .build();
        smBuilder.build();
        return aasBuilder.build();
    }
    
}
