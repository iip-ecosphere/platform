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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactory;
import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.readString;

/**
 * A Asset Administration Shell for device management functionalities.
 * Mostly called by northbound services. A device should not rely on these functionalities.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceManagementAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_RESOURCES;
    public static final String NAME_COLL_DEVICE_MANAGER = "deviceManager";
    public static final String NAME_OP_UPDATE_RUNTIME = "updateRuntime";
    public static final String NAME_OP_ESTABLISH_SSH = "establishSsh";
    public static final String NAME_OP_SET_CONFIG = "setConfig";

    public static final String ECS_UPDATE_URI = "https://an.uri.local";

    /**
     * Basically registers the AAS for the device management.
     *
     * @param aasBuilder the aasBuilder to contributeTo
     * @param iCreator an InvocablesCreator
     * @return null
     */
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        SubmodelElementCollection.SubmodelElementCollectionBuilder deviceManager =
                smB.createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_MANAGER, false, false);

        deviceManager.createOperationBuilder(NAME_OP_UPDATE_RUNTIME)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_UPDATE_RUNTIME)))
                .addInputVariable("deviceId", Type.STRING)
                .build(Type.NONE);
        
        deviceManager.createOperationBuilder(NAME_OP_ESTABLISH_SSH)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_ESTABLISH_SSH)))
                .addInputVariable("deviceId", Type.STRING)
                .build(Type.STRING);
        
        deviceManager.createOperationBuilder(NAME_OP_SET_CONFIG)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_SET_CONFIG)))
                .addInputVariable("deviceId", Type.STRING)
                .addInputVariable("configPath", Type.STRING)
                .build(Type.NONE);
        
        deviceManager.build();

        smB.defer();
        return null;
    }

    /**
     * Defines the operations details.
     *
     * @param sBuilder the ProtocolServerBuilder
     */
    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_UPDATE_RUNTIME),
            new JsonResultWrapper(p -> {
                DeviceManagementFactory.getDeviceManagement().updateRuntime(readString(p));
                return null;
            })
        );

        sBuilder.defineOperation(getQName(NAME_OP_ESTABLISH_SSH),
            p -> {
                DeviceRemoteManagementOperations.SSHConnectionDetails connectionDetails;
                try {
                    connectionDetails = DeviceManagementFactory.getDeviceManagement().establishSsh(readString(p));
                    return new ObjectMapper().writeValueAsString(connectionDetails);
                } catch (JsonProcessingException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            });

        sBuilder.defineOperation(getQName(NAME_OP_SET_CONFIG),
            new JsonResultWrapper(p -> {
                DeviceManagementFactory.getDeviceManagement().setConfig(readString(p), readString(p, 1));
                return null;
            }));
    }

    /**
     * Notify the aas if a new config should be set.
     * This method will redirect the request to the aas of the resource id.
     *
     * @param id the device id
     * @param downloadUri the download uri
     * @param location the location to put the configuration relative to /
     */
    public static void notifySetConfig(String id, String downloadUri, String location) {
        ActiveAasBase.processNotification(AasPartRegistry.NAME_SUBMODEL_RESOURCES, (sub, aas) -> {
            try {
                sub.getSubmodelElementCollection(id)
                        .getOperation("setConfig")
                        .invoke(downloadUri, location);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Notify if a device needs an update and redirect the request.
     *
     * @param id the device id
     * @param downloadUrl the download url
     */
    public static void notifyUpdateRuntime(String id, String downloadUrl) {
        ActiveAasBase.processNotification(AasPartRegistry.NAME_SUBMODEL_RESOURCES, (sub, aas) -> {
            try {
                sub.getSubmodelElementCollection(id)
                    .getOperation("updateRuntime")
                    .invoke(downloadUrl);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Returns a qualified name within this submodel.
     * 
     * @param name the name to be turned into a qualified name
     * @return the qualified name
     */
    private String getQName(String name) {
        return NAME_SUBMODEL + "_" + name;
    }

    /**
     * This aas is a {@code Kind.ACTIVE}.
     * @return Kind.ACTIVE.
     */
    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    /**
     * In case this code is used as library one doesn't want that the aas is setup.
     * @return {@code true} if code based is not used as a library and the aas should be deployed.
     */
    @Override
    public boolean isValid() {
        return null != DeviceManagementFactory.getDeviceManagement() 
            || null != DeviceRegistryFactory.getDeviceRegistry();
    }

}
