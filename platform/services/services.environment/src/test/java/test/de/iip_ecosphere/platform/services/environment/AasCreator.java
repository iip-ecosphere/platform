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
import de.iip_ecosphere.platform.services.environment.ServiceStub;
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
    public static final String AAS_SUBMODEL_OPERATION_MIGRATE = "migrate";
    public static final String AAS_SUBMODEL_OPERATION_UPDATE = "update";
    public static final String AAS_SUBMODEL_OPERATION_SWITCH = "switch";
    public static final String AAS_SUBMODEL_OPERATION_RECONF = "reconfigure";
    
    /**
     * Collects AAS creation results.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class AasResult {
        
        private ServiceStub stub;
        
        /**
         * Returns the service stub.
         * 
         * @return the stub
         */
        public ServiceStub getStub() {
            return stub;
        }
        
    }
    
    /**
     * Creates an AAS for testing.
     * 
     * @param addr the server address (schema ignored)
     * @param service the service to create the AAS for (qualified naming)
     * @param protocol the AAS implementation protocol (see {@link AasFactory#getProtocols()}
     * @return the AAS
     */
    public static Aas createAas(ServerAddress addr, Service service, String protocol) {
        return createAas(addr, service, protocol, null);
    }
    
    /**
     * Creates an AAS for testing.
     * 
     * @param addr the server address (schema ignored)
     * @param service the service to create the AAS for (qualified naming)
     * @param protocol the AAS implementation protocol (see {@link AasFactory#getProtocols()}
     * @param result optional instance to be modified as side effect to have more details about the creation, 
     *     may be <b>null</b> for nothing
     * @return the AAS
     */
    public static Aas createAas(ServerAddress addr, Service service, String protocol, AasResult result) {
        AasFactory factory = AasFactory.getInstance();
        InvocablesCreator iCreator = factory.createInvocablesCreator(protocol, addr.getHost(), addr.getPort());
        AasBuilder aasBuilder = factory.createAasBuilder(AAS_NAME, URN_AAS);
        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(AAS_SUBMODEL_NAME, null);
        ServiceStub stub = new ServiceStub(iCreator, service.getId());
        
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_ID)
            .setType(Type.STRING)
            .bind(stub.getGetter(NAME_PROP_ID), stub.getSetter(NAME_PROP_ID))
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_NAME)
            .setType(Type.STRING)
            .bind(stub.getGetter(NAME_PROP_NAME), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_VERSION)
            .setType(Type.STRING)
            .bind(stub.getGetter(NAME_PROP_VERSION), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_DESCRIPTION)
            .setType(Type.STRING)
            .bind(stub.getGetter(NAME_PROP_DESCRIPTION), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_STATE)
            .setType(Type.STRING)
            .bind(stub.getGetter(NAME_PROP_STATE), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_KIND)
            .setType(Type.STRING)
            .bind(stub.getGetter(NAME_PROP_KIND), InvocablesCreator.READ_ONLY)
            .build();
        smBuilder.createPropertyBuilder(AAS_SUBMODEL_PROPERTY_DEPLOYABLE)
            .setType(Type.BOOLEAN)
            .bind(stub.getGetter(NAME_PROP_DEPLOYABLE), InvocablesCreator.READ_ONLY)
            .build();
        
        // returns are always strings here through JsonResultWrapper
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_ACTIVATE)
            .setInvocable(stub.getOperation(NAME_OP_ACTIVATE))
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_PASSIVATE)
            .setInvocable(stub.getOperation(NAME_OP_PASSIVATE))
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_SETSTATE)
            .setInvocable(stub.getOperation(NAME_OP_SET_STATE))
            .addInputVariable("state", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_MIGRATE)
            .setInvocable(stub.getOperation(NAME_OP_MIGRATE))
            .addInputVariable("resourceId", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_UPDATE)
            .setInvocable(stub.getOperation(NAME_OP_UPDATE))
            .addInputVariable("location", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_SWITCH)
            .setInvocable(stub.getOperation(NAME_OP_SWITCH))
            .addInputVariable("targetId", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.createOperationBuilder(AAS_SUBMODEL_OPERATION_RECONF)
            .setInvocable(stub.getOperation(NAME_OP_RECONF))
            .addInputVariable("values", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        smBuilder.build();
        
        if (result != null) {
            result.stub = stub;
        }
        
        return aasBuilder.build();
    }
    
}
