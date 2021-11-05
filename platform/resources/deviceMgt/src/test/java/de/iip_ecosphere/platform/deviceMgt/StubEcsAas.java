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

package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

import static org.mockito.Mockito.mock;

/**
 * A faked ECS AAS for testing. 
 * @author Dennis Pidun, University of Hildesheim
 */
public class StubEcsAas implements AasContributor {

    static final String A_DEVICE = "A_DEVICE";
    private static JsonResultWrapper updateRuntimeMock;
    private static JsonResultWrapper setConfigMock;
    private static JsonResultWrapper createRemoteConnectionCredentialsMock;

    /**
     * Returns the {@link #updateRuntimeMock}.
     *  
     * @return the mock
     */
    static JsonResultWrapper getUpdateRuntimeMock() {
        return updateRuntimeMock;
    }

    /**
     * Returns the {@link #setConfigMock}.
     *  
     * @return the mock
     */
    static JsonResultWrapper getSetConfigMock() {
        return setConfigMock;
    }

    /**
     * Returns the {@link #createRemoteConnectionCredentialsMock}.
     *  
     * @return the mock
     */
    static JsonResultWrapper getCreateRemoteConnectionCredentialsMock() {
        return createRemoteConnectionCredentialsMock;
    }

    @Override
    public Aas contributeTo(Aas.AasBuilder aasBuilder, InvocablesCreator iCreator) {
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(AasPartRegistry.NAME_SUBMODEL_RESOURCES, null);

        SubmodelElementCollection.SubmodelElementCollectionBuilder device =
                smB.createSubmodelElementCollectionBuilder(A_DEVICE, false, false);

        device.createOperationBuilder("updateRuntime")
                .setInvocable(iCreator.createInvocable(A_DEVICE + "_" + "updateRuntime"))
                .addInputVariable("uri", Type.STRING)
                .addOutputVariable("result", Type.NONE)
               .build();

        device.createOperationBuilder("createRemoteConnectionCredentials")
                .setInvocable(iCreator.createInvocable(A_DEVICE + "_" + "createRemoteConnectionCredentials"))
                .addOutputVariable("key", Type.STRING)
                .addOutputVariable("secret", Type.STRING)
                .build();

        device.createOperationBuilder("setConfig")
                .setInvocable(iCreator.createInvocable(A_DEVICE + "_" + "setConfig"))
                .addInputVariable("uri", Type.STRING)
                .addInputVariable("location", Type.STRING)
                .addOutputVariable("result", Type.NONE)
                .build();

        device.createPropertyBuilder("runtimeName")
                .setValue(Type.STRING, "stubRuntime")
                .build();

        device.createPropertyBuilder("runtimeVersion")
                .setValue(Type.INTEGER, 1)
                .build();

        device.build();
        smB.build();

        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        updateRuntimeMock = mock(JsonResultWrapper.class);
        createRemoteConnectionCredentialsMock = mock(JsonResultWrapper.class);
        setConfigMock = mock(JsonResultWrapper.class);

        sBuilder.defineOperation(A_DEVICE + "_" + "updateRuntime",
                updateRuntimeMock);

        sBuilder.defineOperation(A_DEVICE + "_" + "createRemoteConnectionCredentials",
                createRemoteConnectionCredentialsMock);

        sBuilder.defineOperation(A_DEVICE + "_" + "setConfig",
                setConfigMock);
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
